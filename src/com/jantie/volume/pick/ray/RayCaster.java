package com.jantie.volume.pick.ray;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Vector3f;

import com.kitfox.volume.viewer.ViewerCube;

/**
 * Handles Ray Casts
 *	result1 and/or 2 are set after a successful raycast
 * @author Jan
 */
public class RayCaster {
	ViewerCube cube;
	public Vector3f sphereHit1,sphereHit2,rayDirection = null;
	float radius =  (float)Math.sqrt(2);
	public float[] rayProfile;
	public float[] orginalRayProfile;
	public float[] gpuRayProfile;
	public float[] gpuAccSum;
	public float[] gpuFirstDerv;
	public float[] gpuSecDerv;
	public Vector3f[] rayPositions;
	public ArrayList<IRayCastChangeListner> listners = new ArrayList<IRayCastChangeListner>();
	
	public RayCaster(ViewerCube cube) {
		this.cube = cube;
	}

	
	/**
	 * gets the start and end point of the ray and the handles listeners
	 * @param gl
	 * @param xPos screen mouse
	 * @param yPos screen mouse
	 * @return raycast hit
	 */
	public boolean castRay(GL2 gl, float xPos, float yPos){
		sphereHit1 = sphereHit2 = rayDirection = null;
		//init and fill buffer for unproject()
		FloatBuffer projBuff = FloatBuffer.allocate(16);
	    projBuff.rewind();
	    gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projBuff);
	    
	    FloatBuffer modelViewBuff = FloatBuffer.allocate(16);
	    modelViewBuff.rewind();
	    gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelViewBuff);
	    
	    IntBuffer viewportBuff = IntBuffer.allocate(4);
	    modelViewBuff.rewind();
	    gl.glGetIntegerv(GL.GL_VIEWPORT, viewportBuff);
	    
	    FloatBuffer worldcoordsBuffNear = FloatBuffer.allocate(4);
	    worldcoordsBuffNear.rewind();
	    
	    FloatBuffer worldcoordsBuffFar = FloatBuffer.allocate(4);
	    worldcoordsBuffFar.rewind();
	    
	    float realy = viewportBuff.array()[3] - (int) yPos - 1; //Internet said so
	    
	    GLU glu = new GLU();
	    if(glu.gluUnProject(xPos,realy,0,modelViewBuff,projBuff,viewportBuff,worldcoordsBuffNear)){ //z=0 ->nearplane
	    	if(glu.gluUnProject(xPos,realy,1,modelViewBuff,projBuff,viewportBuff,worldcoordsBuffFar)){ // z= 1 -> farplane
//		    	debug print
//		    	System.out.print("wc Near: ");
//		    	while (worldcoordsBuffNear.hasRemaining()){
//		    		System.out.print(worldcoordsBuffNear.get()+" : ");
//		    	}
//		    	System.out.println();
//		    	System.out.print("wc far: ");
//		    	while (worldcoordsBuffFar.hasRemaining()){
//		    		System.out.print(worldcoordsBuffFar.get()+" : ");
//		    	}
//		    	System.out.println();
	    		float[] wdn = worldcoordsBuffNear.array();
	    		Vector3f nearPlaneHit = new Vector3f(wdn[0],wdn[1],wdn[2]);
	    		float[] wdf = worldcoordsBuffFar.array();
	    		Vector3f farPlaneHit = new Vector3f(wdf[0],wdf[1],wdf[2]);
	    		
	    		//http://www.lighthouse3d.com/tutorials/maths/ray-sphere-intersection/
	    		rayDirection = new Vector3f(farPlaneHit);
	    		rayDirection.sub(nearPlaneHit);
	    		rayDirection.normalize();
	    		
	    		Vector3f v = new Vector3f(nearPlaneHit); 
	    		v.scale(-1f);//-sphere center which is the origin
	    		
	    		float  DdotV = v.dot(rayDirection);
	    		
	    		Vector3f pc = new Vector3f(rayDirection); // pc = rayDir * DdotV + nearPH
	    		pc.scale(DdotV);
	    		pc.add(nearPlaneHit); 
	    		
	    		float distance = pc.length();
	    		

	    		if (distance < radius){//through sphere
	    			//pythagoras
	    			float a = radius;
	    			float b = pc.length(); // = |pc - origin|
	    			double c = Math.sqrt(Math.pow(a,2)- Math.pow(b, 2));
	    			Vector3f di1Vec = new Vector3f(pc);
	    			di1Vec.sub(nearPlaneHit);
	    			
	    			float di1 = di1Vec.length() - (float)(c);
	    			
	    			sphereHit1 = new Vector3f(rayDirection); //= p + d * di1
	    			sphereHit1.scale(di1);
	    			sphereHit1.add(nearPlaneHit);
	    			
	    			float di2 = di1Vec.length() +(float) c;
	    			sphereHit2 = new Vector3f(rayDirection);//= p + d * di2
	    			sphereHit2.scale(di2);
	    			sphereHit2.add(nearPlaneHit);
	    			
//	    			System.out.print("hit: "+result1.toString());
//	    			System.out.println(" dist: " + di1);
//	    			System.out.print("hit: "+result2.toString());
//	    			System.out.println(" dist: " + di2);
	    			
	    			stepAlongRay();
	    			computeDerivations();
	    			
	    			//tell listners
	    			for (IRayCastChangeListner x : listners){
	    				x.rayUpdate();
	    			}
	    			
	    			return true;
	    		}
	    		//else  not piercing sphere
	    		
	    	}
	    } else {
	    	System.out.println("raycast error. porbatly matrices not inversible. try different spot");
	    }
	    return false;
	}

	/**
	 * steps along the ray and takes samples
	 */
	private void stepAlongRay(){
		float stepSize = 0.001f;
		Vector3f step = new Vector3f(rayDirection);
		step.scale(stepSize);
		Vector3f distVec = new Vector3f(sphereHit1);
		distVec.sub(sphereHit2);
		float distance = distVec.length();
		//System.out.println(sphereHit1+" : "+sphereHit2);
		//System.out.println("dist:"+distance+" step:"+step+"----------------------------");
		float traveled = 0;
		Vector3f samplePoint = new Vector3f(sphereHit1);
		rayProfile = new float[(int)((distance/stepSize)+2)];//+2 for good measure
		orginalRayProfile = new float[(int)((distance/stepSize)+2)];//+2 for good measure
		rayPositions = new Vector3f[(int)((distance/stepSize)+2)];//+2 for good measure
		int index =0;
		while (traveled < distance){
			//System.out.println(samplePoint.z+":"+cube.getData().getSampler().getValue(samplePoint.x/2+0.5f,samplePoint.y/2+0.5f,samplePoint.z/2+0.5f));
			rayProfile[index] = getRenderedOpacity(samplePoint);
			orginalRayProfile[index] = getOrginalOpacity(samplePoint);
			rayPositions[index] = new Vector3f(samplePoint);
			index++;
			samplePoint.add(step);
			traveled += stepSize;
		}
	}
	
	float getOrginalOpacity(Vector3f samplePoint){
		if(isOutOfBounds(samplePoint, -1, 1)){
			return 0;
		}
		return cube.getData().getSampler().getValue(samplePoint.x/2+0.5f,samplePoint.y/2+0.5f,samplePoint.z/2+0.5f);
	}
	
	/**
	 * computes the opacity value which is used to render at that point
	 * (to java translated shader code)
	 * @param samplePoint point in volume
	 * @return
	 */
	float getRenderedOpacity(Vector3f samplePoint){
		//comments are code from shader which is translated
//		vec4 vol = texture3D(texVolume, uv);;
//	    float opacityRaw = vol.a;
		if(isOutOfBounds(samplePoint, -1, 1)){
			return 0;
		}
		float opacityRaw = cube.getData().getSampler().getValue(samplePoint.x/2+0.5f,samplePoint.y/2+0.5f,samplePoint.z/2+0.5f);
				
//	    //Gradient at current cell in local space
//	    vec3 grad = vol.rgb * 2.0 - 1.0;
		Vector3f grad = new Vector3f();
		grad.x = cube.getData().getSampler().getDx(samplePoint.x/2+0.5f,samplePoint.y/2+0.5f,samplePoint.z/2+0.5f);
		grad.y = cube.getData().getSampler().getDy(samplePoint.x/2+0.5f,samplePoint.y/2+0.5f,samplePoint.z/2+0.5f);
		grad.z = cube.getData().getSampler().getDz(samplePoint.x/2+0.5f,samplePoint.y/2+0.5f,samplePoint.z/2+0.5f);
		
//	    float gradLen = length(grad);
		float gradLen = grad.length();
		
//	    vec4 xferCol = texture2D(texXfer, vec2(opacityRaw, gradLen * 2.0));
		BufferedImage trans = cube.getData().getTransferFunctionRef();
		int x = Math.max(0,(int)(Math.min(opacityRaw,1)*trans.getWidth())-1); //bounds check
		int y = Math.max(0,(int)((1-Math.min(gradLen * 2.0,1.0f))*trans.getHeight())-1); //bounds check and inverting
		int rgba = trans.getRGB(x,y );

		// transformation code from volume data
//        buf.put((byte)((rgba >> 16) & 0xff));
//        buf.put((byte)((rgba >> 8) & 0xff));
//        buf.put((byte)(rgba & 0xff));
//        buf.put((byte)((rgba >> 24) & 0xff));
		float xferCol_a = ((float)(((rgba >> 24) & 0xff)));
		xferCol_a /= 256.0f;
//	    float opacityLocal = xferCol.a * opacityRaw;
		float opacityLocal = xferCol_a * opacityRaw;
//	    opacityLocal = 1.0 - pow(1.0 - opacityLocal, opacityCorrect);
		opacityLocal = (float) ( 1.0f-Math.pow(1.0f -opacityLocal, cube.getOpacityCorrect()));
//		if(false){
//			System.out.println("-------------");
//			System.out.println("trans x,y: "+x+" , "+y);
//			System.out.println("oRay: "+opacityRaw+" width: "+trans.getWidth());
//			System.out.println("gradlen: "+gradLen+" height: "+trans.getHeight());
//			System.out.println("xfercol_a: "+xferCol_a);
//			System.out.println("opacitylocal: "+opacityLocal);
//		}
		return opacityLocal;
	}

	/**
	 * computes gpu- Profile, AccSum 1. and 2. Derivative
	 */
	private void computeDerivations(){
		//transforming the ray profile into an approximation of how the ray would look on the GPU/rendered Volume
		int numPlanes = cube.getNumPlanes();
		int samplesPerPlane = rayProfile.length / numPlanes;
		if (numPlanes >= rayProfile.length){//something is strange
			gpuRayProfile = rayProfile;
		}else{//expected
			gpuRayProfile = new float[numPlanes]; 
			for (int i = 0; i < gpuRayProfile.length; ++i){
				float sum = 0;
				for ( int j = 0; j < samplesPerPlane;++j){
					sum += rayProfile[(i*samplesPerPlane)+j];
				}
				sum /= samplesPerPlane; //take the mean of all samples
				gpuRayProfile[i] = sum;
			}
		}
		
		gpuAccSum = new float[gpuRayProfile.length]; //not the same as renderd visibilty. Ray cast different sample rate
		float[] gpuCappedAccSum = new float[gpuRayProfile.length];
		gpuAccSum[0] = gpuRayProfile[0];
		gpuCappedAccSum[0] = gpuRayProfile[0];
		for (int i = 1; i < gpuAccSum.length; ++i){
			gpuCappedAccSum[i] += Math.min(1, gpuAccSum[i-1] + gpuRayProfile[i]);
			gpuAccSum[i] +=gpuAccSum[i-1] + gpuRayProfile[i];
		}
		gpuFirstDerv = new float[gpuRayProfile.length];
		derive(gpuCappedAccSum,gpuFirstDerv);
		gpuSecDerv = new float[gpuFirstDerv.length-1];
		derive(gpuFirstDerv, gpuSecDerv);
	}
	
	private void derive(float[] from, float[] to){
		for (int i = 0; i < from.length -1;++i){
			to[i]=from[i+1]-from[i];
		}
	}
	
	/**
	 * checks if any value of a vector is lower than min or higher than max. if thats the case return true
	 * @param vec
	 * @param min
	 * @param max
	 * @return
	 */
	private boolean isOutOfBounds(Vector3f vec, float min, float max){
        if (vec.x < min | max < vec.x)
        	return true;
        if (vec.y < min | max < vec.y)
        	return true;
        if (vec.z < min | max < vec.z)
        	return true;
        return false;
    }
	
	
}
