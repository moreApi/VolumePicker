/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.kitfox.volume;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.vecmath.Color3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import com.kitfox.xml.schema.volumeviewer.cubestate.VectorType;

/**
 *
 * @author kitfox
 */
public class JAXBHelper
{
    public static Vector3f asVec3f(VectorType vec)
    {
        return new Vector3f(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Color3f asColor3f(VectorType vec)
    {
        return new Color3f(vec.getX(), vec.getY(), vec.getZ());
    }

    public static VectorType asVectorType(Tuple3f tuple)
    {
        VectorType vt = new VectorType();

        vt.setX(tuple.x);
        vt.setY(tuple.y);
        vt.setZ(tuple.z);

        return vt;
    }

    public static byte[] imageToBytes(BufferedImage img, String format)
    {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            GZIPOutputStream zout = new GZIPOutputStream(bout);
            BufferedOutputStream buout = new BufferedOutputStream(zout);
            ImageIO.write(img, format, buout);
            buout.close();

            return bout.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(JAXBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static BufferedImage bytesToImage(byte[] bytes)
    {
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
            GZIPInputStream zin = new GZIPInputStream(bin);
            return ImageIO.read(zin);
        } catch (IOException ex) {
            Logger.getLogger(JAXBHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
