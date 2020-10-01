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

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;

/**
 *
 * @author kitfox
 */
public class PolygonSet
{
    ArrayList<Polygon> polys = new ArrayList<Polygon>();
    PolygonClipper clipper = new PolygonClipper();

    class Collector implements PolygonClipper.PolygonEmitter
    {
        ArrayList<Polygon> newPolys;
        Polygon curPoly;
        ArrayList<Point3f> vtxList;

        private Collector(ArrayList<Polygon> newPolys)
        {
            this.newPolys = newPolys;
        }

        public void keepWholePolygon()
        {
            newPolys.add(curPoly);
        }

        public void startPolygon()
        {
            vtxList = new ArrayList<Point3f>();
        }

        public void emitVertex(int index)
        {
            Point3f p0 = new Point3f();
            curPoly.getPosition(index, p0);
            vtxList.add(p0);
        }

        public void emitVertexLerp(int index0, int index1, float alpha)
        {
            Point3f p0 = new Point3f();
            curPoly.getPositionLerp(index0, index1, alpha, p0);
            vtxList.add(p0);
        }

        public void endPolygon()
        {
            newPolys.add(new Polygon(vtxList));
        }

        private void setCurPoly(Polygon curPoly)
        {
            this.curPoly = curPoly;
        }
    }

    public void clear()
    {
        polys.clear();
    }

    public int size()
    {
        return polys.size();
    }

    public Polygon get(int idx)
    {
        return polys.get(idx);
    }

    public void add(Polygon poly)
    {
        polys.add(poly);
    }

    public void clip(Vector4f[] clipPlanes)
    {
        for (int i = 0; i < clipPlanes.length; ++i)
        {
            clip(clipPlanes[i]);
        }
    }

    public void clip(Vector4f clipPlanes)
    {
        ArrayList<Polygon> newPolys = new ArrayList<Polygon>();

        Collector col = new Collector(newPolys);
        for (int i = 0; i < polys.size(); ++i)
        {
            Polygon poly = polys.get(i);
            col.setCurPoly(poly);
            clipper.clip(poly, clipPlanes, .001f, col);
        }

        polys = newPolys;
    }

//    public int[] getVertCounts()
//    {
//        int[] arr = new int[polys.size()];
//        for (int i = 0; i < arr.length; ++i)
//        {
//            arr[i] = polys.get(i).getNumVerts();
//        }
//        return arr;
//    }
//
//    public int getNumVerts()
//    {
//        int num = 0;
//        for (int i = 0; i < polys.size(); ++i)
//        {
//            num += polys.get(i).getNumVerts();
//        }
//        return num;
//    }

}
