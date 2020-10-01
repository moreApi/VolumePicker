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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;

import com.kitfox.volume.MatrixUtil;
import com.kitfox.xml.schema.volumeviewer.cubestate.NavigatorType;

/**
 *
 * @author kitfox
 */
public class ViewportNavigator
{
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    protected float viewerRadius;
    public static final String PROP_VIEWERRADIUS = "viewerRadius";
    protected float viewerYaw;
    public static final String PROP_VIEWERYAW = "viewerYaw";
    protected float viewerPitch;
    public static final String PROP_VIEWERPITCH = "viewerPitch";

    Matrix4f mvMtx;

    public void load(NavigatorType target)
    {
        setViewerRadius(target.getRadius());
        setViewerYaw(target.getYaw());
        setViewerPitch(target.getPitch());
    }

    public NavigatorType save()
    {
        NavigatorType target = new NavigatorType();

        target.setRadius(getViewerRadius());
        target.setPitch(getViewerPitch());
        target.setYaw(getViewerYaw());

        return target;
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }


    public Matrix4f getModelViewMtx()
    {
        if (mvMtx == null)
        {
            //Yaw transform
            Point3f viewerPos = new Point3f(
                    (float)Math.sin(Math.toRadians(viewerYaw)),
                    0,
                    (float)Math.cos(Math.toRadians(viewerYaw)));

            //Add pitch
            AxisAngle4f axis = new AxisAngle4f(
                    -viewerPos.z, 0, viewerPos.x, (float)Math.toRadians(viewerPitch));
            Matrix4f rot = new Matrix4f();
            rot.set(axis);
            rot.transform(viewerPos);

            //Set back by radius
            viewerPos.scale(viewerRadius);

            mvMtx = new Matrix4f();
            MatrixUtil.lookAt(mvMtx,
                    viewerPos.x, viewerPos.y, viewerPos.z,
                    0, 0, 0,
                    0, 1, 0);
        }
        return new Matrix4f(mvMtx);
    }

    /**
     * Get the value of viewerPitch
     *
     * @return the value of viewerPitch
     */
    public float getViewerPitch() {
        return viewerPitch;
    }

    /**
     * Set the value of viewerPitch
     *
     * @param viewerPitch new value of viewerPitch
     */
    public void setViewerPitch(float viewerPitch) {
        float oldViewerPitch = this.viewerPitch;
        this.viewerPitch = viewerPitch;
        mvMtx = null;
        propertyChangeSupport.firePropertyChange(PROP_VIEWERPITCH, oldViewerPitch, viewerPitch);
    }

    /**
     * Get the value of viewerYaw
     *
     * @return the value of viewerYaw
     */
    public float getViewerYaw() {
        return viewerYaw;
    }

    /**
     * Set the value of viewerYaw
     *
     * @param viewerYaw new value of viewerYaw
     */
    public void setViewerYaw(float viewerYaw) {
        float oldViewerYaw = this.viewerYaw;
        this.viewerYaw = viewerYaw;
        mvMtx = null;
        propertyChangeSupport.firePropertyChange(PROP_VIEWERYAW, oldViewerYaw, viewerYaw);
    }

    /**
     * Get the value of viewerRadius
     *
     * @return the value of viewerRadius
     */
    public float getViewerRadius() {
        return viewerRadius;
    }

    /**
     * Set the value of viewerRadius
     *
     * @param viewerRadius new value of viewerRadius
     */
    public void setViewerRadius(float viewerRadius) {
        float oldViewerRadius = this.viewerRadius;
        this.viewerRadius = viewerRadius;
        mvMtx = null;
        propertyChangeSupport.firePropertyChange(PROP_VIEWERRADIUS, oldViewerRadius, viewerRadius);
    }
}
