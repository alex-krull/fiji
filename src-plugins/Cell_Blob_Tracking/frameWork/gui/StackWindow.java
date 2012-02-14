package frameWork.gui;

import frameWork.Model;

import net.imglib2.RandomAccessibleInterval;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

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
