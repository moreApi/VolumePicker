/*
 * Volume Viewer - Display and manipulate 3D volumetric data
 * Copyright © 2009, Mark McKay
 * http://www.kitfox.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kitfox.volume.viewer;

import static com.kitfox.volume.JAXBHelper.bytesToImage;
import static com.kitfox.volume.JAXBHelper.imageToBytes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.event.ChangeEvent;

import com.jogamp.opengl.util.GLBuffers;
import com.kitfox.xml.schema.volumeviewer.cubestate.TransferType;

/**
 * Handles volume data holds sampler and byte buffer
 * @author kitfox
 */
public class VolumeData
{
    static class EmptyDataSampler extends DataSampler
    {

        @Override
        public float getValue(float x, float y, float z) {
            return 1;
        }

        @Override
        public float getDx(float x, float y, float z) {
            return 0;
        }

        @Override
        public float getDy(float x, float y, float z) {
            return 0;
        }

        @Override
        public float getDz(float x, float y, float z) {
            return 0;
        }
    };

    final int xSpan;
    final int ySpan;
    final int zSpan;

    final int numRoughSamples;

    ByteBuffer data;
    private final Histogram hist;
    private BufferedImage transferFunction;

    int textureId3d;
    int textureIdXfer;
    boolean textureDirty3d = true;
    boolean textureDirtyXfer = true;

    ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();
    
    DataSampler sampler;

    public VolumeData()
    {
        this(1, 1, 1, new EmptyDataSampler());
    }

    public VolumeData(int xSpan, int ySpan, int zSpan, DataSampler sampler)
    {
        this(xSpan, ySpan, zSpan, 128, sampler);
    }

    public VolumeData(int xSpan, int ySpan, int zSpan, int numRoughSamples, DataSampler sampler)
    {
    	this.sampler = sampler;
        this.xSpan = roundUpPow2(xSpan);
        this.ySpan = roundUpPow2(ySpan);
        this.zSpan = roundUpPow2(zSpan);
        this.numRoughSamples = numRoughSamples;

        buildData(sampler);
        hist = sampler.createHistogram(numRoughSamples, xSpan, ySpan, zSpan, sampler);

        transferFunction = createXferImage();
        Graphics2D g = transferFunction.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, hist.width, hist.height);
        g.dispose();
    }

    public void load(TransferType target)
    {
        setTransferFunction(bytesToImage(target.getTransferFunction()));
    }

    public TransferType save()
    {
        TransferType target = new TransferType();

        target.setTransferFunction(imageToBytes(transferFunction, "png"));

        return target;
    }


    public void addDataChangeListener(DataChangeListener l)
    {
        listeners.add(l);
    }

    public void removeDataChangeListener(DataChangeListener l)
    {
        listeners.remove(l);
    }

    protected void fireDataChange()
    {
        ChangeEvent evt = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); ++i)
        {
            listeners.get(i).dataChanged(evt);
        }
    }

    private BufferedImage createXferImage()
    {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(hist.width, hist.height, Transparency.TRANSLUCENT);
    }

    private void buildData(DataSampler sampler)
    {
        int length = xSpan * ySpan * zSpan * 4;
        data = GLBuffers.newDirectByteBuffer(length);

        //Random rand = new Random();
        for (int k = 0; k < zSpan; ++k)
        {
            float dk = (float)k / zSpan;
            for (int j = 0; j < ySpan; ++j)
            {
                float dj = (float)j / ySpan;
                for (int i = 0; i < xSpan; ++i)
                {
                    float di = (float)i / xSpan;
					//Debug - just echo input image
					//data.put(toByte(sampler.getValue(di, dj, dk)));
					//data.put(toByte(sampler.getValue(di, dj, dk)));
					//data.put(toByte(sampler.getValue(di, dj, dk)));
					////data.put(toByte(sampler.getValue(di, dj, dk)));
					//data.put((byte)255);

                    data.put(toByte(sampler.getDx(di, dj, dk) / 2 + .5f));
                    data.put(toByte(sampler.getDy(di, dj, dk) / 2 + .5f));
                    data.put(toByte(sampler.getDz(di, dj, dk) / 2 + .5f));
                    data.put(toByte(sampler.getValue(di, dj, dk)));
                }
            }
        }
        data.rewind();
    }


    private byte toByte(float value)
    {
        return (byte)(Math.min(Math.max(value * 255 + .5f, 0), 255));
    }

    private int roundUpPow2(int value)
    {
        int bits = value - 1;
        int bitCount = 0;
        while (bits != 0)
        {
            ++bitCount;
            bits >>= 1;
        }
        return 1 << bitCount;
    }

    private void initTexture(GL gl)
    {
        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(2);
        gl.glGenTextures(2, ibuf);
        textureId3d = ibuf.get(0);
        textureIdXfer = ibuf.get(1);
    }

    private void loadTexture3d(GL2 gl)
    {
        if (textureId3d == 0)
        {
            initTexture(gl);
        }

        gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);

        gl.glBindTexture(GL2.GL_TEXTURE_3D, textureId3d);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_GENERATE_MIPMAP, GL2.GL_TRUE);

        gl.glTexImage3D(GL2.GL_TEXTURE_3D, 0, GL2.GL_RGBA,
                xSpan, ySpan, zSpan,
                0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE,
                data);

        textureDirty3d = false;
    }

    public void bindTexture3d(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        if (textureDirty3d)
        {
            loadTexture3d(gl);
        }

        gl.glBindTexture(GL2.GL_TEXTURE_3D, textureId3d);
    }

    private void loadTextureXfer(GL gl)
    {
        if (textureIdXfer == 0)
        {
            initTexture(gl);
        }

        int width = transferFunction.getWidth();
        int height = transferFunction.getHeight();
        ByteBuffer buf = GLBuffers.newDirectByteBuffer(width * height * 4);
        for (int j = 0; j < height; ++j)
        {
            for (int i = 0; i < width; ++i)
            {
                int rgba = transferFunction.getRGB(i, height - j - 1);

                buf.put((byte)((rgba >> 16) & 0xff));
                buf.put((byte)((rgba >> 8) & 0xff));
                buf.put((byte)(rgba & 0xff));
                buf.put((byte)((rgba >> 24) & 0xff));
            }
        }
        buf.rewind();

        gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);

        gl.glBindTexture(GL2.GL_TEXTURE_2D, textureIdXfer);
//        gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_TEXTURE_WRAP_R, GL2.GL_REPEAT);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_GENERATE_MIPMAP, GL2.GL_TRUE);


        gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGBA,
                width, height,
                0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE,
                buf);

        textureDirtyXfer = false;
    }

    public void bindTextureXfer(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        if (textureDirtyXfer)
        {
            loadTextureXfer(gl);
        }

        gl.glBindTexture(GL2.GL_TEXTURE_2D, textureIdXfer);
    }

    public void dispose(GLAutoDrawable drawable)
    {
        if (textureId3d == 0)
        {
            return;
        }

        GL gl = drawable.getGL();

        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(2);
        ibuf.put(0, textureId3d);
        ibuf.put(1, textureIdXfer);
        gl.glDeleteTextures(2, ibuf);
        textureId3d = 0;
        textureIdXfer = 0;
    }

    /**
     * @return the hist
     */
    public Histogram getHist() {
        return hist;
    }

    /**
     * @return the transferFunction
     */
    public BufferedImage getTransferFunction()
    {
        BufferedImage ret = createXferImage();

        for (int j = 0; j < transferFunction.getHeight(); ++j)
        {
            for (int i = 0; i < transferFunction.getWidth(); ++i)
            {
                int rgba = transferFunction.getRGB(i, j);
                ret.setRGB(i, j, rgba);
            }
        }

        return ret;
    }

    /**
     * @param xferFunction the xferFunction to set
     */
    public void setTransferFunction(BufferedImage xferFunction)
    {
        for (int j = 0; j < transferFunction.getHeight(); ++j)
        {
            for (int i = 0; i < transferFunction.getWidth(); ++i)
            {
                int rgba = xferFunction.getRGB(i, j);
                transferFunction.setRGB(i, j, rgba);
            }
        }

        textureDirtyXfer = true;
        fireDataChange();
//        transferFunction.copyData(xferFunction.getRaster());

//        Graphics2D g = transferFunction.createGraphics();
//        g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
//        g.fillRect(0, 0, transferFunction.getWidth(), transferFunction.getHeight());
//
//        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
//        g.drawImage(xferFunction, 0, 0, transferFunction.getWidth(), transferFunction.getHeight(), null);
//        g.dispose();
    }

    public void getTransferFunction(BufferedImage xferImg)
    {
        for (int j = 0; j < transferFunction.getHeight(); ++j)
        {
            for (int i = 0; i < transferFunction.getWidth(); ++i)
            {
                int rgba = transferFunction.getRGB(i, j);
                xferImg.setRGB(i, j, rgba);
            }
        }

//        Graphics2D g = xferImg.createGraphics();
//        g.drawImage(transferFunction, 0, 0, xferImg.getWidth(), xferImg.getHeight(), null);
//        g.dispose();
    }
    
    public BufferedImage getTransferFunctionRef(){
    	return transferFunction;
    }

	public DataSampler getSampler() {
		return sampler;
	}
    
}
