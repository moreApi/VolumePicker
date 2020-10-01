package com.jantie.volume.pick.ray;

import javax.vecmath.Vector3f;



public class WYSIWYPick {

	private RayPickPanel parent;
	//public RayProfileGraphCanvas graphPanel;
	
	public WYSIWYPick(RayPickPanel parent) {
		this.parent = parent;
	}
	
	public void pick(){
		float[] secDerv = parent.getRayCaster().gpuSecDerv;
		int numPlanes = parent.getRayCaster().cube.getNumPlanes();
		int samplesPerPlane = parent.getRayCaster().rayProfile.length / numPlanes;
		int startLargestSeg = 0;
		int endLargestSeg = 0;
		float largestSegJump = 0;
		int startActiveSeg = 0;
		float last = secDerv[0];
		for (int i = 1;i < secDerv.length-1;++i){
//			The criterion for the upper bounds i max is that first derv stops de-
//			creasing again. For second derv this means that it becomes zero or positive
//			after being negative.
			if (last < 0){
				if(secDerv[i] >= 0){
					//found end segment
					float jumpSize = parent.getRayCaster().gpuAccSum[i] 
							- parent.getRayCaster().gpuAccSum[startActiveSeg];
					if (jumpSize > largestSegJump){
						//found new biggest jump
						startLargestSeg = startActiveSeg;
						//startActiveSeg = 0;
						endLargestSeg = i;
						largestSegJump = jumpSize;
					}
				}
			}
//			The lower bounds i_0 of such intervals are the positions where accumulated opacity starts to grow
//			stronger, that is where second derv becomes positive after being negative or
//			zero. 
			if(last<=0 ){
				if (secDerv[i] > 0){
					startActiveSeg = i;
				}
			} 
			last = secDerv[i];
		}
		int mid = startLargestSeg*samplesPerPlane + endLargestSeg*samplesPerPlane;
		mid /= 2;
		//retransform to raycast values
		Vector3f pos = new Vector3f(parent.getRayCaster().rayPositions[mid]);
		//System.out.println(startLargestSeg+":"+endLargestSeg+"->"+mid+" -> "+parent.getRayCaster().rayPositions[mid]);
		parent.getMark().setPos(pos);
		parent.getCube().setSelectorMaskCenter(pos);
		parent.setRelMarkPos((float)(mid)/parent.getRayCaster().rayPositions.length,
				(float)((startLargestSeg+endLargestSeg)/2)/parent.getRayCaster().gpuAccSum.length);
		
	}
	
	
}
