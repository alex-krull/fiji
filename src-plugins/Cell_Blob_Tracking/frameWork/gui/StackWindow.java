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

public class StackWindow <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<T,IT>{

	
	protected int stackDimension;
	
	
	public StackWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img, String title, int sDim,  ViewModel<T,IT> vm){
		super(mod, img ,title, vm);
		stackDimension=sDim;
		
		RandomAccessibleInterval<IT> imgSlice=Views.hyperSlice(image,2,0);
		imp=ImageJFunctions.show(imgSlice,caption);
	}
	
	
	public void rePaint(long [] position){
		
		long frameNumber= position[stackDimension];
		toDraw=Views.hyperSlice(image,2,frameNumber);
	
    	super.rePaint();
    	
	}
	
}
