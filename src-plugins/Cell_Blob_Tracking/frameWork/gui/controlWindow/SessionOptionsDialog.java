package frameWork.gui.controlWindow;
import frameWork.Model;
import ij.gui.GenericDialog;

public class SessionOptionsDialog {

	 static String title="Example";
	    static int width=512,height=512;
	   
	    public SessionOptionsDialog(Model<?> mod) {
	      GenericDialog gd = new GenericDialog("Global Options");
	      gd.addStringField("Title: ", title);
	      gd.addNumericField("Width: ", width, 0);
	      gd.addNumericField("Height: ", height, 0);
	      gd.showDialog();
	      if (gd.wasCanceled()) return;
	      title = gd.getNextString();
	      width = (int)gd.getNextNumber();
	      height = (int)gd.getNextNumber();

	   }
	    
	    public static void main(String[] args) {
GlobalOptionsDialog test = new GlobalOptionsDialog(null);

}
}
