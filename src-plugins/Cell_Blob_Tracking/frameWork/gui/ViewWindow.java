package frameWork.gui;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public abstract class ViewWindow <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected Model<T,IT> model;
	protected String caption;
	protected ViewModel<T,IT> viewModel;
	protected ViewWindow(Model<T,IT> mod, String title, ViewModel<T,IT> vm){
		viewModel=vm;
		model=mod;
		caption= title;
		
	}
	
	public abstract void rePaint(long[] position);
	
}
