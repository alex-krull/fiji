package frameWork;


import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import tools.OtherTools;

public class ChannelController<T extends Trackable,  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected Model<IT> model;
	protected int selectedSequenceId;
	protected T selectedTrackable;
	protected Session<T,IT> trackingChannel;
	protected List <Integer> selectedIdList;
	protected Policy <T,IT> policy;
	
	public void click(long[] pos, MouseEvent e){
		policy.click(pos, e, model, selectedIdList, trackingChannel);
		return;
	}
	
	private boolean currentlyTracking=false;
	
	public void setSelectionList(List <Integer> selectedIds){
		if(selectedIds.size()>0)
			selectedSequenceId=selectedIds.get(0);
		else
			selectedSequenceId=-1;
		selectedIdList=selectedIds;
	}
	
	public void optimizeFrame(int frameNumber){
		trackingChannel.optimizeFrame(frameNumber,false);
		model.makeChangesPublic(frameNumber);
	}
	
//	protected int getSelectedSeqId(){
//		return selectedSequenceId;
//	}
	
	public ChannelController( Model<IT> mod,Session<T,IT> tc, Policy<T,IT> pol ){
		selectedIdList=new ArrayList<Integer>();
		model =mod;
		trackingChannel=tc;
		policy=pol;
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
				if(!currentlyTracking) break;
				
				List<T> newTrackables= trackingChannel.getFrame(i).cloneTrackablesForFrame(i+1);
				
				for(T t: newTrackables){
					
					
					trackingChannel.addTrackable(t);
				}
				
				
				
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
	
	public void splitSequnce(int frameNumber){
		for(Integer seqId: this.selectedIdList){
			trackingChannel.splitSequenence(seqId, model.getNextSequqnceId(), frameNumber);
		}
		
		
		model.makeChangesPublic();
	}
	
	public void deleteSequence(){
		for(Integer seqId: this.selectedIdList){
			trackingChannel.deleteSequence(seqId);
		}
		selectedIdList.clear();
		
		selectedSequenceId=-1;
		model.makeChangesPublic();
	}
	
	public void trimSequence(int frameNumber){
		for(Integer seqId: this.selectedIdList){
			
			trackingChannel.splitSequenence(seqId, -1, frameNumber);
			trackingChannel.deleteSequence(-1);
		}
		model.makeChangesPublic();
	}
	
	
	public void mergeSequenences(){
		int newSid=model.getNextSequqnceId();
		for(Integer sid: this.selectedIdList){
			Sequence<T> s=trackingChannel.getSequence(sid);
			for(int i= s.getFirstFrame();i<=s.getLastFrame();i++){
				
				TrackingFrame<T,IT> tf= trackingChannel.getFrame(0);
				T t=tf.copy( s.getTrackableForFrame(i) );
				t.sequenceId=newSid;
				trackingChannel.addTrackable(t);
				System.out.println("adding frame:"+ t.frameId);
			}		
		}
		model.makeChangesPublic();	
	}
	
	public void setColor(Color color){
		for(Integer i: this.selectedIdList){
		Sequence<T>seq=trackingChannel.getSequence(i);
		if(seq!=null) seq.setColor(color);
		}
	}
	
	public boolean isSelected(int sId){
		return this.selectedIdList.contains(sId);
	}
	
	public void processLineFromFile(String line){
		trackingChannel.addTrackable(policy.loadTrackableFromString(line, this.getId()));
	}
	
	public void saveSequence(Sequence<T> seq){
		
		try {
			FileWriter fileWriter= new FileWriter(model.getProjectDirectory()+"/seq"+seq.getId()+".trcT");
			fileWriter.write("%-session properties-\n");
			OtherTools.writeProperties(fileWriter, trackingChannel.getProperties());	
			
			
			
			seq.writeToFile(fileWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveAll(){
		
		for(Sequence<T> seq: trackingChannel.getSeqsCollection()){
			saveSequence(seq);
		}
	}
	
	public boolean CheckOrCreateSequence(Properties seqProps){
		int id=Integer.valueOf(seqProps.getProperty("seqId"));
		Sequence<T> seq= trackingChannel.getSequence(id);
		if (seq==null){
			seq=trackingChannel.produceSequence(id, "");
			trackingChannel.addSequence(seq);
		}
		seq.setProperties(seqProps);
		return true;
	}
	
	public int getId(){
		return trackingChannel.getId();
	}
	
	
	
	
}
