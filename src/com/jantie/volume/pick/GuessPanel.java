package com.jantie.volume.pick;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.kitfox.volume.viewer.MarkCube;
import com.kitfox.volume.viewer.ViewerPanel;

public class GuessPanel extends JPanel {
	
	private static final long serialVersionUID = 0;
	
	MarkCube mark;
	ViewerPanel viewerPanel;

	public GuessPanel(ViewerPanel viewPanel){
		super(new GridBagLayout());
		this.viewerPanel = viewPanel;
		
		GridBagConstraints gbc = new GridBagConstraints();
		final JLabel xLabel = new JLabel("X:");
		final JLabel yLabel = new JLabel("Y:");
		final JLabel zLabel = new JLabel("Z:");
		final JSlider xSlider = new JSlider(-100, 100, 0);
		final JSlider ySlider = new JSlider(-100, 100, 0);
		final JSlider zSlider = new JSlider(-100, 100, 0);
		
		xSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mark.setXPos((float)xSlider.getValue()/100f);
				mark.setYPos((float)ySlider.getValue()/100f);
				mark.setZPos((float)zSlider.getValue()/100f);
				viewerPanel.display();
			}
		});
		
		ySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mark.setXPos((float)xSlider.getValue()/100f);
				mark.setYPos((float)ySlider.getValue()/100f);
				mark.setZPos((float)zSlider.getValue()/100f);
				viewerPanel.display();
			}
		});
		
		zSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mark.setXPos((float)xSlider.getValue()/100f);
				mark.setYPos((float)ySlider.getValue()/100f);
				mark.setZPos((float)zSlider.getValue()/100f);
				viewerPanel.display();
			}
		});
		
		JSlider[] sliders = {xSlider,ySlider,zSlider};
		for ( JSlider slid : sliders){
			slid.setMajorTickSpacing(50);
			slid.setMinorTickSpacing(10);
			slid.setPaintTicks(true);
			slid.setPaintLabels(true);
		}
		
		
		gbc.gridy = 0;
		this.add(xLabel, gbc);
		gbc.gridy = 1;
		this.add(yLabel, gbc);
		gbc.gridy = 2;
		this.add(zLabel, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		this.add(xSlider, gbc);
		gbc.gridy = 1;
		this.add(ySlider, gbc);
		gbc.gridy = 2;
		this.add(zSlider, gbc);
	
		
	}
	
	public MarkCube getMark() {
		return mark;
	}

	public void setMark(MarkCube mark) {
		this.mark = mark;
	}
	
	

}
