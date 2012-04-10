

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import blobTracking.BlobPolicy;
import fiji.tool.AbstractTool;
import frameWork.Controller;
import frameWork.Model;
import frameWork.gui.KymographX;
import frameWork.gui.KymographY;
import frameWork.gui.MainWindow;
import frameWork.gui.MaxProjectionX;
import frameWork.gui.MaxProjectionY;
import frameWork.gui.MaxProjectionZ;
import frameWork.gui.ViewModel;
import frameWork.gui.controlWindow.ControlWindow;

public class Cell_Blob_Tracking <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>>extends AbstractTool{

	public class AddingViewsThread extends Thread{
		private final Model <IT> model;
		private final ViewModel<IT> viewModel;
		private final ImagePlus imp;
		
		AddingViewsThread(ViewModel<IT> vm, Model<IT> mod, ImagePlus im){
			imp=im;
			model= mod;
			viewModel=vm;
		}
		@Override
		public void run() {
			try {
	            // Set cross-platform Java L&F (also called "Metal")
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName());
	    } 
	    catch (UnsupportedLookAndFeelException e) {
	       // handle exception
	    }
	    catch (ClassNotFoundException e) {
	       // handle exception
	    }
	    catch (InstantiationException e) {
	       // handle exception
	    }
	    catch (IllegalAccessException e) {
	       // handle exception
	    }
			
			//super.run();
			
			
			System.out.println("iv:" + model.isVolume()+ "  its:" +model.isTimeSequence() + "  imc:"+ model.isMultiChannel() );
			double initZoom=imp.getCanvas().getMagnification();
			
			
			
			ControlWindow<IT> cw= new ControlWindow<IT>(model, "Control Window",viewModel);
	        cw.go();
	        
	        if(model.isVolume()){
	        
	        	
	        System.out.println("adding projections");
			viewModel.addViewWindow(new MaxProjectionX<IT>(model, viewModel),initZoom);
			viewModel.addViewWindow(new MaxProjectionY<IT>(model, viewModel),initZoom);
			viewModel.addViewWindow(new MaxProjectionZ<IT>(model, viewModel),initZoom);
	        }
	        MainWindow<IT> mw=new MainWindow<IT>(imp, model, viewModel);
	        viewModel.addViewWindow(mw,initZoom);
	
			viewModel.addViewWindow(new KymographY<IT>(model, null,viewModel,mw),initZoom);		
			viewModel.addViewWindow(new KymographX<IT>(model, null,viewModel,mw),initZoom);		
		
	        viewModel.addViewWindow(cw,initZoom);
			
	        viewModel.update(model, null);
	    }
	}
	
	@Override
	
	public void run(String arg0) {
		super.run(arg0);
		
		long time0= System.nanoTime();
		ImagePlus imp=IJ.getImage();
		FileInfo fi= imp.getOriginalFileInfo();
		
		
		System.out.println(fi.directory);
		System.out.println(fi.fileName);
		
		
		System.out.println("creating Model...");
		Model< IT> model= new Model<IT>(imp);
				System.out.println("creating Controller...");
					
		
		Controller<IT> cont= new Controller<IT>(model);
		cont.addPolicy(new BlobPolicy<IT>());
		System.out.println("creating ViewModel...");
		ViewModel<IT> vm= new ViewModel<IT>( model,cont);
		System.out.println("done!");
		long time1= System.nanoTime();
		model.addObserver(vm);
		System.out.println("Time taken:"+((time1-time0)/1000000));
		
		
		AddingViewsThread awt= new AddingViewsThread(vm,model, imp);
		cont.load(vm);
		awt.run();
//		for(int i=0; i< 100000000;i++)
//			vm.setPosition(3, i%20);
		
		

		
		
		
	}

}
