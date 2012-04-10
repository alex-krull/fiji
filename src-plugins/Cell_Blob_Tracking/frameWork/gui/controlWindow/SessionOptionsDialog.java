package frameWork.gui.controlWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import blobTracking.BlobSession;

import frameWork.Model;
import ij.gui.GenericDialog;

public class SessionOptionsDialog{

	private double deltaZ;
	private double intensityOffset;
	   
	    public SessionOptionsDialog(BlobSession<?> mod) {
	      GenericDialog gd = new GenericDialog("Session Options");
	     // gd.setPreferredSize(new Dimension(300,200));

	      gd.addCheckbox("Automatic \u03C3", mod.isAutoSigmaZ());
	      gd.addNumericField("Max \u03C3:", mod.getDefaultMaxSigma(), 0);
	      gd.addNumericField("Min \u03C3:", mod.getDefaultMinSigma(), 0);
	      gd.addNumericField("Initial \u03C3:", mod.getDefaultSigma(), 0);
	      
	      if(mod.isVolune()){
		      gd.addNumericField("Max Z \u03C3:", mod.getDefaultMaxSigmaZ(), 0);
		      gd.addNumericField("Min Z \u03C3:", mod.getDefaultMinSigmaZ(), 0);
		      gd.addNumericField("Initial Z \u03C3:", mod.getDefaultSigmaZ(), 0);
	      }
	      
	      gd.addMessage("");
	      gd.addNumericField("Quality Threshold:", 0, 0);
	      
	      
	      gd.showDialog();
	      if (gd.wasCanceled()) return;

	      if(gd.wasOKed()){
	    	  double max=gd.getNextNumber();
	    	  double min = gd.getNextNumber();
	    	  double init = gd.getNextNumber();
	    	  
	    	  if(mod.isVolune()){
	    		  double maxz=gd.getNextNumber();
	    		  double minz=gd.getNextNumber();
	    		  double initz=gd.getNextNumber();
	    		  mod.setDefaultSigmaZ(initz);
	    		  mod.setDefaultMaxSigmaZ(maxz);
	    		  mod.setDefaultMinSigmaZ(minz);
	    	  }
	    	  
	    	  mod.setDefaultMaxSigma(max);
	    	  mod.setDefaultMinSigma(min);
	    	  mod.setDefaultSigma(init);
	    	  

	    	  
	    	  mod.setAutoSigmaZ(gd.getNextBoolean());
	      }
	      

	      
	   }
	    
	
	    
	    public static void main(String[] args) {
	    	SessionOptionsDialog test = new SessionOptionsDialog(null);

}
}
