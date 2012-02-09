

import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import blobTracking.Blob;
import blobTracking.BlobFactory;
import blobTracking.BlobFrame;
import blobTracking.BlobController;
import frameWork.Model;
import frameWork.Controller;
import frameWork.gui.ViewModel;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class Cell_Blob_Tracking <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>> implements PlugIn{

	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		long time0= System.nanoTime();
		ImagePlus imp=IJ.getImage();
		System.out.println("creating Model...");
		Model< IT> model= new Model<IT>(imp);
				System.out.println("creating Controller...");
					
		
		Controller<IT> cont= new Controller<IT>(model);
		System.out.println("creating ViewModel...");
		ViewModel<IT> vm= new ViewModel<IT>(imp, model,cont);
		System.out.println("done!");
		long time1= System.nanoTime();
		model.addObserver(vm);
		System.out.println("Time taken:"+((time1-time0)/1000000));
	}

}
