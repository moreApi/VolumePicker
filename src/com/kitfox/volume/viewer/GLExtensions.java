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

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author kitfox
 */
public class GLExtensions
{
    static GLExtensions instance;
    
    private final boolean shaderOk;
    private final boolean multisampleOk;
    private final boolean shadowLightOk;

    private GLExtensions(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        String ext = gl.glGetString(GL.GL_EXTENSIONS);

        multisampleOk = ext.contains("GL_ARB_multisample");
        shadowLightOk = ext.contains("GL_ARB_framebuffer_object")
                && ext.contains("GL_ARB_texture_rectangle");
        shaderOk = ext.contains("GL_ARB_vertex_program")
                && ext.contains("GL_ARB_vertex_shader")
                && ext.contains("GL_ARB_fragment_program")
                && ext.contains("GL_ARB_fragment_shader")
                && ext.contains("GL_ARB_multitexture");

        /*
         GL_ARB_multisample
         GL_ARB_framebuffer_object
         GL_ARB_texture_rectangle
         GL_ARB_vertex_buffer_object
         GL_ARB_vertex_program
         GL_ARB_fragment_program
         GL_ARB_fragment_shader
         GL_ARB_shader_objects
         GL_ARB_multitexture
         */
        
//       System.err.println("Gl version" + gl.glGetString(GL.GL_VERSION));
    }

    public static void update(GLAutoDrawable drawable)
    {
        instance = new GLExtensions(drawable);

    }

    public static GLExtensions inst()
    {
        return instance;
    }

    /**
     * @return the shaderOk
     */
    public boolean isShaderOk() {
        return shaderOk;
    }

    /**
     * @return the multisampleOk
     */
    public boolean isMultisampleOk() {
        return multisampleOk;
    }

    /**
     * @return the shadowLightOk
     */
    public boolean isShadowLightOk() {
        return shadowLightOk;
    }
}
