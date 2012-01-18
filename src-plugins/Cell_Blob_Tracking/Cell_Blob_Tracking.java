

import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import blobTracking.Blob;
import blobTracking.BlobFrame;
import blobTracking.BlobController;
import frameWork.Model;
import frameWork.Controller;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class Cell_Blob_Tracking <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>> implements PlugIn{

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		long time0= System.nanoTime();
		ImagePlus imp=IJ.getImage();
		Img<IT> img= ImagePlusAdapter.wrap(imp);
		System.out.println("creating Model...");
		Model<Blob, IT> contr= new Model<Blob,IT>(img, new BlobFrame<IT>(0,null));
		System.out.println("creating Gui...");
		BlobController<IT> gui= new BlobController<IT>(imp, img,contr);
		System.out.println("done!");
		long time1= System.nanoTime();
		System.out.println("Time taken:"+((time1-time0)/1000000));
	}

}
