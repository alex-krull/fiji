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

public class StackWindow <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<IT>{

	
	protected int stackDimension;
	
	
	public StackWindow(Model<IT> mod, RandomAccessibleInterval<IT> img, String title, int sDim,  ViewModel<IT> vm){
		super(mod, img ,title, vm,null);
		stackDimension=sDim;
		
		toDraw=Views.hyperSlice(image,2,0);
		
	}
	
	
	public void rePaint(long[] position, boolean rePaintImage){
		
		
		
    	
    	
	}
	
}
