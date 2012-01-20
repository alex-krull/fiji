package frameWork.gui;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class KymographY <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<T,IT>{

	public KymographY(Model<T, IT> mod, RandomAccessibleInterval<IT> img) {
		super(mod, img);
		// TODO Auto-generated constructor stub
	}
	
	public void rePaint(long[] position){
		super.rePaint(position);
		this.addYLineOverlay(position[3]);
	}

}