package frameWork.gui;

import tools.ImglibTools;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class KymoWindow <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends SingleImageWindow<T,IT>  {

	public KymoWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<T,IT> vm){
		super(mod, img, "kymograph", vm);
	}
	
	public void rePaint(long[] position){
		super.rePaint(position);
	}
	
}
