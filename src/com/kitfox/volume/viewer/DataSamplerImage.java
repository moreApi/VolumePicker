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

/**
 *
 * @author kitfox
 */
public class DataSamplerImage extends DataSampler
{
    

    public DataSamplerImage(BufferedImage[] img)
    {
        xSpan = img[0].getWidth();
        ySpan = img[0].getHeight();
        zSpan = img.length;
        
        int size = xSpan * ySpan * zSpan;
        System.err.println("Mem needed " + (size * 4 * 4));
        values = new float[size];
        valuesDx = new float[size];
        valuesDy = new float[size];
        valuesDz = new float[size];


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
                    values[index(i, j, k)]
                            = raster.getSample(i, ySpan - j - 1, 0) / 255f;
                }
            }
        }

        for (int k = 0; k < zSpan; ++k)
        {
            for (int j = 0; j < ySpan; ++j)
            {
                for (int i = 0; i < xSpan; ++i)
                {
                    int idx = (k * ySpan + j) * xSpan + i;
                    valuesDx[idx] = (sampleRaw(i + 1, j, k)
                            - sampleRaw(i - 1, j, k)) / 2;
                    valuesDy[idx] = (sampleRaw(i, j + 1, k)
                            - sampleRaw(i, j - 1, k)) / 2;
                    valuesDz[idx] = (sampleRaw(i, j, k + 1)
                            - sampleRaw(i, j, k - 1)) / 2;
                }
            }
        }
    }



}
