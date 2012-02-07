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
	SingleImageWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img, String title,  ViewModel<T,IT> vm){
		super(mod, img ,title, vm,null);
		imp=ImageJFunctions.show(image,caption);
	}



	@Override
	public void rePaint(long[] position, boolean rePaintImage) {	
		toDraw=image;
    	super.reDraw(position, rePaintImage);
	}



	

	
}
