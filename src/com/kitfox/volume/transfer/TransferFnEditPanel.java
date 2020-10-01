/*
 * TransferFnEditPanel.java
 * Created on Dec 28, 2009, 3:23:40 AM
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

package com.kitfox.volume.transfer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.kitfox.volume.viewer.Histogram;
import com.kitfox.volume.viewer.VolumeData;

/**
 *
 * @author kitfox
 */
public class TransferFnEditPanel extends javax.swing.JPanel
{
    private static final long serialVersionUID = 0;

    MouseEvent mousePos;
    MouseEvent lastDabPos;

    BufferedImage histImg;
    BufferedImage xferImg;
    BufferedImage xferMaskImg;
    BufferedImage brushFace;

    float spacing = 5;

    TexturePaint checkerPaint;

    public static enum OverlayType
    {
        COLOR, MASK
    }

    public static enum BrushType
    {
        BRUSH, ERASER
    }

    protected VolumeData volumeData;
    public static final String PROP_VOLUMEDATA = "volumeData";
    protected float opacity = .5f;
    public static final String PROP_OPACITY = "opacity";
    protected Color brushColor = Color.RED;
    public static final String PROP_BRUSHCOLOR = "brushColor";
    protected OverlayType overlayType = OverlayType.COLOR;
    public static final String PROP_OVERLAYTYPE = "overlayType";
    protected BrushType brushType = BrushType.BRUSH;
    public static final String PROP_BRUSHTYPE = "brushType";
    protected float brushSize = 10;
    public static final String PROP_BRUSHSIZE = "brushSize";
    protected float brushSharp = .5f;
    public static final String PROP_BRUSHSHARP = "brushSharp";
    protected float brushOpacity = .5f;
    public static final String PROP_BRUSHOPACITY = "brushOpacity";
    protected boolean displayHistogram = true;
    public static final String PROP_DISPLAYHISTOGRAM = "displayHistogram";

    /** Creates new form TransferFnEditPanel */
    public TransferFnEditPanel()
    {
        initComponents();
        setPreferredSize(new Dimension(256, 128));
    }

    private TexturePaint getCheckerPaint()
    {
        if (checkerPaint == null)
        {
            BufferedImage bgImg = getGraphicsConfiguration().createCompatibleImage(16, 16);
            Graphics2D g = bgImg.createGraphics();
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, 16, 16);
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, 8, 8);
            g.fillRect(8, 8, 8, 8);
            g.dispose();

            checkerPaint = new TexturePaint(bgImg, new Rectangle(0, 0, 16, 16));
        }

        return checkerPaint;
    }

    @Override
    protected void paintComponent(Graphics gg)
    {
        //super.paintComponent(g);
        Graphics2D g = (Graphics2D)gg;

        if (volumeData == null)
        {
            return;
        }

        //Draw histogram
        Histogram hist = volumeData.getHist();
        if (histImg == null)
        {
            histImg = hist.createImage(getGraphicsConfiguration());
            xferImg = volumeData.getTransferFunction();
            xferMaskImg = getGraphicsConfiguration()
                    .createCompatibleImage(xferImg.getWidth(), xferImg.getHeight(), Transparency.TRANSLUCENT);
        }

        if (displayHistogram)
        {
            g.drawImage(histImg, 0, 0, getWidth(), getHeight(), this);
        }
        else
        {
            g.setPaint(getCheckerPaint());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        //Draw Overlay
        {
            Composite cacheComp = g.getComposite();
            Composite comp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
            g.setComposite(comp);
            volumeData.getTransferFunction(xferImg);

            switch (overlayType)
            {
                case COLOR:
                    g.drawImage(xferImg, 0, 0, getWidth(), getHeight(), this);
                    break;
                case MASK:
                    buildMask();
                    g.drawImage(xferMaskImg, 0, 0, getWidth(), getHeight(), this);
                    break;
            }
            g.setComposite(cacheComp);
        }

        //Draw cursor
        if  (mousePos != null)
        {
            Ellipse2D.Float cursor =
                    new Ellipse2D.Float(-brushSize / 2, -brushSize / 2, brushSize, brushSize);

            AffineTransform xformFnToDev = new AffineTransform();
            xformFnToDev.translate(mousePos.getX(), mousePos.getY());
            xformFnToDev.scale((double)getWidth() / xferImg.getWidth(), (double)getHeight() / xferImg.getHeight());
            Shape drawCursor = xformFnToDev.createTransformedShape(cursor);

            Raster sampleRaster = null;
            switch (overlayType)
            {
                case COLOR:
                    sampleRaster = xferImg.getRaster();
                    break;
                case MASK:
                    sampleRaster = xferMaskImg.getRaster();
                    break;
            }

            Point2D.Float center = new Point2D.Float(mousePos.getX(), mousePos.getY());
            try {
                xformFnToDev.inverseTransform(center, center);
            } catch (NoninvertibleTransformException ex) {
                Logger.getLogger(TransferFnEditPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            int rrr = sampleRaster.getSample((int)center.getX(), (int)center.getY(), 0);
            int ggg = sampleRaster.getSample((int)center.getX(), (int)center.getY(), 1);
            int bbb = sampleRaster.getSample((int)center.getX(), (int)center.getY(), 2);

            Color col = (rrr + ggg + bbb < 1.5) ? Color.WHITE : Color.BLACK;
            g.setColor(col);
            g.draw(drawCursor);
        }
    }

    private void buildMask()
    {
        WritableRaster maskRaster = xferMaskImg.getRaster();
        WritableRaster raster = xferImg.getRaster();

        for (int j = 0; j < raster.getHeight(); ++j)
        {
            for (int i = 0; i < raster.getWidth(); ++i)
            {
                int alpha = raster.getSample(i, j, 3);
                maskRaster.setSample(i, j, 0, alpha);
                maskRaster.setSample(i, j, 1, 0);
                maskRaster.setSample(i, j, 2, 0);
                maskRaster.setSample(i, j, 3, 255);
            }
        }
    }

    private float lerp(float c0, float c1, float alpha)
    {
        return (1 - alpha) * c0 + alpha * c1;
    }

    private int toRGB(float r, float g, float b, float a)
    {
        int rr = (int)(r * 255 + .5f);
        int gg = (int)(g * 255 + .5f);
        int bb = (int)(b * 255 + .5f);
        int aa = (int)(a * 255 + .5f);
        return rr | (gg << 8) | (bb << 16) | (aa << 24);
    }

    private void dabBrush(MouseEvent evt)
    {
        int cx = evt.getX() * xferImg.getWidth() / getWidth();
        int cy = evt.getY() * xferImg.getHeight() / getHeight();

        BufferedImage brushImg = getBrushFace();
        int side = brushImg.getWidth();
        for (int j = 0; j < brushImg.getHeight(); ++j)
        {
            int fy = cy + j - side / 2;
            if (fy < 0 || fy >= xferImg.getHeight())
            {
                continue;
            }

            for (int i = 0; i < brushImg.getWidth(); ++i)
            {
                int fx = cx + i - side / 2;
                if (fx < 0 || fx >= xferImg.getWidth())
                {
                    continue;
                }

                int src = brushImg.getRGB(i, j);
                int dest = xferImg.getRGB(fx, fy);

                float sr = (src & 0xff) / 255f;
                float sg = ((src >> 8) & 0xff) / 255f;
                float sb = ((src >> 16) & 0xff) / 255f;
                float sa = ((src >> 24) & 0xff) / 255f;

                float dr = (dest & 0xff) / 255f;
                float dg = ((dest >> 8) & 0xff) / 255f;
                float db = ((dest >> 16) & 0xff) / 255f;
                float da = ((dest >> 24) & 0xff) / 255f;


                if (brushType == BrushType.ERASER)
                {
                    int rgba = toRGB(
                            dr, dg, db,
                            da * (1 - sa)
                            );
                    xferImg.setRGB(fx, fy, rgba);
                }
                else if (overlayType == OverlayType.MASK)
                {
                    xferImg.setRGB(fx, fy, toRGB(
                            dr, dg, db,
                            sa + (1 - sa) * da
                            ));
                }
                else
                {
                    xferImg.setRGB(fx, fy, toRGB(
                            lerp(dr, sr, sa),
                            lerp(dg, sg, sa),
                            lerp(db, sb, sa),
                            sa + (1 - sa) * da
                            ));
                }
            }
        }

        volumeData.setTransferFunction(xferImg);
    }

    private BufferedImage getBrushFace()
    {
        BufferedImage brush = brushFace;
        if (brushFace == null)
        {
            int side = (int)Math.ceil(brushSize);
            brushFace = brush = getGraphicsConfiguration()
                    .createCompatibleImage(side, side, Transparency.TRANSLUCENT);

            float cx = side / 2f;
            float cy = side / 2f;
            int color = brushColor.getRGB();

            float invRadius = 2 / brushSize;
            for (int j = 0; j < side; ++j)
            {
                for (int i = 0; i < side; ++i)
                {
                    float dist = (float)Math.sqrt(square(i - cx) + square(j - cy));
                    float p = dist * invRadius;
                    float intensity = 0;
                    if (p < 1)
                    {
                        intensity = p > brushSharp
                                ? (1 - p) / (1 - brushSharp)
                                : 1;
                    }

                    int lum = (int)(brushOpacity * intensity * 255 + .5f);
                    color = (color & 0xffffff) | lum << 24;
                    brush.setRGB(i, j, color);
                }
            }
//try {
//    ImageIO.write(brush, "png", new File("brushHead.png"));
//} catch (IOException ex) {
//    Logger.getLogger(TransferFnEditPanel.class.getName()).log(Level.SEVERE, null, ex);
//}
        }
        return brush;
    }

    private float square(float value)
    {
        return value * value;
    }

    private void clearImageCache()
    {
        histImg = null;
        xferImg = null;
        xferMaskImg = null;
    }

    private void rebuildBrush()
    {
        brushFace = null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setToolTipText("X: Density, Y: Roughness");
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
    }// </editor-fold>//GEN-END:initComponents

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        lastDabPos = evt;
        dabBrush(evt);
        repaint();
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        mousePos = evt;

        int dx = evt.getX() - lastDabPos.getX();
        int dy = evt.getY() - lastDabPos.getY();
        if (dx * dx + dy * dy > spacing)
        {
            lastDabPos = evt;
            dabBrush(evt);
            repaint();
        }

    }//GEN-LAST:event_formMouseDragged

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        mousePos = evt;
        repaint();
    }//GEN-LAST:event_formMouseMoved

    /**
     * Get the value of volumeData
     *
     * @return the value of volumeData
     */
    public VolumeData getVolumeData()
    {
        return volumeData;
    }

    /**
     * Set the value of volumeData
     *
     * @param volumeData new value of volumeData
     */
    public void setVolumeData(VolumeData volumeData)
    {
        VolumeData oldVolumeData = this.volumeData;
        this.volumeData = volumeData;
        clearImageCache();
        firePropertyChange(PROP_VOLUMEDATA, oldVolumeData, volumeData);
    }

    /**
     * Get the value of opacity
     *
     * @return the value of opacity
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Set the value of opacity
     *
     * @param opacity new value of opacity
     */
    public void setOpacity(float opacity) {
        float oldOpacity = this.opacity;
        this.opacity = opacity;
        firePropertyChange(PROP_OPACITY, oldOpacity, opacity);
    }

    /**
     * Get the value of brushColor
     *
     * @return the value of brushColor
     */
    public Color getBrushColor() {
        return brushColor;
    }

    /**
     * Set the value of brushColor
     *
     * @param brushColor new value of brushColor
     */
    public void setBrushColor(Color brushColor) {
        Color oldBrushColor = this.brushColor;
        this.brushColor = brushColor;
        rebuildBrush();
        firePropertyChange(PROP_BRUSHCOLOR, oldBrushColor, brushColor);
    }

    /**
     * Get the value of brushType
     *
     * @return the value of brushType
     */
    public BrushType getBrushType() {
        return brushType;
    }

    /**
     * Set the value of brushType
     *
     * @param brushType new value of brushType
     */
    public void setBrushType(BrushType brushType) {
        BrushType oldBrushType = this.brushType;
        this.brushType = brushType;
        firePropertyChange(PROP_BRUSHTYPE, oldBrushType, brushType);
    }

    /**
     * Get the value of overlayType
     *
     * @return the value of overlayType
     */
    public OverlayType getOverlayType() {
        return overlayType;
    }

    /**
     * Set the value of overlayType
     *
     * @param overlayType new value of overlayType
     */
    public void setOverlayType(OverlayType overlayType) {
        OverlayType oldOverlayType = this.overlayType;
        this.overlayType = overlayType;
        firePropertyChange(PROP_OVERLAYTYPE, oldOverlayType, overlayType);
    }

    /**
     * Get the value of brushOpacity
     *
     * @return the value of brushOpacity
     */
    public float getBrushOpacity() {
        return brushOpacity;
    }

    /**
     * Set the value of brushOpacity
     *
     * @param brushOpacity new value of brushOpacity
     */
    public void setBrushOpacity(float brushOpacity) {
        float oldBrushOpacity = this.brushOpacity;
        this.brushOpacity = brushOpacity;
        rebuildBrush();
        firePropertyChange(PROP_BRUSHOPACITY, oldBrushOpacity, brushOpacity);
    }

    /**
     * Get the value of brushSharp
     *
     * @return the value of brushSharp
     */
    public float getBrushSharp() {
        return brushSharp;
    }

    /**
     * Set the value of brushSharp
     *
     * @param brushSharp new value of brushSharp
     */
    public void setBrushSharp(float brushSharp) {
        float oldBrushSharp = this.brushSharp;
        this.brushSharp = brushSharp;
        rebuildBrush();
        firePropertyChange(PROP_BRUSHSHARP, oldBrushSharp, brushSharp);
    }

    /**
     * Get the value of brushSize
     *
     * @return the value of brushSize
     */
    public float getBrushSize() {
        return brushSize;
    }

    /**
     * Set the value of brushSize
     *
     * @param brushSize new value of brushSize
     */
    public void setBrushSize(float brushSize) {
        float oldBrushSize = this.brushSize;
        this.brushSize = brushSize;
        rebuildBrush();
        firePropertyChange(PROP_BRUSHSIZE, oldBrushSize, brushSize);
    }

    /**
     * Get the value of displayHistogram
     *
     * @return the value of displayHistogram
     */
    public boolean isDisplayHistogram() {
        return displayHistogram;
    }

    /**
     * Set the value of displayHistogram
     *
     * @param displayHistogram new value of displayHistogram
     */
    public void setDisplayHistogram(boolean displayHistogram) {
        boolean oldDisplayHistogram = this.displayHistogram;
        this.displayHistogram = displayHistogram;
        firePropertyChange(PROP_DISPLAYHISTOGRAM, oldDisplayHistogram, displayHistogram);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
