/**
 * 
 */
package com.jantie.volume.input;

import java.nio.FloatBuffer;

/**
 * a container class for a Buffer of relative volume density data data and its meta data
 * @author Jan
 *
 */
public class VolumeDataBufferContainer {

	private int bitLength;
	private int width,height,depth;
	private FloatBuffer buffer;
	private float[] histogram;
	
	public int getBitLength() {
		return bitLength;
	}


	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}


	public int getDepth() {
		return depth;
	}
	
	public FloatBuffer getBuffer(){
		return buffer;
	}

	public float[] getHistogram() {
		return histogram;
	}


	/**
	 * 
	 * @param in stream of voume data
	 * @param bitLength bitlength of one texel value
	 * @param width width of the volume data
	 * @param height height of the volume data
	 * @param depth depth of the volume data
	 * @param histogram histogram over the density values of the data
	 */
	public VolumeDataBufferContainer(FloatBuffer buffer, int bitLength, int width,
			int height, int depth, float[] histogram) {
		this.bitLength = bitLength;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.buffer = buffer;
		this.histogram = histogram;
	}

}
