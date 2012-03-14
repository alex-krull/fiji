import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class Blob_Simulator implements PlugIn{

	@Override
	public void run(String arg0) {
		GenericDialog gd = new GenericDialog("title");
		gd.showDialog();
	}

}
