package blobTracking;

import frameWork.Gui;
import ij.plugin.PlugIn;

public class CellAndBlobTracking implements PlugIn{

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
		Gui g= new BlobGui(null);
		g.exec();
	}

}
