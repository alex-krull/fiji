package frameWork;

import java.util.Properties;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class Policy<T extends Trackable, IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >  {
	/*public abstract String getTypeName();
	public abstract void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected, Sequence<T> seq);
	public abstract void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected, Sequence<T> seq);
	protected abstract TrackingFrame<T,IT> produceFrame(int frameNum);
	
	protected abstract boolean isAssociatedWithMovieChannel(int id);
	public abstract T loadTrackableFromString(String s);
	public abstract T copy(T toCopy);*/
	
	public abstract ChannelController<T,IT> produceControllerAndChannel(Properties sessionProps, Model <IT> model);
	public abstract String getTypeName();
	protected abstract Sequence<T> produceSequence(int ident, String lab);
}
