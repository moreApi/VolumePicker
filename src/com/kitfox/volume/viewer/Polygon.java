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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.kitfox.volume.viewer.PolygonClipper.PolygonEmitter;
import com.kitfox.volume.viewer.PolygonClipper.PolygonSource;

/**
 *
 * @author kitfox
 */
public class Polygon implements PolygonSource
{

    class Collector implements PolygonEmitter
    {
        ArrayList<Point3f> vertList = new ArrayList<Point3f>();
        boolean update;

        public void keepWholePolygon()
        {
            update = false;
        }

        public void startPolygon()
        {
        }

        public void emitVertex(int index)
        {
            vertList.add(verts.get(index));
        }

        public void emitVertexLerp(int index0, int index1, float alpha)
        {
            Point3f p0 = verts.get(index0);
            Point3f p1 = verts.get(index1);

            Point3f p = new Point3f(
                    p0.x * (1 - alpha) + p1.x * alpha,
                    p0.y * (1 - alpha) + p1.y * alpha,
                    p0.z * (1 - alpha) + p1.z * alpha
                    );
            vertList.add(p);
        }

        public void endPolygon()
        {
            update = true;
        }
    }

    ArrayList<Point3f> verts = new ArrayList<Point3f>();

    public Polygon(Point3f[] vertList)
    {
        verts.addAll(Arrays.asList(vertList));
    }

    public Polygon(List<Point3f> vertList)
    {
        verts.addAll(vertList);
    }

    public int getNumVerts()
    {
        return verts.size();
    }

    public void getPosition(int index, Point3f pt)
    {
        pt.set(verts.get(index));
    }

    public void getPositionLerp(int index0, int index1, float alpha, Point3f pt)
    {
        Point3f p0 = verts.get(index0);
        Point3f p1 = verts.get(index1);
        pt.set(
                p0.x * (1 - alpha) + p1.x * alpha,
                p0.y * (1 - alpha) + p1.y * alpha,
                p0.z * (1 - alpha) + p1.z * alpha
                );
    }

    public void clipToPlanes(float epsilon, Vector4f... planes)
    {
        PolygonClipper clip = new PolygonClipper();

        for (int i = 0; i < planes.length; ++i)
        {
            Collector col = new Collector();
            clip.clip(this, planes[i], epsilon, col);
            if (col.update)
            {
                verts = col.vertList;
            }
        }
    }

    public void appendFan(FloatBuffer arrayBuf)
    {
        for (int i = 0; i < verts.size(); ++i)
        {
            Point3f p = verts.get(i);
            arrayBuf.put(p.x);
            arrayBuf.put(p.y);
            arrayBuf.put(p.z);
        }
    }

    public void appendFanWithTexCoord(FloatBuffer arrayBuf, Vector3f boxRadius)
    {
        for (int i = 0; i < verts.size(); ++i)
        {
            Point3f p = verts.get(i);
            arrayBuf.put(p.x);
            arrayBuf.put(p.y);
            arrayBuf.put(p.z);
            arrayBuf.put((p.x / boxRadius.x + 1) * .5f);
            arrayBuf.put((p.y / boxRadius.y + 1) * .5f);
            arrayBuf.put((p.z / boxRadius.z + 1) * .5f);
        }
    }
}
