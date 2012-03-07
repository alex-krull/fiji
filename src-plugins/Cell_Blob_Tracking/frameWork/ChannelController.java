package frameWork;


import java.awt.event.MouseEvent;
import java.util.List;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class ChannelController<T extends Trackable,  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected Model<IT> model;
	public int selectedSequenceId;
	protected T selectedTrackable;
	protected TrackingChannel<T,IT> trackingChannel;
	public abstract void click(long[] pos, MouseEvent e);
	private boolean currentlyTracking=false;
	public void optimizeFrame(int frameNumber){
		trackingChannel.optimizeFrame(frameNumber,false);
		model.makeChangesPublic(frameNumber);
	}
	
	protected ChannelController( Model<IT> mod,TrackingChannel<T,IT> tc ){
		model =mod;
		trackingChannel=tc;
	}
	
	
	private class TrackingThread extends Thread{
		private final int startingFrame;
		TrackingThread(int frameToStart){
			startingFrame=frameToStart;
		}
		
		@Override
		public void run(){
			
			if(!currentlyTracking)	{
			synchronized (trackingChannel){
			
			currentlyTracking=true;	
			for(int i= startingFrame; i<trackingChannel.getNumberOfFrames();i++){
				System.out.println("trackingFrame:"+i);
				optimizeFrame( i);
				List<T> newTrackables= trackingChannel.getFrame(i).cloneTrackablesForFrame(i+1);
				
				for(T t: newTrackables){
					
					
					trackingChannel.addTrackable(t);
				}
				
				
				if(!currentlyTracking) break;
			}
			currentlyTracking=false;
			}
			}
		}
	}
	

	public void startTracking(int frameId){
		Thread thread= new TrackingThread(frameId);
		thread.start();
	}
	
	public void stopTracking(){
		currentlyTracking=false;
	}
	
	public boolean isTracking(){
		return currentlyTracking;
	}
	
	
	
	
	
}
