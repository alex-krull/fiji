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


	   
	    public SessionOptionsDialog(BlobSession<?> mod) {
	      GenericDialog gd = new GenericDialog("Session Options");
	     
	     // gd.setPreferredSize(new Dimension(300,200));
	     
	      gd.addCheckbox("Automatic \u03C3", mod.isAutoSigmaZ());
	      gd.addNumericField("\u03C3:", mod.getDefaultSigma(), 0);
	      gd.addNumericField("Max \u03C3:", mod.getDefaultMaxSigma(), 0);
	      gd.addNumericField("Min \u03C3:", mod.getDefaultMinSigma(), 0);
	      
	      
	      if(mod.isVolune()){
	    	  gd.addMessage("");
	    	  gd.addNumericField("\u03C3Z:", mod.getDefaultSigmaZ(), 0);
	    	  gd.addNumericField("Max \u03C3Z:", mod.getDefaultMaxSigmaZ(), 0);
		      gd.addNumericField("Min \u03C3Z:", mod.getDefaultMinSigmaZ(), 0);
		      
	      }
	      
	      gd.addMessage("");
	      gd.addNumericField("Quality Threshold:", 0, 0);
	      
	      
	      gd.showDialog();
	      if (gd.wasCanceled()) return;

	      if(gd.wasOKed()){
	    	  double init = gd.getNextNumber();
	    	  double max=gd.getNextNumber();
	    	  double min = gd.getNextNumber();
	    	  
	    	  
	    	  if(mod.isVolune()){
	    		  double initz=gd.getNextNumber();
	    		  double maxz=gd.getNextNumber();
	    		  double minz=gd.getNextNumber();
	    		  
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
