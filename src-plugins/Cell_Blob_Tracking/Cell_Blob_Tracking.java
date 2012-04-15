

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import tools.ImglibTools;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import blobTracking.MaximumLikelihoodBlobPolicy;
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

import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;

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
	
	public String getToolIcon(){
		return "CeffD60CbbbD70C9aaD80CdeeD90"
				+ "CbccD41C899D51C566D61C455L7181C566L91a1C999Db1"
				+ "C99aD32C566L4252C677D62C466D72C456D82C677L92a2C566Db2C677Dc2CeeeDd2"
				+ "C9aaD23C566D33C677D43CdddD53CcccD63C566L7383CabbD93C999Db3C566Lc3d3CeeeDe3"
				+ "CbccD14C566D24C677D34CcccD64C566L7484CabbD94CaabDc4C566Dd4C899De4"
				+ "D15C566D25CcddD35CcccD65C566L7585CabbD95C788Dd5C566De5CcccDf5"
				+ "CeefD06C566D16C677D26CdddL3656CaaaD66C566L7686C899D96CdddLa6c6CaaaDd6C566De6C9aaDf6"
				+ "CcccD07C455D17C456D27C566L3767C455L7787C466D97C566La7d7C455De7C677Df7"
				+ "CbccD08C456L1828C566L3868C455L7888C466D98C566La8d8C455De8C677Df8"
				+ "CdddD09C566D19C677D29CbbbL3959C899D69C566D79C466D89C788D99CbbbLa9c9C9aaDd9C566De9C899Df9"
				+ "C566D1aC567D2aCcccD6aC566L7a8aCabbD9aC9aaDdaC566DeaCbbbDfa"
				+ "C9aaD1bC566D2bC999D3bCcccD6bC566L7b8bCabbD9bCeeeDcbC566LdbebCeefDfb"
				+ "C677D2cC566D3cC9aaD4cCcccD6cC566L7c8cCabbD9cCdeeDbcC677DccC566DdcCaaaDec"
				+ "CeeeD2dC567D3dC566D4dC677D5dC899D6dC566L7d8dC899D9dC9aaDadC566LbdcdC99aDdd"
				+ "CeefD3eC899D4eC566L5e6eC455L7e8eC566L9eaeC677DbeCabbDce"
				+ "CdddD5fCaabD6fC677L7f8fC899D9fCbbcDaf"

;
		
	}
	@Override
	
	public void run(String arg0) {
		super.run(arg0);
		
		long time0= System.nanoTime();
		ImagePlus imp=IJ.getImage();
	//	Img <IT> img = ImagePlusAdapter.wrap(imp);	
	//	ImglibTools.generatePyramid(img, 1,1,1);
		
		FileInfo fi= imp.getOriginalFileInfo();
		
		
		System.out.println(fi.directory);
		System.out.println(fi.fileName);
		
		
		System.out.println("creating Model...");
		Model< IT> model= new Model<IT>(imp);
				System.out.println("creating Controller...");
					
		
		Controller<IT> cont= new Controller<IT>(model);
		cont.addPolicy(new MaximumLikelihoodBlobPolicy<IT>());
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
