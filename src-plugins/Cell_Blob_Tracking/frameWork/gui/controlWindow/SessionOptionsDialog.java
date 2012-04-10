package frameWork.gui.controlWindow;
import frameWork.Model;
import ij.gui.GenericDialog;

public class SessionOptionsDialog {

	private double deltaZ;
	private double intensityOffset;
	   
	    public SessionOptionsDialog(Model<?> mod) {
	      GenericDialog gd = new GenericDialog("Global Options");
	      //gd.setPreferredSize(new Dimension(300,200));
	      gd.addNumericField("Intensity Offset: ", 0, 0);
	      gd.addNumericField("\u0394 Z (in pixels): ", 0, 0);
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
GlobalOptionsDialog test = new GlobalOptionsDialog(null);

}
}
