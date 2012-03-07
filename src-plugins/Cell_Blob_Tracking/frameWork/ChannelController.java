package frameWork;


import java.awt.event.MouseEvent;
import java.util.List;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class ChannelController<T extends Trackable,  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > implements Runnable {
	protected Model<IT> model;
	public int selectedSequenceId;
	protected T selectedTrackable;
	protected TrackingChannel<T,IT> trackingChannel;
	public abstract void click(long[] pos, MouseEvent e);
	private int trackingFrame=0;
	public void optimizeFrame(int frameNumber){
		trackingChannel.optimizeFrame(frameNumber,false);
		model.makeChangesPublic();
	}
	
	protected ChannelController( Model<IT> mod,TrackingChannel<T,IT> tc ){
		model =mod;
		trackingChannel=tc;
	}
	
	@Override
	public void run(){
		while(trackingFrame<trackingChannel.getNumberOfFrames()){
	//		optimizeFrame( trackingFrame);
			List<T> newTrackables= trackingChannel.getFrame(trackingFrame).cloneTrackablesForFrame(trackingFrame+1);
			
			for(T t: newTrackables){
				
				
				trackingChannel.addTrackable(t);
			}
			
			trackingFrame++;
		}
	}

	protected void startTracking(int frameId){
		trackingFrame=frameId;
		Thread thread= new Thread(this);
		thread.start();
	}
	
	
	
}
