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
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

/**
 *
 * @author kitfox
 */
public class ZipDataLoader
{

    public ZipDataLoader()
    {
    }

//    public static VolumeTexture createSampler(URL url) throws IOException
//    {
//        return createSampler(url.openStream());
//    }
//
//    public static VolumeTexture createSampler(InputStream is) throws IOException
//    {
//        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(is));
//
//        String[] suffixes = ImageIO.getReaderFileSuffixes();
//
//        byte[] buffer = new byte[2048];
//
//        ArrayList<BufferedImage> imageList = new ArrayList<BufferedImage>();
//
//        for (ZipEntry entry = zin.getNextEntry();
//            entry != null;
//            entry = zin.getNextEntry())
//        {
//            if (!endsWith(entry.getName(), suffixes))
//            {
//                continue;
//            }
//
//            ByteArrayOutputStream bout = new ByteArrayOutputStream();
//            int len = 0;
//            while ((len = zin.read(buffer)) > 0)
//            {
//                bout.write(buffer, 0, len);
//            }
//            bout.close();
//
//            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
//            imageList.add(ImageIO.read(bin));
//        }
//
//
//        return imageList.isEmpty()
//                ? null
//                : new VolumeTexture(imageList.toArray(new BufferedImage[imageList.size()]));
//    }

    public static DataSamplerImage createSampler(URL url) throws IOException
    {
        return createSampler(url.openStream());
    }

    public static DataSamplerImage createSampler(InputStream is) throws IOException
    {
        ZipInputStream zin = new ZipInputStream(new BufferedInputStream(is));

        String[] suffixes = ImageIO.getReaderFileSuffixes();

        byte[] buffer = new byte[2048];

        LinkedList<BufferedImage> imageList = new LinkedList<BufferedImage>();

        for (ZipEntry entry = zin.getNextEntry();
            entry != null;
            entry = zin.getNextEntry())
        {
//System.err.println("Parse entry " + entry.getName());
            if (!endsWith(entry.getName(), suffixes))
            {
                continue;
            }

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            int len = 0;
            while ((len = zin.read(buffer)) > 0)
            {
                bout.write(buffer, 0, len);
            }
            bout.close();

            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            imageList.addFirst(ImageIO.read(bin));
        }

//System.err.println("Num entries " + imageList.size());
        return imageList.isEmpty()
                ? null
                : new DataSamplerImage(imageList.toArray(new BufferedImage[imageList.size()]));
    }


    private static boolean endsWith(String name, String[] suffixes)
    {
        for (int i = 0; i < suffixes.length; ++i)
        {
            if (name.endsWith("." + suffixes[i]))
            {
                return true;
            }
        }
        return false;
    }
}
