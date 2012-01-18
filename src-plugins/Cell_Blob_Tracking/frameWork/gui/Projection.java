package frameWork.gui;

import frameWork.Model;
import frameWork.Trackable;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.plugin.ContrastEnhancer;

public class Projection <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<T,IT>{

	protected ImagePlus imp;
	protected ImageCanvas canvas;
	protected RandomAccessibleInterval<IT> image;
	
	
	public Projection(Model<T,IT> mod, RandomAccessibleInterval<IT> img){
		super(mod);
		image=img;
		
		RandomAccessibleInterval<IT> imgSlice=Views.hyperSlice(image,2,0);
		imp=ImageJFunctions.show(imgSlice," ");
	}
	
	
	public void rePaint(int frameNumber){
		RandomAccessibleInterval<IT> imgSlice=Views.hyperSlice(image,2,frameNumber);
		
		ImagePlus impl=ImageJFunctions.wrap( imgSlice , " ");
	    ContrastEnhancer ce= new ContrastEnhancer();
    	ce.stretchHistogram(impl.getProcessor(), 0.5); 
    	
    	this.imp.setProcessor(impl.getProcessor());
    	imp.updateAndDraw();
	}
	
}
