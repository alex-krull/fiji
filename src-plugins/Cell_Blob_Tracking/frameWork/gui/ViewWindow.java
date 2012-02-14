package frameWork.gui;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;

public abstract class ViewWindow < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected Model<IT> model;
	protected String caption;
	protected ViewModel<IT> viewModel;
	protected ViewWindow(Model<IT> mod, String title, ViewModel<IT> vm){
		viewModel=vm;
		model=mod;
		caption= title;
		
	}
	
	public abstract void rePaint(long[] position, boolean rePaintImage);
	
}
