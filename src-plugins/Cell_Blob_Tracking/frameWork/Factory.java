package frameWork;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public interface Factory <T extends Trackable,IT extends NumericType<IT> & NativeType<IT> & RealType<IT>>{
	public abstract Frame<T,IT> produceFrame(int frameNum);
	public abstract Sequence<T> produceSequence(int ident, String lab); 	
}
