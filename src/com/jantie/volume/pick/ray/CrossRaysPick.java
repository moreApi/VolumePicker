package com.jantie.volume.pick.ray;

import java.util.Vector;

import javax.vecmath.Vector3f;

import com.jogamp.opengl.math.VectorUtil;

/**
 * picking viva the intersection (or closest point) between two rays
 * @author Jan
 *
 */
public class CrossRaysPick {

	private RayPickPanel parent;
	private Vector3f[] firstRay = null;
	
	
	public CrossRaysPick(RayPickPanel parent) {
		this.parent = parent;
	}

	public void freshRay(){
		Vector3f[] rayPositions = parent.getRayCaster().rayPositions;
		if (firstRay == null){
			firstRay = new Vector3f[2];
		} else {
			Vector3f[] secondRay = new Vector3f[2];
			secondRay[0] = new Vector3f(rayPositions[0]);
			secondRay[1] = new Vector3f(rayPositions[rayPositions.length-2]);
			
			//let the rays be drawn
			parent.getDebugDraw().setFirstRay(firstRay.clone());
			parent.getDebugDraw().setSecondRay(secondRay.clone());
			
			Vector3f U1 = new Vector3f(firstRay[0]);
			U1.sub(firstRay[1]);
			U1.normalize();
			Vector3f U2 = new Vector3f(secondRay[0]);
			U2.sub(secondRay[1]);
			U2.normalize();
			
			//http://objectmix.com/graphics/133793-coordinates-closest-points-pair-skew-lines.html
			// P21 = P2 - P1
			Vector3f P21 = new Vector3f(firstRay[0]);
			P21.sub(secondRay[0]);
			//M = U2 x U1
			Vector3f M = new Vector3f();
			M.cross(U2, U1);
			//m2 = M · M ; · mean Dot product
			float m = M.dot(M);
			//R = P21 x M/m2
			Vector3f R = new Vector3f();
			M.scale(1/m);
			R.cross(P21, M);
			
			//t1 = R · U2 => Q1 = P1 + t1 * U1
			float t1 = R.dot(U2);
			//t2 = R · U1 => Q2 = P2 + t2 * U2
			float t2 = R.dot(U1);
			
			Vector3f S1 = new Vector3f(U1);
			S1.scale(-t1);
			S1.add(firstRay[0]);
			
			Vector3f S2 = new Vector3f(U2);
			S2.scale(-t2);
			S2.add(secondRay[0]);
			
			//get mean point
			S1.add(S2);
			S1.scale(0.5f);
			
			//calculate closest point on ray to mark
			float distance;
			int closest = 0;
			Vector3f diff = new Vector3f(S1);
			diff.sub(rayPositions[0]);
			distance = diff.length();
			for (int i = 1; i < rayPositions.length-2; ++i){
				diff = new Vector3f(S1);
				diff.sub(rayPositions[i]);
				if (distance > diff.length()){
					closest = i;
					distance = diff.length();
				}
				if(i%100 == 1){
					System.out.println(i+": "+S1 +" : "+rayPositions[i]+" -> "+diff+" : "+diff.length()+" -> "+distance);
				}
			}
			parent.setRelMarkPos((float)(closest)/rayPositions.length, (float)(closest)/rayPositions.length);
			parent.getMark().setPos(S1);
			parent.getCube().setSelectorMaskCenter(S1);
		}
		//save new ray as first ray for next pick
		firstRay[0] = new Vector3f(rayPositions[0]);
		firstRay[1] = new Vector3f(rayPositions[rayPositions.length-2]);
	}
}
