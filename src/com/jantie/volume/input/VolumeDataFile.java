package com.jantie.volume.input;

import java.io.File;

/**
 * container class containing file path, bit length,depth, width, height information.
 * also stores static instances of itself
 * @author Jan
 *
 */
public class VolumeDataFile {
	
	public final static VolumeDataFile DATA_VIS_EXPL = new VolumeDataFile ("volume.raw",12,512,512,421);
	public final static VolumeDataFile HEAD_MRT = new VolumeDataFile("mrt16_angio.raw",10,416,512,112);
	public final static VolumeDataFile COLON_PHANTOM = new VolumeDataFile("colon_phantom16.raw", 12, 512, 512, 442);
	public final static VolumeDataFile BACKPACK = new VolumeDataFile("backpack16.raw", 12, 512, 512, 373);
	public final static VolumeDataFile STENT = new VolumeDataFile("stent16.raw", 12, 512, 512, 174);
	public final static VolumeDataFile COLON_PRONE = new VolumeDataFile("supine16.raw", 12, 512, 512, 426);
	public final static VolumeDataFile MOUSE = new VolumeDataFile("mouse"+File.separator+"mouse0.raw", 14, 150, 150, 276);
	
	private int bitLength;
	private int width,height,depth;
	private String file;
	
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

	public String getPath() {
		return file;
	}

	public VolumeDataFile(String path, int bitLength, int width, int height, int depth) {
		super();
		this.bitLength = bitLength;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.file = path;
	}

}
