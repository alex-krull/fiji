

import blobTracking.BlobGui;
import frameWork.Gui;
import ij.plugin.PlugIn;

public class Cell_Blob_Tracking implements PlugIn{

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		Gui g= new BlobGui(null);
		g.exec();
	}

}
