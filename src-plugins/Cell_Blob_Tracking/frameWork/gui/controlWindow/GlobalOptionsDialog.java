package frameWork.gui.controlWindow;

import java.awt.Dimension;

import frameWork.Model;
import ij.gui.GenericDialog;

import javax.swing.JDialog;

public class GlobalOptionsDialog {

	private double deltaZ;
	private int intensityOffset;
	   
	    public GlobalOptionsDialog(Model<?> mod) {
	      GenericDialog gd = new GenericDialog("Global Options");
	      //gd.setPreferredSize(new Dimension(300,200));
	      gd.addNumericField("Intensity Offset: ", mod.getIntensityOffset(), 0);
	      gd.addNumericField("\u0394 Z (in pixels): ", mod.getXyToZ(), 0);
	      gd.showDialog();
	      if (gd.wasCanceled()) return;

	      intensityOffset = (int)gd.getNextNumber();
	      deltaZ = (double)gd.getNextNumber();
	      
	      
	      if (gd.wasOKed()){
	    	  mod.setIntensityOffset(intensityOffset);
	    	  mod.setXyToZ(deltaZ);
	    	  
	      }
	   }
	    
	    public double getDeltaZ(){
	    	return deltaZ;
	    	
	    }
	    
	    public double getInensityOffest(){
	    	return intensityOffset;
	    }
	    
	    
	    public static void main(String[] args) {
GlobalOptionsDialog test = new GlobalOptionsDialog(null);

}
}
