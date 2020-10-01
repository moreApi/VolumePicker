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

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.util.GLBuffers;

/**
 * cube which marks the selection
 * @author jantie
 */
public class DebugDraw
{
	public boolean drawSecondRay = false;
	int wirePointId;
    int wireIndexId;
    Vector3f[] firstRay= {new Vector3f(),new Vector3f()};
    Vector3f[] secondRay= {new Vector3f(),new Vector3f()};
    float[] faceVerts;
    FloatBuffer frameBuffer;
    

    private FloatBuffer createWireframeVertices()
    {
    	
    	
        faceVerts = new float[]{
        		firstRay[0].x,firstRay[0].y,firstRay[0].z,
        		firstRay[1].x,firstRay[1].y,firstRay[1].z,
        		secondRay[0].x,secondRay[0].y,secondRay[0].z,
        		secondRay[1].x,secondRay[1].y,secondRay[1].z
        };

        frameBuffer = GLBuffers.newDirectFloatBuffer(faceVerts.length);
        frameBuffer.put(faceVerts);
        frameBuffer.rewind();
        return frameBuffer;
    }

    private IntBuffer createWireframeIndices()
    {
        int[] indices = new int[]{
            0, 1,
            2, 3,
        };

        IntBuffer buf = GLBuffers.newDirectIntBuffer(indices.length);
        buf.put(indices);
        buf.rewind();
        return buf;
    }

    private void initVBO(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(2);

        gl.glGenBuffers(2, ibuf);
        wireIndexId = ibuf.get(0);
        wirePointId = ibuf.get(1);

        {
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, wirePointId);
            FloatBuffer arrayBuf = createWireframeVertices();
            gl.glBufferData(GL2.GL_ARRAY_BUFFER, 
                    arrayBuf.limit() * GLBuffers.SIZEOF_FLOAT, 
                    arrayBuf, GL2.GL_STATIC_DRAW);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        }

        {
            gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, wireIndexId);
            IntBuffer arrayBuf = createWireframeIndices();
            gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, 
                    arrayBuf.limit() * GLBuffers.SIZEOF_INT, 
                    arrayBuf, GL2.GL_STATIC_DRAW);
            gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }
    
    void updateBuffer(GLAutoDrawable drawable){
    	faceVerts = new float[]{
        		firstRay[0].x,firstRay[0].y,firstRay[0].z,
        		firstRay[1].x,firstRay[1].y,firstRay[1].z,
        		secondRay[0].x,secondRay[0].y,secondRay[0].z,
        		secondRay[1].x,secondRay[1].y,secondRay[1].z
        };
    	//fill buffer with new data
        frameBuffer.rewind();
    	frameBuffer.put(faceVerts);
        frameBuffer.rewind();
        //upload buffer
        GL gl = drawable.getGL();
        {
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, wirePointId);
            gl.glBufferData(GL2.GL_ARRAY_BUFFER, 
            		frameBuffer.limit() * GLBuffers.SIZEOF_FLOAT, 
            		frameBuffer, GL2.GL_STATIC_DRAW);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        }
    }

    public void render(GLAutoDrawable drawable)
    {
        if (wireIndexId == 0)
        {
            initVBO(drawable);
        }

        GL2 gl = drawable.getGL().getGL2();
        
        updateBuffer(drawable);

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, wirePointId);
//        gl.glEnableVertexAttribArray(0);
//        gl.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);
//        gl.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 3 * BufferUtil.SIZEOF_FLOAT, 0);
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, wireIndexId);

        if (drawSecondRay){
        	gl.glDrawElements(GL2.GL_LINES, 4, GL2.GL_UNSIGNED_INT, 0);
        } else {
        	gl.glDrawElements(GL2.GL_LINES, 2, GL2.GL_UNSIGNED_INT, 0);
        }

//        gl.glDisableVertexAttribArray(0);
//        gl.glVertexAttribPointer(0, 4, GL2.GL_FLOAT, false, 0, 0);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
        
    }

    public void dispose(GLAutoDrawable drawable)
    {
        if (wirePointId == 0)
        {
            return;
        }

        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(2);
        ibuf.put(0, wireIndexId);
        ibuf.put(1, wirePointId);

        GL gl = drawable.getGL();
        gl.glDeleteBuffers(2, ibuf);

        wireIndexId = wirePointId = 0;
    }

	public Vector3f[] getFirstRay() {
		return firstRay;
	}

	public void setFirstRay(Vector3f[] firstRay) {
		this.firstRay = firstRay;
	}

	public Vector3f[] getSecondRay() {
		return secondRay;
	}

	public void setSecondRay(Vector3f[] secondRay) {
		this.secondRay = secondRay;
	}
    
    
    
    
}
