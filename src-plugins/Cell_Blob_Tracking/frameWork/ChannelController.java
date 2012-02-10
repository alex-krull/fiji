package frameWork;

import java.awt.event.MouseEvent;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class ChannelController<T extends Trackable,  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {
	protected Model<IT> model;
	public int selectedSequenceId;
	protected T selectedTrackable;
	protected TrackingChannel<T,IT> trackingChannel;
	public abstract void click(long[] pos, MouseEvent e);
	
	protected ChannelController( Model<IT> mod,TrackingChannel<T,IT> tc ){
		model =mod;
		trackingChannel=tc;
	}
	
}
