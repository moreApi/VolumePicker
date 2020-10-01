/*
 * ViewerPanel.java
 * Created on Dec 23, 2009, 12:17:55 PM
 *
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

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.vecmath.Matrix4f;

import com.jogamp.opengl.util.GLBuffers;
import com.kitfox.volume.MatrixUtil;
import com.kitfox.xml.schema.volumeviewer.cubestate.NavigatorType;

/**
 *
 * @author kitfox
 */
public class ViewerPanel extends GLCanvas
        implements GLEventListener, PropertyChangeListener, DataChangeListener
{
    private static final long serialVersionUID = 0;
    
    private float maxUserRadius = 10;
    private float minUserRadius = 3;
    private float cameraAngularVel = 1f; //Degress per pixel
    private float cameraZoomVel = .4f;
    private float fovY = 30;
    private float aspect = 1;
    private float nearPlane = .25f;
    private float farPlane = 1000;

    private ViewerCube cube;

    final ViewportNavigator nav = new ViewportNavigator();

    ArrayList<GLAction> actionQueue = new ArrayList<GLAction>();

    /** Creates new form ViewerPanel */
    public ViewerPanel()
    {
        super(getCapabilities());

        nav.setViewerPitch(0);
        nav.setViewerRadius(7);
//        nav.setViewerYaw(90);
        nav.setViewerYaw(0);

        addGLEventListener(this);
        nav.addPropertyChangeListener(this);

        initComponents();
    }

    public void postGLAction(GLAction action)
    {
        actionQueue.add(action);
    }

    public NavigatorType save()
    {
        return nav.save();
    }

    public void load(NavigatorType target)
    {
        nav.load(target);
    }

    private static GLCapabilities getCapabilities()
    {
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities cap = new GLCapabilities(glprofile);

        cap.setAlphaBits(8);
        cap.setHardwareAccelerated(true);
        cap.setSampleBuffers(true);
        cap.setNumSamples(8);

        return cap;
    }

    public void init(GLAutoDrawable drawable)
    {
        //Debug
        drawable.setGL(drawable.getGL());

        GLExtensions.update(drawable);
        checkExtensions();

        GL2 gl = drawable.getGL().getGL2();
        System.err.println("Initializing GL Thread");
        System.err.println(gl.glGetString(GL.GL_EXTENSIONS));
        System.err.println("Gl version" + gl.glGetString(GL.GL_VERSION));

        // Enable VSync
        gl.setSwapInterval(1);

        gl.glShadeModel(GL2.GL_FLAT);

    }

    float[] colArr = new float[3];

    public void display(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        //Run any posted events
        if (!actionQueue.isEmpty())
        {
            for (int i = 0; i < actionQueue.size(); ++i)
            {
                actionQueue.get(i).run(drawable);
            }
            actionQueue.clear();
        }

//        {
//            IntBuffer ibuf = BufferUtil.newIntBuffer(1);
////            gl.glGetIntegerv(GL.GL_ALPHA_BITS, ibuf);
////            int alphaBits = ibuf.get(0);
////            alphaBits += 0;
//            ibuf.rewind();
//            gl.glGetIntegerv(GL.GL_SAMPLE_BUFFERS, ibuf);
//            System.out.println("number of sample buffers is " + ibuf.get(0));
//            ibuf.rewind();
//            gl.glGetIntegerv(GL.GL_SAMPLES, ibuf);
//            System.out.println("number of samples is " + ibuf.get(0));
//        }

        {
            //Clear
            Color bg = getBackground();
            bg.getColorComponents(colArr);
//            gl.glClearColor(colArr[0], colArr[1], colArr[2], 0.0f);
            gl.glClearColor(0, 0, 0, 0.0f);
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
//            gl.glClear(GL.GL_COLOR_BUFFER_BIT);


            //Draw view cube
            ViewerCube curCube = cube;
            if (curCube != null)
            {
                curCube.setViewerMvMtx(nav.getModelViewMtx());
                curCube.render(drawable);
            }

        }

    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        GL gl = drawable.getGL();

        gl.glViewport(x, y, width, height);

        //Calc projection matrix
        Matrix4f proj = new Matrix4f();
        if (height <= 0)
        {
            height = 1;
        }
        final float scrnAspect = (float)width / height;
        MatrixUtil.frustumPersp(proj, fovY, scrnAspect * aspect, nearPlane, farPlane);
        cube.setViewerProjMtx(proj);

//        System.err.println("reshape");
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
    {
    }

    @Override
    public void dispose(GLAutoDrawable glad)
    {
    }

    public BufferedImage dumpBuffer(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();

        int width = getWidth();
        int height = getHeight();
        ByteBuffer bb = GLBuffers.newDirectByteBuffer(width * height * 4);
        gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, bb);

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

                img.setRGB(i, height - 1 - j, argb);
            }
        }
        return img;
    }

    private void checkExtensions()
    {
        boolean ms = GLExtensions.inst().isMultisampleOk();
        boolean sh = GLExtensions.inst().isShaderOk();
        boolean sl = GLExtensions.inst().isShadowLightOk();

        if (ms && sh && sl)
        {
            //All ok
            return;
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        if (!ms)
        {
            pw.println("Graphics card does not support multisampling.  Feature disabled.");
        }
        if (!sh)
        {
            pw.println("Graphics card does not support shaders.  Feature disabled.");
        }
        if (!sl)
        {
            pw.println("Graphics card does not support frame buffer objects.  Feature disabled.");
        }
        pw.close();

        JOptionPane.showMessageDialog(this, sw.toString(), "Some features could not be created", JOptionPane.WARNING_MESSAGE);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                formMouseWheelMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    MouseEvent oldMouse;
    float startYaw;
    float startPitch;
    //cube

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        oldMouse = evt;
        startYaw = nav.getViewerYaw();
        startPitch = nav.getViewerPitch();
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
    	if (evt.isPopupTrigger()){//popup = right click
//    		float x = (float)(evt.getX())/this.getWidth();
//    		float y = (float)(evt.getY())/this.getHeight();
//    		System.out.println("right click");
//    		System.out.println("X: " +evt.getX()+"("+x+")");
//    		System.out.println("y: " +evt.getY()+"("+y+")");
    		cube.setTriggerRaycast(evt.getX(), evt.getY());
    		this.display();
    	}
        
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        int dx = evt.getX() - oldMouse.getX();
        int dy = evt.getY() - oldMouse.getY();

        float newYaw = startYaw - dx * cameraAngularVel;
        float newPitch = startPitch + dy;
        newPitch = Math.min(Math.max(newPitch, -85), 85);

        nav.setViewerYaw(newYaw);
        nav.setViewerPitch(newPitch);
        repaint();
    }//GEN-LAST:event_formMouseDragged

    private void formMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_formMouseWheelMoved
        float newRad = nav.getViewerRadius() + evt.getWheelRotation() * cameraZoomVel;
        newRad = Math.max(Math.min(newRad, maxUserRadius), minUserRadius);

        nav.setViewerRadius(newRad);
        repaint();
    }//GEN-LAST:event_formMouseWheelMoved

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        repaint();
    }//GEN-LAST:event_formComponentShown

    /**
     * @return the maxUserRadius
     */
    public float getMaxUserRadius() {
        return maxUserRadius;
    }

    /**
     * @param maxUserRadius the maxUserRadius to set
     */
    public void setMaxUserRadius(float maxUserRadius) {
        this.maxUserRadius = maxUserRadius;
    }

    /**
     * @return the minUserRadius
     */
    public float getMinUserRadius() {
        return minUserRadius;
    }

    /**
     * @param minUserRadius the minUserRadius to set
     */
    public void setMinUserRadius(float minUserRadius) {
        this.minUserRadius = minUserRadius;
    }

    /**
     * @return the cameraAngularVel
     */
    public float getCameraAngularVel() {
        return cameraAngularVel;
    }

    /**
     * @param cameraAngularVel the cameraAngularVel to set
     */
    public void setCameraAngularVel(float cameraAngularVel) {
        this.cameraAngularVel = cameraAngularVel;
    }

    /**
     * @return the fovY
     */
    public float getFovY() {
        return fovY;
    }

    /**
     * @param fovY the fovY to set
     */
    public void setFovY(float fovY) {
        this.fovY = fovY;
    }

    /**
     * @return the aspect
     */
    public float getAspect() {
        return aspect;
    }

    /**
     * @param aspect the aspect to set
     */
    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    /**
     * @return the nearPlane
     */
    public float getNearPlane() {
        return nearPlane;
    }

    /**
     * @param nearPlane the nearPlane to set
     */
    public void setNearPlane(float nearPlane) {
        this.nearPlane = nearPlane;
    }

    /**
     * @return the farPlane
     */
    public float getFarPlane() {
        return farPlane;
    }

    /**
     * @param farPlane the farPlane to set
     */
    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
    }

    /**
     * Degress per pixel
     * @return the cameraZoomVel
     */
    public float getCameraZoomVel() {
        return cameraZoomVel;
    }

    /**
     * @param cameraZoomVel the cameraZoomVel to set
     */
    public void setCameraZoomVel(float cameraZoomVel) {
        this.cameraZoomVel = cameraZoomVel;
    }

    /**
     * @return the cube
     */
    public ViewerCube getCube()
    {
        return cube;
    }

    /**
     * @param cube the cube to set
     */
    public void setCube(ViewerCube cube)
    {
        if (this.cube != null)
        {
            this.cube.removePropertyChangeListener(this);
            this.cube.removeDataChangeListener(this);
        }
        this.cube = cube;
        if (this.cube != null)
        {
            this.cube.addPropertyChangeListener(this);
            this.cube.addDataChangeListener(this);
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource() == nav)
        {
            if (cube != null)
            {
                cube.setViewerMvMtx(nav.getModelViewMtx());
            }
        }
        repaint();
    }

    public void dataChanged(ChangeEvent evt)
    {
        repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
