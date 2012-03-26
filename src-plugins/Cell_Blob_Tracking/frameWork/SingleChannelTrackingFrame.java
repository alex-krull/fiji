package frameWork;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class SingleChannelTrackingFrame<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends TrackingFrame<T,IT>{

	protected MovieFrame<IT> movieFrame;
	protected SingleChannelTrackingFrame(int frameNum, MovieFrame<IT> mv) {
		super(frameNum);
		movieFrame=mv;
	}

}
