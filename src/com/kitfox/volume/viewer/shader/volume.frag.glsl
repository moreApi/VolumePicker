/*
 *
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

#extension GL_ARB_texture_rectangle:enable
//#extension GL_EXT_gpu_shader4 : enable

const int LIGHT_STYLE_COLOR = 0;
const int LIGHT_STYLE_PHONG = 1;
const int LIGHT_STYLE_LIGHTMAP = 2;
const int LIGHT_STYLE_ALPHA = 3;

const float LIGHTMAP_SIDE_LEN = 512.0;

uniform vec3 lightColor;
uniform vec3 lightDir;
uniform vec3 lightHalfDir;
uniform int lightStyle;
uniform float opacityCorrect;

uniform sampler3D texVolume;
uniform sampler2D texXfer;
uniform sampler2DRect texLightMap;

uniform sampler3D texOctantMask;
uniform vec3 octantCenter;

uniform mat4 lightMvp;

//varying vec3 posView;
varying vec3 posLocal;
varying vec3 uv;

float saturate(float value)
{
    return clamp(value, 0.0, 1.0);
}

void main()
{
    //Apply mask
    vec3 maskCoord = uv - octantCenter;
    maskCoord = (maskCoord + 1.0) * 0.5;

    if (texture3D(texOctantMask, maskCoord).a < 0.5)
    {
//        gl_FragColor = vec4(0, 0, 0, 0);
//        return;
        discard;
    }

    //Lookup density & gradient
    vec4 vol = texture3D(texVolume, uv);
    float opacityRaw = vol.a;

    //Gradient at current cell in local space
    vec3 grad = vol.rgb * 2.0 - 1.0;
    float gradLen = length(grad);

    vec4 xferCol = texture2D(texXfer, vec2(opacityRaw, gradLen * 2.0));

    float opacityLocal = xferCol.a * opacityRaw;
    opacityLocal = 1.0 - pow(1.0 - opacityLocal, opacityCorrect);

    if (lightStyle == LIGHT_STYLE_ALPHA)
    {
        gl_FragColor = vec4(0, 0, 0, opacityLocal);
        return;
    }

    vec3 color = xferCol.xyz;

    if (lightStyle == LIGHT_STYLE_LIGHTMAP)
    {
        vec4 lightPos = lightMvp * vec4(posLocal, 1);
        vec2 coord = lightPos.xy / lightPos.w;
        coord = (coord + 1.0) * 0.5;
        vec4 lightMapColor = texture2DRect(texLightMap, coord * LIGHTMAP_SIDE_LEN);

        color *= lightMapColor.xyz * (1.0 - lightMapColor.w);

//gl_FragColor = lightMapColor;
//gl_FragColor = vec4(lightMapColor.aaa, 1);
//return;
    }

    if (lightStyle == LIGHT_STYLE_PHONG)
    {
        vec3 normal = -normalize(grad);
        float diff = saturate(dot(normal, lightDir));
        float sat = pow(saturate(dot(normal, lightHalfDir)), 20.0);

        //Any component of the gradient will be bounded by [-.5 .5],
        // so divide by .5^3 to normalize gradient length to 1.
        // Plus throw in an artistic scalar of 2 to make it more
        // visually sharp.
        float scalar = saturate(gradLen * 2.0 / (0.5 * 0.5 * 0.5));
        color = color * lightColor * saturate(diff + sat) * scalar;
    }

    //Premult alpha
    gl_FragColor = vec4(color.rgb * opacityLocal, opacityLocal);

}

