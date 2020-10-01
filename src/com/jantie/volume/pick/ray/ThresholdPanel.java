package com.jantie.volume.pick.ray;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3f;


public class ThresholdPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private RayPickPanel parent;
	private float selectedValue = 0.5f;
	private final int sliderLength= 100000;
	
	private final JLabel selectedThresholdValue = new JLabel(""+selectedValue);
	private final JSlider SliderThresholdSelect = new JSlider(0, sliderLength);
	private final JLabel LDescThreshold;
	
	public ThresholdPanel(RayPickPanel parent) {
		super(new GridBagLayout());
		this.parent = parent;
		
		this.addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				SliderThresholdSelect.setValue(SliderThresholdSelect.getValue()+e.getWheelRotation()*-50);
			}
		});

		//slider description
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put( new Integer( 0 ), new JLabel("0.0") );
		labelTable.put( new Integer( sliderLength), new JLabel("5.0") );
		SliderThresholdSelect.setLabelTable( labelTable );
		SliderThresholdSelect.setPaintLabels(true);
		//set slider update behavior
		SliderThresholdSelect.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				selectedValue = 5*(float)(SliderThresholdSelect.getValue())/sliderLength;
				selectedThresholdValue.setText(""+selectedValue);
				
			}
		});
		
		GridBagConstraints gbc = new GridBagConstraints();
		LDescThreshold = new JLabel("Threshold:");
		this.add(LDescThreshold,gbc);
		
		gbc.gridx=1;
		this.add(selectedThresholdValue, gbc);
		
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.gridy=1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		this.add(SliderThresholdSelect, gbc);
		
		gbc.gridwidth = 1;
	}
	
	public void pick(boolean firstHit){
		int index = pick2(firstHit);
		if (index != 0){
			int samplesPerPlane = parent.getRayCaster().rayProfile.length / parent.getRayCaster().cube.getNumPlanes();
			parent.getMark().setPos(new Vector3f(parent.getRayCaster().rayPositions[index*samplesPerPlane]));
			parent.setRelMarkPos((float)(index*samplesPerPlane)/parent.getRayCaster().rayProfile.length
					,(float)(index)/parent.getRayCaster().gpuRayProfile.length);
			parent.getCube().setSelectorMaskCenter(new Vector3f(parent.getRayCaster().rayPositions[index*samplesPerPlane]));
		}
	}
	
	private int pick2(boolean firstHit){
		for (int i = 5; i< parent.getRayCaster().gpuRayProfile.length; ++i){
			if(firstHit){//first hit
				if ( parent.getRayCaster().gpuRayProfile[i] > selectedValue){//we have a hit
					System.out.println(parent.getRayCaster().gpuRayProfile[i] +">"+ selectedValue);
					return i;
				}
			}
			if(!firstHit){//sumHit
				if ( i < parent.getRayCaster().gpuAccSum.length){
					if ( parent.getRayCaster().gpuAccSum[i] > selectedValue){//we have a hit
						return i;
					}
				}
			}
		}
		return 0;
	}

	@Override
	public void setEnabled(boolean enabled) {
		selectedThresholdValue.setEnabled(enabled);
		SliderThresholdSelect.setEnabled(enabled);
		LDescThreshold.setEnabled(enabled);
		
		super.setEnabled(enabled);
	}
	
	

	
	

}
