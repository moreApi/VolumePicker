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

import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 *
 * @author kitfox
 */
public class Histogram
{
    int maxValue;
    final int width;
    final int height;
    final int[] data;

    public Histogram(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.data = new int[width * height];
    }

    public void add(int x, int y)
    {
        x = Math.max(Math.min(x, width - 1), 0);
        y = Math.max(Math.min(y, height - 1), 0);

        int value = data[x + y * width] + 1;
        data[x + y * width] = value;
        maxValue = Math.max(maxValue, value);
    }

    public BufferedImage createImage(GraphicsConfiguration config)
    {
        BufferedImage img = config.createCompatibleImage(width, height);
        WritableRaster raster = img.getRaster();

        double invLog = 1 / Math.log(maxValue);
        for (int j = 0; j < height; ++j)
        {
            for (int i = 0; i < width; ++i)
            {
                float intensity = (float)data[i + j * width] / maxValue;
                float power = (float)(Math.log(intensity) * invLog);
                int lum = (int)(power * 255 + .5f);
                raster.setSample(i, height - 1 - j, 0, lum);
                raster.setSample(i, height - 1 - j, 1, lum);
                raster.setSample(i, height - 1 - j, 2, lum);
            }
        }
        return img;
    }
    
    public int getMaxValue() {
    	return maxValue;
    }
}
