package frameWork.gui;

import ij.ImagePlus;
import ij.plugin.ContrastEnhancer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class SingleImageWindow <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<T,IT>{
	SingleImageWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img, String title){
		super(mod, img ,title);
		imp=ImageJFunctions.show(image,caption);
	}



	@Override
	public void rePaint(long[] position) {
		
		this.clearOverlay();
		ImagePlus impl=ImageJFunctions.wrap( image , caption);
	    ContrastEnhancer ce= new ContrastEnhancer();
    	ce.stretchHistogram(impl.getProcessor(), 0.5); 
    	
    	this.imp.setProcessor(impl.getProcessor());
    	//imp.updateAndDraw();
	}
	
}
