package frameWork;

import frameWork.gui.ViewModel;
import ij.gui.Overlay;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class Policy<T extends Trackable, IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >  {
	/*public abstract String getTypeName();
	public abstract void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected, Sequence<T> seq);
	public abstract void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected, Sequence<T> seq);
	
	
	protected abstract boolean isAssociatedWithMovieChannel(int id);
	public abstract T loadTrackableFromString(String s);
	public abstract T copy(T toCopy);*/
	
	public abstract ChannelController<T,IT> produceControllerAndChannel(Properties sessionProps, Model <IT> model);
	public abstract String getTypeName();
	
	protected abstract TrackingFrame<T,IT> produceFrame(int frameNum, MovieChannel<IT> mc);
	
	public abstract T loadTrackableFromString(String s, int sessionId);
	
	public abstract void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected,
			SortedMap <Integer,T> trackables, Color color);
	public abstract void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected,
			SortedMap <Integer,T> trackables, Color color);
	
	public abstract int click(long[] pos, MouseEvent e, Model<IT> model, List<Integer>  selectedIdList, Session<T,IT> trackingChannel, int selectedSequenceId, ViewModel <IT> vm);

	public abstract T copy(T toCopy);
	
	public abstract void  optimizeFrame(boolean alternateMethod, List<T> trackables,
			MovieFrame<IT> movieFrame, double qualityT, Session<T,IT> session);
	public Sequence<T> produceSequence(int ident, String lab,
			Session<T, IT> session, String filePath) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getLabelForAlternateTracking(){
		return null;
	}
	
	public boolean isHidden(){
		return false;
	}
	
	public abstract void copyOptions(T src, T dst);
	
}
