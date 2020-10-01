package com.jantie.volume.pick.ray.graph;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jantie.volume.pick.ray.IRayCastChangeListner;
import com.jantie.volume.pick.ray.RayCaster;

public class RayProfilePanel extends JPanel implements IRayCastChangeListner {
	private static final long serialVersionUID = 1L;

	private RayCaster rayCaster;

	private RayProfileGraph orginalProfile = new RayProfileGraph("Orginal values ray profile");
	private RayProfileGraph gpuRayProfile = new RayProfileGraph("Renderd ray profile (RRP)",Color.BLUE,0.95f,0.05f);
	private RayProfileGraph gpuAccSum = new RayProfileGraph("accumulated RRP (ARRP)");
	private RayProfileGraph gpuFirstDerv = new RayProfileGraph("1. derv. vis. ARRP",Color.green,0.95f,0.05f);
	private RayProfileGraph gpuSecDerv = new RayProfileGraph("2. derv. vis. ARRP",Color.MAGENTA,0.5f,0.5f);
	private RayProfileGraph [] imLazy = {orginalProfile, gpuRayProfile, gpuAccSum , gpuFirstDerv ,gpuSecDerv};

	private class CbEvent implements ActionListener{
		private RayProfileGraph graph;
		private JCheckBox box;
		protected CbEvent(RayProfileGraph graph,JCheckBox box){
			this.graph = graph;
			this.box = box;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			graph.setVisible(box.isSelected());
		}
	}
	
	public RayProfilePanel(){
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		JPanel PCheckBoxes = new JPanel(new FlowLayout());
		this.add(PCheckBoxes);
		
		gpuRayProfile.setVisible(false);
		gpuFirstDerv.setVisible(false);
		gpuSecDerv.setVisible(false);
		
		for (RayProfileGraph rpg : imLazy){
			System.out.println(rpg.getName());
			JCheckBox CBtmp = new JCheckBox(rpg.getName());
			CBtmp.addActionListener(new CbEvent(rpg, CBtmp));
			CBtmp.setSelected(rpg.isVisible());
			PCheckBoxes.add(CBtmp);
			this.add(rpg);
		}
	}
	
	@Override
	public void rayUpdate() {
		orginalProfile.setRayProfile(rayCaster.orginalRayProfile);
		gpuRayProfile.setRayProfile(rayCaster.gpuRayProfile);
		gpuAccSum.setRayProfile(rayCaster.gpuAccSum);
		gpuFirstDerv.setRayProfile(rayCaster.gpuFirstDerv);
		gpuSecDerv.setRayProfile(rayCaster.gpuSecDerv);
		
	}

	public void setRayCaster(RayCaster rayCaster) {
		this.rayCaster = rayCaster;
	}
	
	public void setRelMarkIndex(float relIndex,float relGpuIndex){
		for (RayProfileGraph rpg : imLazy){
			rpg.setRelMarkIndex(relGpuIndex);
		}
		orginalProfile.setRelMarkIndex(relIndex);
	}
	
	
}
