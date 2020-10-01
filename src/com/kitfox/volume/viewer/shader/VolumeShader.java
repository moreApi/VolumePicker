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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.util.GLBuffers;
import com.kitfox.volume.MatrixUtil;

/**
 * handles initialization and binding of the shader
 * @author kitfox
 */
public class VolumeShader
{
    public static enum PassType
    {
        COLOR(0), PHONG(1), LIGHTMAP(2), ALPHA(3);

        private final int id;

        PassType(int id)
        {
            this.id = id;
        }

        /**
         * @return the id
         */
        public int getId() {
            return id;
        }
    }

    private PassType passType = PassType.COLOR;

    int programId;
    int uidLightMvp;
    int uidLightDir;
    int uidLightHalfDir;
    int uidLightColor;
    int uidLightStyle;
    int uidTexOctantMask;
    int uidOctantCenter;
    int uidTexVolume;
    int uidTexXfer;
    int uidTexLightMap;
    int uidOpacityCorrect;

    private int texOctantMask;
    private Vector3f octantCenter = new Vector3f(.5f, .5f, .5f);
    private int texVolumeId;
    private int texXferId;
    private int texLightMapId;
    private Vector3f viewDir = new Vector3f(0, 0, -1);
    private Vector3f lightDir = new Vector3f();
    private Color3f lightColor = new Color3f();
    private Matrix4f lightMvp = new Matrix4f();
    private float opacityCorrect = 1;

    private String loadTextFile(URL url) throws IOException
    {
        InputStream stream = url.openStream();
        InputStreamReader sr = new InputStreamReader(stream);

        BufferedReader reader = new BufferedReader(sr);
        StringBuilder sb = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine())
        {
            sb.append(line);
            sb.append("\n");
        }

        return sb.toString();
    }

    protected String getLog(GL2 gl, int handle)
    {
        //Compilation failed
        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(1);
        //ibuf.put();
        gl.glGetObjectParameterivARB(handle, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, ibuf);
        int logLength = ibuf.get(0);

        ByteBuffer bbuf = GLBuffers.newDirectByteBuffer(logLength);
        ibuf.rewind();
        gl.glGetInfoLogARB(handle, logLength, ibuf, bbuf);

        byte[] msg = new byte[logLength];
        bbuf.get(msg);
        return new String(msg);
    }

    protected boolean isCompileValid(GL2 gl, int handle)
    {
        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(1);
        gl.glGetObjectParameterivARB(handle, GL2.GL_OBJECT_COMPILE_STATUS_ARB, ibuf);
        if (ibuf.get(0) == 0)
        {
            //Compilation failed
            String log = getLog(gl, handle);
            throw new RuntimeException(log);
        }
        return true;
    }

    private void loadShader(GL2 gl, URL url, int shaderType) throws IOException
    {
        String text = loadTextFile(url);
        int shaderId = gl.glCreateShader(shaderType);

        gl.glShaderSource(shaderId, 1, new String[]{text}, (IntBuffer)null);
        gl.glCompileShader(shaderId);

        if (!isCompileValid(gl, shaderId))
        {
            return;
        }

        gl.glAttachObjectARB(programId, shaderId);
        //Flag for deletion when program is deleted
        gl.glDeleteObjectARB(shaderId);
    }

    protected boolean isLinkValid(GL2 gl, int handle)
    {
        IntBuffer ibuf = GLBuffers.newDirectIntBuffer(1);
        gl.glGetObjectParameterivARB(handle, GL2.GL_OBJECT_LINK_STATUS_ARB, ibuf);
        if (ibuf.get(0) == 0)
        {
            //Compilation failed
            String log = getLog(gl, handle);
            throw new RuntimeException(log);
        }
        return true;
    }

    protected void init(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        try {
            programId = gl.glCreateProgram();

            loadShader(gl,
                    VolumeShader.class.getResource("volume.vert.glsl"),
                    GL2.GL_VERTEX_SHADER);

            loadShader(gl,
                    VolumeShader.class.getResource("volume.frag.glsl"),
                    GL2.GL_FRAGMENT_SHADER);


            gl.glLinkProgram(programId);

            isLinkValid(gl, programId);

            //Get uniform values
            uidLightMvp = gl.glGetUniformLocation(programId, "lightMvp");
            uidLightDir = gl.glGetUniformLocation(programId, "lightDir");
            uidLightHalfDir = gl.glGetUniformLocation(programId, "lightHalfDir");
            uidLightColor = gl.glGetUniformLocation(programId, "lightColor");
            uidLightStyle = gl.glGetUniformLocation(programId, "lightStyle");
            uidTexOctantMask = gl.glGetUniformLocation(programId, "texOctantMask");
            uidOctantCenter = gl.glGetUniformLocation(programId, "octantCenter");
            uidTexVolume = gl.glGetUniformLocation(programId, "texVolume");
            uidTexXfer = gl.glGetUniformLocation(programId, "texXfer");
            uidTexLightMap = gl.glGetUniformLocation(programId, "texLightMap");
            uidOpacityCorrect = gl.glGetUniformLocation(programId, "opacityCorrect");
        } catch (IOException ex) {
            Logger.getLogger(VolumeShader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void dispose(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();
        gl.glDeleteObjectARB(programId);
        programId = 0;
    }

    FloatBuffer mtxBuf = GLBuffers.newDirectFloatBuffer(16);
    public void bind(GLAutoDrawable drawable, boolean frontToBack)
    {
        if (programId == 0)
        {
            init(drawable);
        }

        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(GL2.GL_BLEND);
        gl.glDepthMask(false);
        if (frontToBack)
        {
            gl.glBlendFunc(GL2.GL_ONE_MINUS_DST_ALPHA, GL2.GL_ONE);
        }
        else
        {
            gl.glBlendFunc(GL2.GL_ONE, GL2.GL_ONE_MINUS_SRC_ALPHA);
        }


        gl.glUseProgram(programId);

        Vector3f lightHalf = new Vector3f(lightDir);
        lightHalf.add(viewDir);
        lightHalf.normalize();

        gl.glUniform1i(uidTexOctantMask, texOctantMask);
        gl.glUniform3f(uidOctantCenter, octantCenter.x, octantCenter.y, octantCenter.z);

        gl.glUniform1i(uidTexVolume, texVolumeId);
        gl.glUniform1i(uidTexXfer, texXferId);
        gl.glUniform1i(uidTexLightMap, texLightMapId);
        gl.glUniform1f(uidOpacityCorrect, getOpacityCorrect());

        MatrixUtil.setMatrixc(lightMvp, mtxBuf);
        mtxBuf.rewind();
        gl.glUniformMatrix4fv(uidLightMvp, 1, false, mtxBuf);
        gl.glUniform1i(uidLightStyle, passType.getId());
        gl.glUniform3f(uidLightDir, lightDir.x, lightDir.y, lightDir.z);
        gl.glUniform3f(uidLightHalfDir, lightHalf.x, lightHalf.y, lightHalf.z);
        gl.glUniform3f(uidLightColor, lightColor.x, lightColor.y, lightColor.z);
    }

    public void unbind(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        gl.glUseProgram(0);

        gl.glDepthMask(true);
        gl.glDisable(GL2.GL_BLEND);
    }

    /**
     * @return the texVolumeId
     */
    public int getTexVolumeId() {
        return texVolumeId;
    }

    /**
     * @param texVolumeId the texVolumeId to set
     */
    public void setTexVolumeId(int texVolumeId) {
        this.texVolumeId = texVolumeId;
    }

    /**
     * @return the texXferId
     */
    public int getTexXferId() {
        return texXferId;
    }

    /**
     * @param texXferId the texXferId to set
     */
    public void setTexXferId(int texXferId) {
        this.texXferId = texXferId;
    }

    /**
     * @return the lightDir
     */
    public Vector3f getLightDir()
    {
        return new Vector3f(lightDir);
    }

    /**
     * @param lightDir the lightDir to set
     */
    public void setLightDir(Vector3f lightDir)
    {
        this.lightDir.set(lightDir);
    }

    /**
     * @return the viewDir
     */
    public Vector3f getViewDir()
    {
        return new Vector3f(viewDir);
    }

    /**
     * @param viewDir the lightDir to set
     */
    public void setViewDir(Vector3f viewDir)
    {
        this.viewDir.set(viewDir);
    }

    /**
     * @return the lightColor
     */
    public Color3f getLightColor()
    {
        return new Color3f(lightColor);
    }

    /**
     * @param lightColor the lightColor to set
     */
    public void setLightColor(Color3f lightColor)
    {
        this.lightColor.set(lightColor);
    }

    /**
     * @return the lightingStyle
     */
    public PassType getPassType()
    {
        return passType;
    }

    /**
     * @param passType the lightingStyle to set
     */
    public void setPassType(PassType passType)
    {
        this.passType = passType;
    }

    /**
     * @return the opacityCorrect
     */
    public float getOpacityCorrect() {
        return opacityCorrect;
    }

    /**
     * @param opacityCorrect the opacityCorrect to set
     */
    public void setOpacityCorrect(float opacityCorrect) {
        this.opacityCorrect = opacityCorrect;
    }

    /**
     * @return the texLightMapId
     */
    public int getTexLightMapId() {
        return texLightMapId;
    }

    /**
     * @param texLightMapId the texLightMapId to set
     */
    public void setTexLightMapId(int texLightMapId) {
        this.texLightMapId = texLightMapId;
    }

    /**
     * @return the lightMvp
     */
    public Matrix4f getLightMvp() {
        return new Matrix4f(lightMvp);
    }

    /**
     * @param lightMvp the lightMvp to set
     */
    public void setLightMvp(Matrix4f lightMvp) {
        this.lightMvp.set(lightMvp);
    }

    /**
     * @return the texOctantMask
     */
    public int getTexOctantMask() {
        return texOctantMask;
    }

    /**
     * @param texOctantMask the texOctantMask to set
     */
    public void setTexOctantMask(int texOctantMask) {
        this.texOctantMask = texOctantMask;
    }

    /**
     * @return the octantCenter
     */
    public Vector3f getOctantCenter() {
        return octantCenter;
    }

    /**
     * @param octantCenter the octantCenter to set
     */
    public void setOctantCenter(Vector3f octantCenter) {
        this.octantCenter = octantCenter;
    }
}
