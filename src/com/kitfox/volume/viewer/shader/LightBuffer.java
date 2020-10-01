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

package com.kitfox.volume.viewer.shader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Color3f;

import com.jogamp.opengl.util.GLBuffers;
import com.kitfox.volume.viewer.ViewerPanel;

/**
 *
 * @author kitfox
 */
public class LightBuffer
{
    private int frameBufAccumId;
    private int renderBufAccumId;

    private int frameBufTexId;
    private int texId;

    boolean dirty = true;

    static final int width = 512;
    static final int height = 512;

    private void checkFramebuffer(GL2 gl)
    {
        int status = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
        switch (status)
        {
            case GL2.GL_FRAMEBUFFER_COMPLETE:
                break;
            case GL2.GL_FRAMEBUFFER_UNSUPPORTED:
                throw new RuntimeException("Framebuffer unsupported");
            default:
                throw new RuntimeException("Incomplete buffer " + status);
        }
    }

    private void init(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();
        
        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(2);

        gl.glGenFramebuffers(2, ibuf);
        frameBufAccumId = ibuf.get(0);
        frameBufTexId = ibuf.get(1);

        gl.glGenTextures(1, ibuf);
        texId = ibuf.get(0);

        gl.glGenRenderbuffers(1, ibuf);
        renderBufAccumId = ibuf.get(0);

        //Create shadow accumulation buffer
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufAccumId);
        gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, renderBufAccumId);
        gl.glRenderbufferStorageMultisample(GL2.GL_RENDERBUFFER,
                4,
                GL2.GL_RGBA,
                width, height);
        gl.glFramebufferRenderbuffer(
                GL2.GL_FRAMEBUFFER,
                GL2.GL_COLOR_ATTACHMENT0,
                GL2.GL_RENDERBUFFER,
                renderBufAccumId);

        checkFramebuffer(gl);

        //Create render to texture buffer
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufTexId);

        gl.glActiveTexture(GL2.GL_TEXTURE0);
        gl.glBindTexture(GL2.GL_TEXTURE_RECTANGLE, texId);
        gl.glTexImage2D(GL2.GL_TEXTURE_RECTANGLE, 0, GL2.GL_RGBA8,
                width, height,
                0, GL2.GL_RGBA, GL2.GL_UNSIGNED_BYTE,
                null);
//        gl.glGenerateMipmapEXT(GL2.GL_TEXTURE_2D);
        gl.glFramebufferTexture2D(
                GL2.GL_FRAMEBUFFER,
                GL2.GL_COLOR_ATTACHMENT0,
                GL2.GL_TEXTURE_RECTANGLE, texId, 0);

        checkFramebuffer(gl);



        gl.glBindTexture(GL2.GL_TEXTURE_RECTANGLE, 0);
        gl.glBindRenderbuffer(GL2.GL_RENDERBUFFER, 0);
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);

    }

    public void bind(GLAutoDrawable drawable)
    {
        if (frameBufAccumId == 0)
        {
            init(drawable);
        }
        
        GL2 gl = drawable.getGL().getGL2();

//        gl.glActiveTexture(GL2.GL_TEXTURE0);
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufAccumId);

        gl.glPushAttrib(GL2.GL_VIEWPORT_BIT);
        gl.glViewport(0, 0, width, height);

        dirty = true;
    }

    public void unbind(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        gl.glPopAttrib();
        gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
    }

    public void clear(GLAutoDrawable drawable, Color3f lightColor)
    {
        bind(drawable);

        GL gl = drawable.getGL();
//        gl.glClearColor(lightColor.x, lightColor.y, lightColor.z, 1);
        gl.glClearColor(lightColor.x, lightColor.y, lightColor.z, 0);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);

        unbind(drawable);
    }

    /**
     * @return the texId
     */
    public int getTexId()
    {
        return texId;
    }

    public void flushToTexture(GLAutoDrawable drawable)
    {
        if (!dirty)
        {
            return;
        }

        GL2 gl = drawable.getGL().getGL2();
            dirty = false;

//            gl.glEnable(GL2.GL_BLEND);
//            gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ZERO);
            gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, frameBufAccumId);
            gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, frameBufTexId);
            gl.glBlitFramebuffer(0, 0, width, height,
                    0, 0, width, height,
                    GL2.GL_COLOR_BUFFER_BIT, GL2.GL_NEAREST);
//            gl.glBindFramebufferEXT(GL2.GL_FRAMEBUFFER_EXT, 0);
            gl.glBindFramebuffer(GL2.GL_READ_FRAMEBUFFER, 0);
            gl.glBindFramebuffer(GL2.GL_DRAW_FRAMEBUFFER, 0);
//            gl.glGenerateMipmapEXT(GL2.GL_TEXTURE_2D);
    }

    public void bindLightTexture(GLAutoDrawable drawable)
    {
        if (frameBufTexId == 0)
        {
            init(drawable);
        }

        GL2 gl = drawable.getGL().getGL2();

        flushToTexture(drawable);

        gl.glBindTexture(GL2.GL_TEXTURE_RECTANGLE, texId);
//        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_EDGE);
//        gl.glTexParameterf(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_EDGE);
//        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
//        gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);

    }

    public void unbindLightTexture(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();
        gl.glBindTexture(GL2.GL_TEXTURE_RECTANGLE, 0);
    }

    public void dumpLightTexture(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        gl.glActiveTexture(GL2.GL_TEXTURE0);
        bindLightTexture(drawable);

        byte[] buf = new byte[width * height * 4];
        ByteBuffer bb = GLBuffers.newDirectByteBuffer(buf.length);
        gl.glGetTexImage(GL2.GL_TEXTURE_RECTANGLE, 0, GL2.GL_RGBA,
                GL2.GL_UNSIGNED_BYTE, bb);
        bb.rewind();
        bb.get(buf);
        byte h = buf[0];

        bb.rewind();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int j = 0; j < height; ++j)
        {
            for (int i = 0; i < width; ++i)
            {
                int r = bb.get() & 0xff;
                int g = bb.get() & 0xff;
                int b = bb.get() & 0xff;
                int a = bb.get() & 0xff;

                int argb = (a << 24)
                        | (r << 16)
                        | (g << 8)
                        | (b << 0);
//                int argb = (255 << 24)
//                        | (a << 16)
//                        | (a << 8)
//                        | (a << 0);

                img.setRGB(i, height - 1 - j, argb);
            }
        }
        try {
            ImageIO.write(img, "png", new File("lightMap.png"));
        } catch (IOException ex) {
            Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        unbindLightTexture(drawable);
    }

    public static int getLightTextureWidth()
    {
        return width;
    }

    public static int getLightTextureHeight()
    {
        return height;
    }

}
