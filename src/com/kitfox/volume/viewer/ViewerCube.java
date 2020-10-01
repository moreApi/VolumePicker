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

import static com.kitfox.volume.JAXBHelper.asColor3f;
import static com.kitfox.volume.JAXBHelper.asVec3f;
import static com.kitfox.volume.JAXBHelper.asVectorType;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.event.ChangeEvent;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import com.jantie.volume.pick.ray.RayCaster;
import com.jogamp.opengl.util.GLBuffers;
import com.kitfox.volume.MatrixUtil;
import com.kitfox.volume.mask.SectorMask;
import com.kitfox.volume.viewer.shader.LightBuffer;
import com.kitfox.volume.viewer.shader.VolumeShader;
import com.kitfox.volume.viewer.shader.VolumeShader.PassType;
import com.kitfox.xml.schema.volumeviewer.cubestate.CubeType;
import com.kitfox.xml.schema.volumeviewer.cubestate.LightingStyleType;

/**
 *
 * @author kitfox
 */
public class ViewerCube
        implements DataChangeListener, ViewPlaneStack.SliceTracker,
        PropertyChangeListener
{
    public static enum LightingStyle
    {
        NONE(1), PHONG(1), DIFFUSE(2);

        private final int numPasses;

        LightingStyle(int numPasses)
        {
            this.numPasses = numPasses;
        }

        /**
         * @return the numPasses
         */
        public int getNumPasses() {
            return numPasses;
        }
    }
    

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private Vector3f volumeRadius;
    public static final String PROP_VOLUMERADIUS = "volumeRadius";
    //Opacity of slices should be adjusted so that they have a combined
    // opacity equivilant to the opacity of this number of slices with
    // no adjustment
    protected float opacityReference = 5;
    public static final String PROP_OPACITYREFERENCE = "opacityReference";

    protected Vector3f lightDir;  //Light direction in view space
    public static final String PROP_LIGHTDIR = "lightDir";
    protected Color3f lightColor = new Color3f(1, 1, 1);
    public static final String PROP_LIGHTCOLOR = "lightColor";
    public static final String PROP_NUMPLANES = "numPlanes";

    protected boolean drawWireframe = true;
    protected boolean drawMark = true;
    protected boolean drawRay = true;
    public static final String PROP_DRAWWIREFRAME = "drawWireframe";
    protected boolean drawLightbuffer = true;
    public static final String PROP_DRAWLIGHTBUFFER = "drawLightbuffer";
    protected boolean multisampled;
    public static final String PROP_MULTISAMPLED = "multisampled";

    protected LightingStyle lightingStyle = LightingStyle.NONE;
    public static final String PROP_LIGHTINGSTYLE = "lightingStyle";

    protected Matrix4f viewerMvMtx = new Matrix4f();
    public static final String PROP_VIEWERMVMTX = "viewerMvMtx";
    protected Matrix4f viewerProjMtx = new Matrix4f();
    public static final String PROP_VIEWERPROJMTX = "viewerProjMtx";

    protected VolumeData data = new VolumeData();
    public static final String PROP_DATA = "data";

    //Cached data calculated from the above
    Matrix4f viewerMvTMtx;
    Matrix4f lightProjMtx;
    Matrix4f lightMvMtx;
    Matrix4f lightMvpMtx;
    boolean frontToBack;
    Vector3f planeNormal;
    Vector3f lightDirCube;  //Light dir is local cube space
    Vector3f viewerDirCube;
    
    //raycasting stuff
    RayCaster rayCaster;
    boolean triggerRayCast = false;
    float xRay = 0;
    float yRay = 0;

    //Utilities for rendering
    final WireCube wire = new WireCube();
    final MarkCube mark = new MarkCube();
    final DebugDraw debugDraw = new DebugDraw();
    final ViewPlaneStack planes = new ViewPlaneStack();
    final VolumeShader shader = new VolumeShader();
    final LightBuffer lightBuffer = new LightBuffer();
    private final SectorMask sectorMask = new SectorMask();



    ArrayList<DataChangeListener> listeners = new ArrayList<DataChangeListener>();



    public ViewerCube()
    {
    	rayCaster = new RayCaster(this);
        lightDir = new Vector3f(.5f, .5f, 1);
        lightDir.normalize();
        volumeRadius = new Vector3f(1, 1, 1);

        sectorMask.addPropertyChangeListener(this);
    }

    public void load(CubeType target)
    {
        setMultisampled(target.isMultisampled());
        setDrawLightbuffer(target.isDrawLightbuffer());
        setDrawWireframe(target.isDrawWireframe());
        setLightColor(asColor3f(target.getLightColor()));
        setLightDir(asVec3f(target.getLightDir()));
        setLightingStyle(LightingStyle.valueOf(target.getLightingStyle().name()));
        setNumPlanes(target.getNumPlanes());
        setOpacityReference(target.getOpacityRef());
        setVolumeRadius(asVec3f(target.getVolumeRadius()));

        sectorMask.load(target.getSectorMask());
        data.load(target.getTransfer());
    }

    public CubeType save()
    {
        CubeType target = new CubeType();

        target.setMultisampled(multisampled);
        target.setDrawLightbuffer(drawLightbuffer);
        target.setDrawWireframe(drawWireframe);
        target.setLightColor(asVectorType(lightColor));
        target.setLightDir(asVectorType(lightDir));
        target.setLightingStyle(LightingStyleType.valueOf(lightingStyle.name()));
        target.setNumPlanes(getNumPlanes());
        target.setOpacityRef(getOpacityReference());
        target.setVolumeRadius(asVectorType(volumeRadius));

        target.setSectorMask(sectorMask.save());
        target.setTransfer(data.save());

        return target;
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        //Pass along to listeners of this component
//        propertyChangeSupport.firePropertyChange(evt);
        propertyChangeSupport.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
    }

    public void addDataChangeListener(DataChangeListener l)
    {
        listeners.add(l);
    }

    public void removeDataChangeListener(DataChangeListener l)
    {
        listeners.remove(l);
    }

    protected void fireRefresh()
    {
        ChangeEvent evt = new ChangeEvent(this);
        for (int i = 0; i < listeners.size(); ++i)
        {
            listeners.get(i).dataChanged(evt);
        }
    }

    private void clearCache()
    {
        planeNormal = null;
        viewerDirCube = null;
        lightDirCube = null;
        lightProjMtx = null;
        lightMvMtx = null;
        lightMvpMtx = null;
    }

    private void buildCache()
    {
        //Map vectors from view space to model space
        viewerMvTMtx = new Matrix4f(viewerMvMtx);
        viewerMvTMtx.transpose();

        //Determine plane direction
        planeNormal = new Vector3f(lightDir);

        //Calculations are in view space.  Viewer looks down (0, 0, -1)
        float viewDotLight = lightDir.z;
        if (viewDotLight > 0)
        {
            //Light behind viewer
            planeNormal.z += 1;  //add viewer dir (0, 0, 1)
            planeNormal.normalize();
            frontToBack = true;
        }
        else
        {
            //Light in front of viewer
            planeNormal.z -= 1;  //sub viewer dir (0, 0, 1)
            planeNormal.normalize();
            frontToBack = false;
        }

        //Map plane normal to object space
        viewerMvTMtx.transform(planeNormal);
        lightDirCube = new Vector3f(lightDir);
        viewerMvTMtx.transform(lightDirCube);
        viewerDirCube = new Vector3f(0, 0, 1);
        viewerMvTMtx.transform(viewerDirCube);

        //Setup view cameras

        //Setup light cameras
//        float volSize = volumeRadius.length();
        lightProjMtx = new Matrix4f();
        lightMvMtx = new Matrix4f();
        lightMvpMtx = new Matrix4f();
//        MatrixUtil.lookAt(lightMvMtx,
//                lightDirCube.x, lightDirCube.y, lightDirCube.z,
//                0, 0, 0,
//                0, 1, 0);
        MatrixUtil.lookAt(lightMvMtx,
                0, 0, 0,
                -lightDirCube.x, -lightDirCube.y, -lightDirCube.z,
                0, 1, 0);
        {
            float minX, maxX, minY, maxY, minZ, maxZ;
            minX = maxX = minY = maxY = minZ = maxZ = 0;
            //Project bounding box of cube into modelview space
//            Vector3f pt = new Vector3f();
            Point3f pt = new Point3f();
            for (int i = 0; i < 8; ++i)
            {
                pt.set(
                        ((i & 1) == 0) ? volumeRadius.x : -volumeRadius.x,
                        ((i & 2) == 0) ? volumeRadius.y : -volumeRadius.y,
                        ((i & 4) == 0) ? volumeRadius.z : -volumeRadius.z
                        );
                lightMvMtx.transform(pt);
                minX = Math.min(pt.x, minX);
                maxX = Math.max(pt.x, maxX);
                minY = Math.min(pt.y, minY);
                maxY = Math.max(pt.y, maxY);
                minZ = Math.min(pt.z, minZ);
                maxZ = Math.max(pt.z, maxZ);
            }
            MatrixUtil.frustumOrtho(lightProjMtx,
                    minX, maxX, minY, maxY, minZ, maxZ);
//System.err.println(String.format("Vol %f Max/min x[%f %f] y[%f %f] z[%f %f]", volSize, minX, maxX, minY, maxY, minZ, maxZ));
//            MatrixUtil.frustumOrtho(lightProjMtx,
//                    -volSize, volSize, -volSize, volSize, -volSize, volSize);
        }
        lightMvpMtx.mul(lightProjMtx, lightMvMtx);
    }

    private Vector3f getPlaneNormal()
    {
        if (planeNormal == null)
        {
            buildCache();
        }
        return planeNormal;
    }

    private Matrix4f getLightProjMtx()
    {
        if (lightProjMtx == null)
        {
            buildCache();
        }
        return lightProjMtx;
    }

    private Matrix4f getLightMvMtx()
    {
        if (lightMvMtx == null)
        {
            buildCache();
        }
        return lightMvMtx;
    }

    private Matrix4f getLightMvpMtx()
    {
        if (lightMvpMtx == null)
        {
            buildCache();
        }
        return lightMvpMtx;
    }

    FloatBuffer mtxBuf = GLBuffers.newDirectFloatBuffer(16);
    private void setupViewerCamera(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        MatrixUtil.setMatrixc(viewerProjMtx, mtxBuf);
        mtxBuf.rewind();
        gl.glLoadMatrixf(mtxBuf);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        MatrixUtil.setMatrixc(viewerMvMtx, mtxBuf);
        mtxBuf.rewind();
        gl.glLoadMatrixf(mtxBuf);
    }

    private void setupLightCamera(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        MatrixUtil.setMatrixc(getLightProjMtx(), mtxBuf);
        mtxBuf.rewind();
        gl.glLoadMatrixf(mtxBuf);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        MatrixUtil.setMatrixc(getLightMvMtx(), mtxBuf);
        mtxBuf.rewind();
        gl.glLoadMatrixf(mtxBuf);
    }

    private void setupHUDCamera(GL2 gl)
    {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void render(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        gl.glShadeModel(GL2.GL_FLAT);
//        gl.glEnable(GL2.GL_DEPTH_TEST);

        setupViewerCamera(gl);

        if (multisampled && GLExtensions.inst().isMultisampleOk())
        {
            gl.glEnable(GL2.GL_MULTISAMPLE);
//            gl.glEnable(GL2.GL_SAMPLE_ALPHA_TO_COVERAGE);
//            gl.glEnable(GL2.GL_SAMPLE_ALPHA_TO_ONE);
        }

        //do raycast
        if(triggerRayCast){
        	rayCaster.castRay(gl, xRay, yRay);
        	triggerRayCast=false;
        }
        
        //Draw bounds
        if (drawWireframe)
        {
            gl.glColor3f(1, .5f, 1);
            gl.glPushMatrix();
            gl.glScalef(volumeRadius.x, volumeRadius.y, volumeRadius.z);
            wire.render(drawable);
            gl.glPopMatrix();
        }
        
        //draw mark
        if (drawMark)
        {
            gl.glColor3f(1, 0, 1);
            gl.glPushMatrix();
            gl.glScalef(volumeRadius.x, volumeRadius.y, volumeRadius.z);

            mark.render(drawable);
            gl.glPopMatrix();
        }
        //draw debug drawings
        if (drawRay)
        {
            gl.glColor3f(1, 0, 0);
            gl.glPushMatrix();
            gl.glScalef(volumeRadius.x, volumeRadius.y, volumeRadius.z);

            debugDraw.render(drawable);
            gl.glPopMatrix();
        }
        
        if (!GLExtensions.inst().isShaderOk())
        {
            drawAlphaOnly(drawable);
        }
        else
        {
            drawShaded(drawable);
        }

        if (multisampled && GLExtensions.inst().isMultisampleOk())
        {
            gl.glDisable(GL2.GL_MULTISAMPLE);
//            gl.glDisable(GL2.GL_SAMPLE_ALPHA_TO_COVERAGE);
//            gl.glDisable(GL2.GL_SAMPLE_ALPHA_TO_ONE);
        }
    }

	private void drawAlphaOnly(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        if (data == null)
        {
            return;
        }

        gl.glEnable(GL2.GL_BLEND);
        gl.glEnable(GL2.GL_TEXTURE_3D);
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);

        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        gl.glActiveTexture(GL2.GL_TEXTURE0);
        data.bindTexture3d(drawable);

        gl.glColor3f(lightColor.x, lightColor.y, lightColor.z);

        planes.setBoxRadius(volumeRadius);

        Vector3f dir = getViewerDirCube();
        dir.negate();
        planes.setNormal(dir);
        planes.render(drawable, 1, ViewPlaneStack.NULL_SLICE_TRACKER);

        gl.glDisable(GL2.GL_TEXTURE_3D);
        gl.glDisable(GL2.GL_BLEND);
    }

    private void drawShaded(GLAutoDrawable drawable)
    {
        GL2 gl = drawable.getGL().getGL2();

        if (data == null)
        {
            return;
        }
//            {
//                gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
//                planes.setBoxRadius(volumeRadius);
//                planes.setNormal(getPlaneNormal());
//                planes.render(drawable, 1, this);
//                gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
//            }
        if (GLExtensions.inst().isShadowLightOk())
        {
            lightBuffer.clear(drawable, lightColor);
        }

        gl.glActiveTexture(GL2.GL_TEXTURE0);
        data.bindTexture3d(drawable);
        shader.setTexVolumeId(0);

        gl.glActiveTexture(GL2.GL_TEXTURE1);
        data.bindTextureXfer(drawable);
        shader.setTexXferId(1);

        if (GLExtensions.inst().isShadowLightOk())
        {
            gl.glActiveTexture(GL2.GL_TEXTURE2);
            lightBuffer.bindLightTexture(drawable);
            shader.setTexLightMapId(2);
        }

        gl.glActiveTexture(GL2.GL_TEXTURE3);
        sectorMask.bindTexture(drawable);
        shader.setTexOctantMask(3);

        shader.setOctantCenter(sectorMask.getCenter());

        shader.setLightMvp(getLightMvpMtx());
        shader.setLightColor(lightColor);
        shader.setLightDir(getLightDirCube());
        shader.setViewDir(getViewerDirCube());
        shader.setOpacityCorrect(opacityReference / planes.getNumPlanes());


        planes.setBoxRadius(volumeRadius);
        planes.setNormal(getPlaneNormal());
        planes.render(drawable, lightingStyle.getNumPasses(), this);

        gl.glActiveTexture(GL2.GL_TEXTURE0);
        

        //Display light buffer
        if (drawLightbuffer 
                && resolveLightingStyle() == LightingStyle.DIFFUSE)
        {
//            lightBuffer.dumpLightTexture(drawable);

            setupHUDCamera(gl);

            gl.glEnable(GL2.GL_BLEND);
//            gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
            gl.glBlendFunc(GL2.GL_ONE_MINUS_SRC_ALPHA, GL2.GL_ZERO);

            gl.glActiveTexture(GL2.GL_TEXTURE0);
            
            gl.glEnable(GL2.GL_TEXTURE_RECTANGLE);
            gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_REPLACE);
            lightBuffer.bindLightTexture(drawable);

            gl.glBegin(GL2.GL_QUADS);
            {
                final int w = LightBuffer.getLightTextureWidth();
                final int h = LightBuffer.getLightTextureHeight();

                gl.glTexCoord2f(0, 0); gl.glVertex2f(0, 0);
//                gl.glTexCoord2f(1, 0); gl.glVertex2f(1, 0);
//                gl.glTexCoord2f(1, 1); gl.glVertex2f(1, 1);
//                gl.glTexCoord2f(0, 1); gl.glVertex2f(0, 1);
                gl.glTexCoord2f(w, 0); gl.glVertex2f(1, 0);
                gl.glTexCoord2f(w, h); gl.glVertex2f(1, 1);
                gl.glTexCoord2f(0, h); gl.glVertex2f(0, 1);
            }
            gl.glEnd();
            
            lightBuffer.unbindLightTexture(drawable);
            gl.glDisable(GL2.GL_TEXTURE_RECTANGLE);
            gl.glDisable(GL2.GL_BLEND);
        }

    }

    private LightingStyle resolveLightingStyle()
    {
        LightingStyle style = lightingStyle;
        if (!GLExtensions.inst().isShadowLightOk()
                && style == LightingStyle.DIFFUSE)
        {
            return LightingStyle.NONE;
        }
        return style;
    }

    FloatBuffer bufferMtx = GLBuffers.newDirectFloatBuffer(16);
    public void startSlice(GLAutoDrawable drawable, int iteration)
    {
        switch (resolveLightingStyle())
        {
            case NONE:
                shader.setPassType(PassType.COLOR);
                shader.bind(drawable, frontToBack);
                break;
            case PHONG:
                shader.setPassType(PassType.PHONG);
                shader.bind(drawable, frontToBack);
                break;
            case DIFFUSE:
                GL2 gl = drawable.getGL().getGL2();
                switch (iteration)
                {
                    case 0:
                        setupViewerCamera(gl);
                        lightBuffer.flushToTexture(drawable);
                        shader.setPassType(PassType.LIGHTMAP);
                        shader.bind(drawable, frontToBack);
//                        gl.glEnable(GL2.GL_MULTISAMPLE);
                        break;
                    case 1:
                        setupLightCamera(gl);
                        lightBuffer.bind(drawable);
                        shader.setPassType(PassType.ALPHA);
                        shader.bind(drawable, true);
                        break;
                }
                break;
        }

    }

    public void endSlice(GLAutoDrawable drawable, int iteration)
    {
        switch (resolveLightingStyle())
        {
            case NONE:
            case PHONG:
                shader.unbind(drawable);
                break;
            case DIFFUSE:
                switch (iteration)
                {
                    case 0:
                        shader.unbind(drawable);
//                        gl.glDisable(GL2.GL_MULTISAMPLE);
                        break;
                    case 1:
                        lightBuffer.unbind(drawable);
                        shader.unbind(drawable);
                        break;
                }
                break;
        }
    }
    
    public void setTriggerRaycast(float xRelPos, float yRelPos){
    	triggerRayCast = true;
    	xRay = xRelPos;
    	yRay = yRelPos;
    }
    
    public Vector3f getLightDirCube()
    {
        if (lightDirCube == null)
        {
            buildCache();
        }
        return new Vector3f(lightDirCube);
    }

    public Vector3f getViewerDirCube()
    {
        if (viewerDirCube == null)
        {
            buildCache();
        }
        return new Vector3f(viewerDirCube);
    }

    /**
     * @return the volumeRadius
     */
    public Vector3f getVolumeRadius()
    {
        return new Vector3f(volumeRadius);
    }

    /**
     * @param volumeRadius the volumeRadius to set
     */
    public void setVolumeRadius(Vector3f volumeRadius)
    {
        Vector3f oldVolumeRadius = new Vector3f(this.volumeRadius);
        this.volumeRadius.set(volumeRadius);
        clearCache();
        propertyChangeSupport.firePropertyChange(PROP_VOLUMERADIUS, oldVolumeRadius, volumeRadius);
    }

    /**
     * Get the value of lightColor
     *
     * @return the value of lightColor
     */
    public Color3f getLightColor()
    {
        return new Color3f(lightColor);
    }

    /**
     * Set the value of lightColor
     *
     * @param lightColor new value of lightColor
     */
    public void setLightColor(Color3f lightColor)
    {
        Color3f oldLightColor = new Color3f(this.lightColor);
        this.lightColor.set(lightColor);
        propertyChangeSupport.firePropertyChange(PROP_LIGHTCOLOR, oldLightColor, lightColor);
    }

    /**
     * Get the value of lightDir
     *
     * @return the value of lightDir
     */
    public Vector3f getLightDir() {
        return new Vector3f(lightDir);
    }

    /**
     * Set the value of lightDir
     *
     * @param lightDir new value of lightDir
     */
    public void setLightDir(Vector3f lightDir) {
        Vector3f oldLightDir = new Vector3f(this.lightDir);
        this.lightDir.set(lightDir);
        clearCache();
        propertyChangeSupport.firePropertyChange(PROP_LIGHTDIR, oldLightDir, lightDir);
    }

    /**
     * Get the value of data
     *
     * @return the value of data
     */
    public VolumeData getData() {
        return data;
    }

    /**
     * Set the value of data
     *
     * @param data new value of data
     */
    public void setData(VolumeData data) {
        if (this.data != null)
        {
            this.data.removeDataChangeListener(this);
        }
        VolumeData oldData = this.data;
        this.data = data;
        if (this.data != null)
        {
            this.data.addDataChangeListener(this);
        }
        propertyChangeSupport.firePropertyChange(PROP_DATA, oldData, data);
    }

    public void setNumPlanes(int numPlanes)
    {
        int oldNumPlanes = planes.getNumPlanes();
        planes.setNumPlanes(numPlanes);
        propertyChangeSupport.firePropertyChange(PROP_LIGHTDIR, oldNumPlanes, numPlanes);
    }

    public int getNumPlanes()
    {
        return planes.getNumPlanes();
    }

    public void dataChanged(ChangeEvent evt)
    {
        fireRefresh();
    }

    /**
     * Get the value of lightingStyle
     *
     * @return the value of lightingStyle
     */
    public LightingStyle getLightingStyle() {
        return lightingStyle;
    }

    /**
     * Set the value of lightingStyle
     *
     * @param lightingStyle new value of lightingStyle
     */
    public void setLightingStyle(LightingStyle lightingStyle) {
        LightingStyle oldLightingStyle = this.lightingStyle;
        this.lightingStyle = lightingStyle;
        propertyChangeSupport.firePropertyChange(PROP_LIGHTINGSTYLE, oldLightingStyle, lightingStyle);
    }

    /**
     * Get the value of opacityReference
     *
     * @return the value of opacityReference
     */
    public float getOpacityReference() {
        return opacityReference;
    }

    /**
     * Set the value of opacityReference
     *
     * @param opacityReference new value of opacityReference
     */
    public void setOpacityReference(float opacityReference) {
        float oldOpacityReference = this.opacityReference;
        this.opacityReference = opacityReference;
        propertyChangeSupport.firePropertyChange(PROP_OPACITYREFERENCE, oldOpacityReference, opacityReference);
    }

    /**
     * Get the value of viewerProjMtx
     *
     * @return the value of viewerProjMtx
     */
    public Matrix4f getViewerProjMtx() {
        return new Matrix4f(viewerProjMtx);
    }

    /**
     * Set the value of viewerProjMtx
     *
     * @param viewerProjMtx new value of viewerProjMtx
     */
    public void setViewerProjMtx(Matrix4f viewerProjMtx) {
        Matrix4f oldViewerProjMtx = this.viewerProjMtx;
        this.viewerProjMtx.set(viewerProjMtx);
        clearCache();
        propertyChangeSupport.firePropertyChange(PROP_VIEWERPROJMTX, oldViewerProjMtx, viewerProjMtx);
    }

    /**
     * Get the value of viewerMvMtx
     *
     * @return the value of viewerMvMtx
     */
    public Matrix4f getViewerMvMtx() {
        return new Matrix4f(viewerMvMtx);
    }

    /**
     * Set the value of viewerMvMtx
     *
     * @param viewerMvMtx new value of viewerMvMtx
     */
    public void setViewerMvMtx(Matrix4f viewerMvMtx) {
        Matrix4f oldViewerMvMtx = this.viewerMvMtx;
        this.viewerMvMtx.set(viewerMvMtx);
        clearCache();
        propertyChangeSupport.firePropertyChange(PROP_VIEWERMVMTX, oldViewerMvMtx, viewerMvMtx);
    }

    /**
     * Get the value of drawLightbuffer
     *
     * @return the value of drawLightbuffer
     */
    public boolean isDrawLightbuffer() {
        return drawLightbuffer;
    }

    /**
     * Set the value of drawLightbuffer
     *
     * @param drawLightbuffer new value of drawLightbuffer
     */
    public void setDrawLightbuffer(boolean drawLightbuffer) {
        boolean oldDrawLightbuffer = this.drawLightbuffer;
        this.drawLightbuffer = drawLightbuffer;
        propertyChangeSupport.firePropertyChange(PROP_DRAWLIGHTBUFFER, oldDrawLightbuffer, drawLightbuffer);
    }

    /**
     * Get the value of drawWireframe
     *
     * @return the value of drawWireframe
     */
    public boolean isDrawWireframe() {
        return drawWireframe;
    }

    /**
     * Set the value of drawWireframe
     *
     * @param drawWireframe new value of drawWireframe
     */
    public void setDrawWireframe(boolean drawWireframe)
    {
        boolean oldDrawWireframe = this.drawWireframe;
        this.drawWireframe = drawWireframe;
        propertyChangeSupport.firePropertyChange(PROP_DRAWWIREFRAME, oldDrawWireframe, drawWireframe);
    }

    /**
     * @return the octantMask
     */
    public SectorMask getOctantMask()
    {
        return sectorMask;
    }

    /**
     * Get the value of multisampled
     *
     * @return the value of multisampled
     */
    public boolean isMultisampled() {
        return multisampled;
    }

    /**
     * Set the value of multisampled
     *
     * @param multisampled new value of multisampled
     */
    public void setMultisampled(boolean multisampled) {
        boolean oldMultisampled = this.multisampled;
        this.multisampled = multisampled;
        propertyChangeSupport.firePropertyChange(PROP_MULTISAMPLED, oldMultisampled, multisampled);
    }

	public MarkCube getMark() {
		return mark;
	}

	public RayCaster getRayCaster() {
		return rayCaster;
	}
	
	public float getOpacityCorrect(){
		return opacityReference / planes.getNumPlanes();
	}

	public DebugDraw getDebugDraw() {
		return debugDraw;
	}

	public boolean isDrawRay() {
		return drawRay;
	}

	public void setDrawRay(boolean drawRay) {
		this.drawRay = drawRay;
	}
	
    public boolean isDrawRaySecond() {
		return debugDraw.drawSecondRay;
	}

	public void setDrawRaySecond(boolean drawRaySecond) {
		this.debugDraw.drawSecondRay = drawRaySecond;
	}
	
	public void setSelectorMaskCenter(Vector3f center){
		Vector3f tmp = new Vector3f();
		tmp.x = (center.x +1) /2;
		tmp.y = (center.y +1) /2;
		tmp.z = (center.z +1) /2;
		
		sectorMask.setCenter(tmp);
	}

    
    

}
