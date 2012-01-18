package frameWork.gui;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class ViewWindow <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected ViewWindow(Model<T,IT> mod){
		model=mod;
	}
	Model<T,IT> model;
}
