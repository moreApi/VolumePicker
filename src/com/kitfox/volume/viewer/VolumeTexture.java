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

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import com.jogamp.opengl.util.GLBuffers;

/**
 *
 * @author kitfox
 */
public class VolumeTexture
{
    int xSpan;
    int ySpan;
    int zSpan;
    ByteBuffer data;

    public VolumeTexture(BufferedImage[] img)
    {
        xSpan = img[0].getWidth();
        ySpan = img[0].getHeight();
        zSpan = img.length;

        int size = xSpan * ySpan * zSpan;
        data = GLBuffers.newDirectByteBuffer(size * 4);
        System.err.println("Mem needed " + (size * 4 * 4));

//        values = new float[size];
//        valuesDx = new float[size];
//        valuesDy = new float[size];
//        valuesDz = new float[size];


        //Pull values from images
        for (int k = 0; k < zSpan; ++k)
        {
            WritableRaster raster = img[k].getRaster();
            if (raster.getWidth() != xSpan
                    || raster.getHeight() != ySpan)
            {
                throw new IllegalArgumentException("All images must be the same size");
            }

            for (int j = 0; j < ySpan; ++j)
            {
                for (int i = 0; i < xSpan; ++i)
                {
                    data.put(index(i, j, k) * 4 + 3, (byte)raster.getSample(i, j, 0));
                }
            }
        }

        for (int k = 0; k < zSpan; ++k)
        {
            for (int j = 0; j < ySpan; ++j)
            {
                for (int i = 0; i < xSpan; ++i)
                {
                    int x0 = data.get(index(i - 1, j, k) * 4 + 3) & 0xff;
                    int x1 = data.get(index(i + 1, j, k) * 4 + 3) & 0xff;
                    int y0 = data.get(index(i, j - 1, k) * 4 + 3) & 0xff;
                    int y1 = data.get(index(i, j + 1, k) * 4 + 3) & 0xff;
                    int z0 = data.get(index(i, j, k - 1) * 4 + 3) & 0xff;
                    int z1 = data.get(index(i, j, k + 1) * 4 + 3) & 0xff;

                    int idx = index(i, j, k);
                    data.put(idx * 4, (byte)((x1 - x0) / 2));
                    data.put(idx * 4 + 1, (byte)((y1 - y0) / 2));
                    data.put(idx * 4 + 2, (byte)((z1 - z0) / 2));
                }
            }
        }
    }

    private int index(int x, int y, int z)
    {
        x = Math.min(Math.max(x, 0), xSpan - 1);
        y = Math.min(Math.max(y, 0), ySpan - 1);
        z = Math.min(Math.max(z, 0), zSpan - 1);
        return (z * ySpan + y) * xSpan + x;
    }
}
