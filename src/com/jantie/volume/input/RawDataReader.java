package com.jantie.volume.input;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.logging.Logger;

/** Responsible for handeling the raw input and transforming it in something useful
 * 
 * @author Jan
 *
 */
public class RawDataReader {
	private static final Logger LOGGER = Logger.getLogger(RawDataReader.class.getName());
	
	final static String FILE_PATH = "data"+File.separator;	

	/**
	 * takes an VolumeDataFile and converts it to a VDBC by writing it in a buffer and creating a histogram at the same time
	 * @param vdf
	 * @return
	 */
	static public VolumeDataBufferContainer convertStream(VolumeDataFile vdf){
		int dimx = vdf.getWidth();
		int dimy = vdf.getHeight();
		int dimz = vdf.getDepth();
		File file = new File(vdf.getPath());
		DataInputStream stream = null;
		int vecSize = 1;
		int size = dimx*dimy*dimz*vecSize;
		float[] histogram = new float[1<<vdf.getBitLength()];
		
		FloatBuffer buffer = FloatBuffer.allocate(size);
		
		try {
			stream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
		} catch (FileNotFoundException e) {
			LOGGER.severe("error creating input streams");
			e.printStackTrace();
		}
		
		if (16 >= vdf.getBitLength() && vdf.getBitLength() > 8 ){
			int first = 0;
			boolean firstSet = false;
			for (int z=0;z<dimz;z++){
				//System.out.println("new layer");
				for (int y=0;y<dimy;y++){
					//System.out.println();
					for (int x=0;x<dimx*2;x++){ //double width cause 2 bytes = 1 voxel
						try {
							int in =stream.readUnsignedByte();
							int out = 0;
							if (firstSet){ //have a number
								out = (int)(in<<8) + first;
								histogram[out]++;
								firstSet = false;
								buffer.put(out/(float)(1<<(vdf.getBitLength()))); //density values between 0 and 1
								//System.out.print(out + " | ");
							} else {
								firstSet = true;
								first = in;
							}
						} catch (IOException e) {
							LOGGER.severe("error reading input stream");
							e.printStackTrace();
							System.exit(1);
						}
					}
				}
			}
		} else if(8 >= vdf.getBitLength() && vdf.getBitLength() >0 ){
			for (int z=0;z<dimz*dimx*dimy;z++){
				try {
					int in =stream.readUnsignedByte();
					int out = 0;
					out = (int)(in) ;
					histogram[out]++;
					buffer.put(out/(float)(1<<(vdf.getBitLength()))); //density values between 0 and 1
					//System.out.print(out + " | ");
				} catch (IOException e) {
					LOGGER.severe("error reading input stream");
					e.printStackTrace();
					System.exit(1);
				}
			}
		} else{		
			throw new IllegalArgumentException("only 16bit voxel are supported" );
		}	
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		buffer.rewind();
		LOGGER.fine("Histogram: " + Arrays.toString(histogram));
		
		VolumeDataBufferContainer vds= new VolumeDataBufferContainer(buffer,vdf.getBitLength(),vdf.getWidth(),vdf.getHeight(),vdf.getDepth(),histogram);		
		return vds;
	}

}
