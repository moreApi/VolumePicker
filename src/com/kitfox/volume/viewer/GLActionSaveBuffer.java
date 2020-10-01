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
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author kitfox
 */
public class GLActionSaveBuffer implements GLAction
{
    final File file;
    final ViewerPanel viewer;

    public GLActionSaveBuffer(File file, ViewerPanel viewer)
    {
        this.file = file;
        this.viewer = viewer;
    }

    public void run(GLAutoDrawable drawable)
    {
        BufferedImage img = viewer.dumpBuffer(viewer);

        try {
            ImageIO.write(img, "png", file);
        } catch (IOException ex) {
            Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.err.println("Saved snapshot to " + file);
    }

}
