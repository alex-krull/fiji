package frameWork.gui.controlWindow;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import frameWork.Model;
import ij.gui.GenericDialog;

public class SessionOptionsDialog extends JDialog{

	private double deltaZ;
	private double intensityOffset;
	   
	    public SessionOptionsDialog(Model<?> mod) {
	      GenericDialog gd = new GenericDialog("Session Options");
	      gd.setPreferredSize(new Dimension(300,200));
	      
	      gd.addCheckbox("Automatic \u03C3", true);
	      //gd.addNumericField("Initial \u03C3: ", 0, 0);
	     // gd.addNumericField("Max \u03C3: ", 0, 0);
	      //gd.addNumericField("Min \u03C3: ", 0, 0);
	      
	      JPanel test = new JPanel();
	      test.add(new JButton("test"));
	      
	      gd.add(test);
	      gd.showDialog();
	      if (gd.wasCanceled()) return;

	      intensityOffset = (int)gd.getNextNumber();
	      deltaZ = (int)gd.getNextNumber();
	      
	   }
	    
	    public double getDeltaZ(){
	    	return deltaZ;
	    	
	    }
	    
	    public double getInensityOffest(){
	    	return intensityOffset;
	    }
	    
	    public static void main(String[] args) {
	    	SessionOptionsDialog test = new SessionOptionsDialog(null);

}
}
