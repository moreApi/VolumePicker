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

package com.kitfox.volume;

import java.nio.FloatBuffer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author kitfox
 */
public class MatrixUtil
{

    public static void setMatrixr(Matrix4f m, FloatBuffer buf)
    {
        buf.put(0, m.m00); buf.put(1, m.m01); buf.put(2, m.m02); buf.put(3, m.m03);
        buf.put(4, m.m10); buf.put(5, m.m11); buf.put(6, m.m12); buf.put(7, m.m13);
        buf.put(8, m.m20); buf.put(9, m.m21); buf.put(10, m.m22); buf.put(11, m.m23);
        buf.put(12, m.m30); buf.put(13, m.m31); buf.put(14, m.m32); buf.put(15, m.m33);
    }

    /**
     * Setup matrix buffer in column major format.  This is the way GL fixed
     * function expects matrices.
     * @param m
     * @param buf
     */
    public static void setMatrixc(Matrix4f m, FloatBuffer buf)
    {
        buf.put(0, m.m00); buf.put(1, m.m10); buf.put(2, m.m20); buf.put(3, m.m30);
        buf.put(4, m.m01); buf.put(5, m.m11); buf.put(6, m.m21); buf.put(7, m.m31);
        buf.put(8, m.m02); buf.put(9, m.m12); buf.put(10, m.m22); buf.put(11, m.m32);
        buf.put(12, m.m03); buf.put(13, m.m13); buf.put(14, m.m23); buf.put(15, m.m33);
    }

    /**
     * Similar to gluPerspective()
     *
     * This creates a perspective matrix that projects from view coordinates to
     * clip coordinates.  Any point in clip space that is within the unit cube
     * (-1, -1, -1) (1, 1, 1) is within the frustum.  Otherwise, it lies outside of the frustum.
     *
     *
     * @param fovy - Angle in radians
     */
    public static Matrix4f frustumPersp(Matrix4f m, float fovy, float aspect, float near, float far)
    {
        double radians = Math.toRadians(fovy / 2);
//        double radians = fovy / 2;

        float dz = far - near;
        double sine = Math.sin(radians);

        if (dz == 0 || sine == 0 || aspect == 0)
        {
            throw new IllegalArgumentException();
        }

        double cotangent = Math.cos(radians) / sine;

        m.setIdentity();
        m.m00 = (float)(cotangent / aspect);
        m.m11 = (float)cotangent;
        m.m22 = -(far + near) / dz;
        m.m23 = -2 * near * far / dz;
        m.m32 = -1;
        m.m33 = 0;

        return m;
    }

    public static Matrix4f frustumOrtho(Matrix4f m, float left, float right, float bottom, float top, float near, float far)
    {
        if (m == null)
        {
            m = new Matrix4f();
        }

        m.m00 = 2 / (right - left);
        m.m01 = 0;
        m.m02 = 0;
        m.m03 = -(right + left) / (right - left);

        m.m10 = 0;
        m.m11 = 2 / (top - bottom);
        m.m12 = 0;
        m.m13 = -(top + bottom) / (top - bottom);

        m.m20 = 0;
        m.m21 = 0;
        m.m22 = -2 / (far - near);
        m.m23 = -(far + near) / (far - near);

        m.m30 = 0;
        m.m31 = 0;
        m.m32 = 0;
        m.m33 = 1;

        return m;
    }

    /**
     * Creates an affine transformation that maps world space to the local space of
     * a camera with eye position 'eye', up vector 'up' and is facing 'center'.
     *
     * Similar to gluLookAt() from OpenGL.
     */
    public static Matrix4f lookAt(Matrix4f m, float eyex, float eyey, float eyez, float centerx, float centery, float centerz, float upx, float upy, float upz)
    {
        Vector3f forward = new Vector3f(
                centerx - eyex,
                centery - eyey,
                centerz - eyez);
        forward.normalize();

        Vector3f up = new Vector3f(upx, upy, upz);

        Vector3f side = new Vector3f();
        side.cross(forward, up);
        side.normalize();

        up.cross(side, forward);

        m.m00 = side.x;
        m.m01 = side.y;
        m.m02 = side.z;
//        m.m03 = 0;

        m.m10 = up.x;
        m.m11 = up.y;
        m.m12 = up.z;
//        m.m13 = 0;

        m.m20 = -forward.x;
        m.m21 = -forward.y;
        m.m22 = -forward.z;
//        m.m32 = 0;

        m.m03 = -(side.x * eyex + side.y * eyey + side.z * eyez);
        m.m13 = -(up.x * eyex + up.y * eyey + up.z * eyez);
        m.m23 = forward.x * eyex + forward.y * eyey + forward.z * eyez;
        m.m33 = 1;

        return m;
    }

}
