package com.jantie.volume.pick.ray.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jantie.volume.pick.ray.IRayCastChangeListner;
import com.jantie.volume.pick.ray.RayCaster;

/**
 * displays one graph 
 * also provides axis description
 * @author Jan
 *
 */
public class RayProfileGraph extends JPanel{
	private static final long serialVersionUID = 1L;
	
	private RayProfileGraphCanvas graphCanvas;
	private JLabel max;
	private JLabel min;
	private JLabel LName;
	private float relOffset =0;
	private float relMarkIndex = 0;

	public RayProfileGraph(){
		this("Graph");
	}
	
	public RayProfileGraph(String name){
		this(name,Color.blue, 1,0);
	}
	
	public RayProfileGraph(String name,Color color, float scale, float relOffest) {
		super(new BorderLayout());
		this.setPreferredSize(new Dimension(770, 225));
		this.setName(name);
		this.relOffset = relOffest;
		
		graphCanvas = new RayProfileGraphCanvas(color,scale,relOffest);
		//graphPanel.setPreferredSize(new Dimension(700, 250));
		
		JPanel desc = new JPanel(new BorderLayout());
		
		max = new JLabel("1.0000");
		max.setPreferredSize(new Dimension(75, 10));
		desc.add(max,BorderLayout.NORTH);
		
		min = new JLabel("0.0000");
		min.setPreferredSize(new Dimension(50, 10));
		desc.add(min,BorderLayout.SOUTH);
		
		if(name != null){
			JPanel PName = new JPanel();
			LName = new JLabel(name);
			PName.add(LName);
			this.add(PName, BorderLayout.NORTH);
		}
		
		this.add(desc,BorderLayout.WEST);
		this.add(graphCanvas,BorderLayout.CENTER);
	}

	/**
	 * sets profile and calculates the maximum
	 * @param profile
	 */
	public void setRayProfile(float[] profile){
		float maxValue = 0;
		for (float f:profile){
			if (f > maxValue)
				maxValue = f;
		}
		max.setText(""+maxValue);
		min.setText(""+((maxValue*-1)*relOffset*2));
		
		graphCanvas.setRayProfile(profile, maxValue);
		graphCanvas.graphUpdate();
		this.repaint();
	}
	
	protected void setRelMarkIndex(float relIndex){
		relMarkIndex = relIndex;
	}
	
	class RayProfileGraphCanvas extends JPanel {
		private static final long serialVersionUID = 1L;

		private int width ,height;
		private float scale,relOffset;
		
		private float[] profile = null;
		private float maxValue;
		private Color color;

		public RayProfileGraphCanvas(){
			this(Color.blue,1,0);
		}
		
		public RayProfileGraphCanvas(Color color,float scale, float relOffset) {
			super();
			this.setBackground(Color.white);
			this.color = color;
			this.scale = scale;
			this.relOffset = relOffset;
		}

		public void paintComponent(Graphics g){
			super.paintComponent(g);
			if (profile == null){
				return;
			}
			g.setColor(Color.black);
			g.drawLine(0, (int)((1-relOffset)*height), width, (int)((1-relOffset)*height));
			g.setColor(color);
			width = getWidth();
			height = getHeight();
			drawRay(g, profile, scale,relOffset);
			//draw mark mark
			if (relMarkIndex != 0){
				g.setColor(Color.red);
				g.drawLine((int)(relMarkIndex*width), 0, (int)(relMarkIndex*width), height);	
			}
		}
		
		private void drawRay(Graphics g, float[] rayProfile,float scale, float relOffset){
			int x = 5;
			int last = 5;
			while (x < width-5){
			//for (int x = 5; x < width-5;x++){				
				int xRay = (int) ((x/(float)width)*rayProfile.length);
				int lastRay = (int) ((last/(float)width)*rayProfile.length);
				if (xRay != lastRay){
					float y = rayProfile[lastRay];
					y = y/maxValue*(height*scale); 
					y = height -y;
					y -= height*relOffset;
					float yNext = rayProfile[Math.min(xRay,rayProfile.length-1)];
					yNext = (yNext/maxValue*(height*scale)); 
					yNext = height - yNext;
					yNext -= height*relOffset;
					//System.out.println(" y:"+y+" yNext: "+yNext);
					g.drawLine(last,(int)y,x, (int) yNext); //draw line to next value
					last = x;
				} 
				x+=1;
			}
		}

		protected void setRayProfile(float[] profile,float maxValue){
			this.profile = profile;
			this.maxValue = maxValue;
		}

		public void graphUpdate() {
			this.repaint();
		}
		
		public void setColor(Color c){
			color = c;
		}
	}

}
