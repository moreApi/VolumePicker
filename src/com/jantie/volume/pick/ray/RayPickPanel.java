package com.jantie.volume.pick.ray;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.vecmath.Vector3f;

import com.jantie.volume.pick.ray.graph.RayProfileGraph;
import com.jantie.volume.pick.ray.graph.RayProfilePanel;
import com.kitfox.volume.viewer.DebugDraw;
import com.kitfox.volume.viewer.MarkCube;
import com.kitfox.volume.viewer.ViewerCube;
import com.kitfox.volume.viewer.ViewerPanel;

public class RayPickPanel extends JPanel implements IRayCastChangeListner{
	private static final long serialVersionUID = 1L;

	private RayCaster rayCaster;
	private ViewerCube viewerCube;
	private DebugDraw debugDraw;
	private RayProfilePanel rayProfilePanel;
	
	private WYSIWYPick wysiwyPick;
	private CrossRaysPick crossRaysPick;

	private final JRadioButton RadioButtSum;
	private final JRadioButton RadioButtFirstHit;
	private final JRadioButton RadioButtWYSIWYP;
	private final JRadioButton RadioButtCrossRays;
	private final ThresholdPanel thresholdPanel;

	public RayPickPanel(ViewerPanel viewPanel) {
		super(new GridBagLayout());
		
		wysiwyPick = new WYSIWYPick(this);
		crossRaysPick = new CrossRaysPick(this);
		
		RadioButtSum = new JRadioButton("Sum");
		RadioButtSum.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				thresholdPanel.setEnabled(true);
				viewerCube.getMark().setPos(0, 0, 0);
				setRelMarkPos(0,0);
			}
		});
		RadioButtFirstHit = new JRadioButton("First hit");
		RadioButtFirstHit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				thresholdPanel.setEnabled(true);
				viewerCube.getMark().setPos(0, 0, 0);
				setRelMarkPos(0,0);
			}
		});
		RadioButtWYSIWYP = new JRadioButton("WYSIWYP");
		RadioButtWYSIWYP.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				thresholdPanel.setEnabled(false);
				viewerCube.getMark().setPos(0, 0, 0);
				setRelMarkPos(0,0);
			}
		});
		RadioButtCrossRays = new JRadioButton("Ray crossing");
		RadioButtCrossRays.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				thresholdPanel.setEnabled(false);
				viewerCube.getMark().setPos(0, 0, 0);
				setRelMarkPos(0,0);
			}
		});
		RadioButtFirstHit.setSelected(true);
		ButtonGroup ButtGroupMethod = new ButtonGroup();
		ButtGroupMethod.add(RadioButtFirstHit);
		ButtGroupMethod.add(RadioButtSum);
		ButtGroupMethod.add(RadioButtWYSIWYP);
		ButtGroupMethod.add(RadioButtCrossRays);
		
		thresholdPanel = new ThresholdPanel(this);
		
		GridBagConstraints gbc = new GridBagConstraints();		
		JLabel LDescMethodSelection = new JLabel("Method: ");
		this.add(LDescMethodSelection,gbc);
		
		gbc.gridy=1;
		this.add(RadioButtFirstHit, gbc);
		
		gbc.gridx=1;
		this.add(RadioButtSum,gbc);		
		
		gbc.gridx=2;
		this.add(RadioButtWYSIWYP, gbc);
		
		gbc.gridx = 3;
		this.add(RadioButtCrossRays, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 4;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.WEST;
		this.add(thresholdPanel,gbc);
		
	}

	@Override
	public void rayUpdate() {
		setRelMarkPos(0,0);
		
		viewerCube.setDrawRay(true);
		Vector3f[] rayPositions = getRayCaster().rayPositions;
		viewerCube.setDrawRaySecond(false);
		getDebugDraw().setFirstRay(new Vector3f[]{new Vector3f(rayPositions[0]), new Vector3f(rayPositions[rayPositions.length-2])});
		if(RadioButtFirstHit.isSelected()){//first hit
			thresholdPanel.pick(true);
		}
		if(RadioButtSum.isSelected()){//sumHit
			thresholdPanel.pick(false);
		}
		if(RadioButtWYSIWYP.isSelected()){//wysiwyPick
			wysiwyPick.pick();
		}
		if(RadioButtCrossRays.isSelected()){//ray crossing
			
			viewerCube.setDrawRay(true);
			viewerCube.setDrawRaySecond(true);
			crossRaysPick.freshRay();
		}
	}

	public DebugDraw getDebugDraw() {
		return debugDraw;
	}

	public void setDebugDraw(DebugDraw debugDraw) {
		this.debugDraw = debugDraw;
	}

	public MarkCube getMark() {
		return viewerCube.getMark();
	}
	
	public ViewerCube getCube(){
		return viewerCube;
	}

	public void setCube(ViewerCube cube){
		this.viewerCube = cube;
	}

	public RayCaster getRayCaster() {
		return rayCaster;
	}

	public void setRayCaster(RayCaster rayCaster) {
		this.rayCaster = rayCaster;
	}

	public WYSIWYPick getWysiwyPick() {
		return wysiwyPick;
	}
	
	public void setRelMarkPos(float relPos,float relGpuPos){
		rayProfilePanel.setRelMarkIndex(relPos,relGpuPos);
	}

	public RayProfilePanel getRayProfilePanel() {
		return rayProfilePanel;
	}

	public void setRayProfilePanel(RayProfilePanel rayProfilePanel) {
		this.rayProfilePanel = rayProfilePanel;
	}

	

	
}
