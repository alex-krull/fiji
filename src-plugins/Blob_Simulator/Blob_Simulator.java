import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class Blob_Simulator implements PlugIn{

	@Override
	public void run(String arg0) {
		GenericDialog gd = new GenericDialog("blob simulator");
		gd.addNumericField("xSize", 100, 0);
		gd.addNumericField("ySize", 100, 0);
		gd.addNumericField("frames", 100, 0);
		gd.addNumericField("number of Blobs", 1, 0);
		gd.showDialog();
		
		
		if (gd.wasCanceled()) {
		IJ.error("PlugIn canceled!");
		return;
		}

		
		int xSize= (int)gd.getNextNumber();
		int ySize= (int)gd.getNextNumber();
		int frames= (int)gd.getNextNumber();
		int numOfBlobs= (int)gd.getNextNumber();
		

	}

}
