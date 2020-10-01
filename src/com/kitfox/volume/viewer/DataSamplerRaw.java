package com.kitfox.volume.viewer;

import com.jantie.volume.input.VolumeDataBufferContainer;

public class DataSamplerRaw extends DataSampler {

	public DataSamplerRaw(VolumeDataBufferContainer vdc) {
        xSpan = vdc.getWidth();
        ySpan = vdc.getHeight();
        zSpan = vdc.getDepth();
        
        int size = xSpan * ySpan * zSpan;
        System.err.println("Mem needed " + (size * 4 * 4));
        values = new float[size];
        valuesDx = new float[size];
        valuesDy = new float[size];
        valuesDz = new float[size];
        
        vdc.getBuffer().get(values);

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
