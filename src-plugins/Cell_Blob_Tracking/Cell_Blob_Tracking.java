

import java.util.ArrayList;

import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import blobTracking.Blob;
import blobTracking.BlobTrackingChannel;
import blobTracking.BlobFrame;
import blobTracking.BlobController;
import frameWork.Model;
import frameWork.Controller;
import frameWork.MovieChannel;
import frameWork.MovieFrame;
import frameWork.gui.KymographX;
import frameWork.gui.KymographY;
import frameWork.gui.MainWindow;
import frameWork.gui.MaxProjectionX;
import frameWork.gui.MaxProjectionY;
import frameWork.gui.MaxProjectionZ;
import frameWork.gui.ViewModel;
import frameWork.gui.ViewWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class Cell_Blob_Tracking <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>> implements PlugIn{

	public class AddingViewsThread extends Thread{
		private Model <IT> model;
		private ViewModel<IT> viewModel;
		
		AddingViewsThread(ViewModel<IT> vm, Model<IT> mod){
			model= mod;
			viewModel=vm;
		}
		public void run() {
			
			System.out.println("iv:" + model.isVolume()+ "  its:" +model.isTimeSequence() + "  imc:"+ model.isMultiChannel() );
			
	        if(model.isVolume()){
	        System.out.println("adding projections");
			viewModel.addViewWindow(new MaxProjectionX<IT>(model, viewModel));
			viewModel.addViewWindow(new MaxProjectionY<IT>(model, viewModel));
			viewModel.addViewWindow(new MaxProjectionZ<IT>(model, viewModel));
	        }
	        
			viewModel.addViewWindow(new KymographY<IT>(model, null,viewModel));		
			viewModel.addViewWindow(new KymographX<IT>(model, null,viewModel));
		       
			
	    }
	}
	
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
		
		AddingViewsThread awt= new AddingViewsThread(vm,model);
		awt.start();
	}

}
