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

import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;

/**
 *
 * @author kitfox
 */
public class PolygonClipper
{
    public static interface PolygonSource
    {
        public int getNumVerts();
        public void getPosition(int index, Point3f pt);
    }

    public static interface PolygonEmitter
    {
        public void keepWholePolygon();
        public void startPolygon();

        //Emit a vertex which is a copy of the given input vertex
        public void emitVertex(int index);

        //Emit a vertex which is linearly interpolated between two vertices
        public void emitVertexLerp(int index0, int index1, float alpha);
        public void endPolygon();
    }

    static enum ClipSide { INSIDE, ON, OUTSIDE };
    Point3f curPt = new Point3f();
    Point3f nextPt = new Point3f();

    public PolygonClipper()
    {
    }

    public void normalizePlane(Vector4f plane)
    {
        float mag = (float)Math.sqrt(plane.x * plane.x + plane.y * plane.y + plane.z * plane.z);
        plane.scale(1 / mag);
    }

    private float distToPlane(Vector4f plane, Point3f pt)
    {
        float projAlongNormal = plane.x * pt.x + plane.y * pt.y + plane.z * pt.z;
        return projAlongNormal + plane.w;
    }

    private ClipSide getClipSide(Vector4f plane, Point3f pt, float epsilon)
    {
        float dist = distToPlane(plane, pt);
        if (dist > epsilon)
        {
            return ClipSide.INSIDE;
        }
        else if (dist < -epsilon)
        {
            return ClipSide.OUTSIDE;
        }
        return ClipSide.ON;
    }

    /**
     *
     * @param provider
     * @param plane
     * @param epsilon
     * @param emitter 
     */
    public void clip(PolygonSource provider, Vector4f plane, float epsilon, PolygonEmitter emitter)
    {
        normalizePlane(plane);
        
        boolean foundInside = false;
        boolean foundOutside = false;

        //Set to the index of a vertex that is outside of the clip plane
        int outsideIndex = -1;

        //Check to see if we need to clip
        final int numVerts = provider.getNumVerts();
        for (int i = 0; i < numVerts; ++i)
        {
            provider.getPosition(i, curPt);
            switch (getClipSide(plane, curPt, epsilon))
            {
                case INSIDE:
                {
                    foundInside = true;
                    break;
                }
                case OUTSIDE:
                {
                    foundOutside = true;
                    outsideIndex = i;
                    break;
                }
            }
        }

        if (!foundInside)
        {
            //We are completly excluded
            return;
        }

        if (!foundOutside)
        {
            //We are entirely within clip plane
            emitter.keepWholePolygon();
            return;
        }

        //Do clip

        //Find first outside vertex
        //Vertex curVert = vertices.get(outsideIndex);
        int curVert = outsideIndex;
        provider.getPosition(outsideIndex, curPt);
        ClipSide curSide = ClipSide.OUTSIDE;
        boolean drawingShape = false;

        for (int i = 0; i < numVerts; ++i)
        {
            int nextVert = (i + outsideIndex + 1) % numVerts;
            provider.getPosition(nextVert, nextPt);
            ClipSide nextSide = getClipSide(plane, nextPt, epsilon);

            if (!drawingShape)
            {
                if (nextSide == ClipSide.INSIDE)
                {
                    emitter.startPolygon();
                    drawingShape = true;

                    if (curSide == ClipSide.ON)
                    {
                        emitter.emitVertex(curVert);
                    }
                    else
                    {
                        float curDist = distToPlane(plane, curPt);
                        float nextDist = distToPlane(plane, nextPt);
                        float delta = nextDist - curDist;

                        emitter.emitVertexLerp(curVert, nextVert, -curDist / delta);
                    }
                }
            }
            else
            {
                emitter.emitVertex(curVert);

                if (nextSide == ClipSide.ON)
                {
                    emitter.emitVertex(curVert);
                    emitter.endPolygon();
                    drawingShape = false;
                }
                else if (nextSide == ClipSide.OUTSIDE)
                {
                    //Finish shape
                    float curDist = distToPlane(plane, curPt);
                    float nextDist = distToPlane(plane, nextPt);
                    float delta = nextDist - curDist;

                    emitter.emitVertexLerp(curVert, nextVert, -curDist / delta);
                    emitter.endPolygon();
                    drawingShape = false;
                }
            }

            //Next vert
            curSide = nextSide;
            curVert = nextVert;
            curPt.set(nextPt);
        }
    }
}
