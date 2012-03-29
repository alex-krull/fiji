package frameWork;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class TrackingStrategy <T extends Trackable, IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	public abstract void optimizeFrame(TrackingFrame<T,IT> tf);
}
