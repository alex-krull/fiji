package frameWork;


import ij.IJ;
import ij.gui.GenericDialog;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import tools.OtherTools;
import frameWork.gui.controlWindow.ControlWindow;

public class ChannelController<T extends Trackable,  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected Model<IT> model;
	protected int selectedSequenceId;
	protected T selectedTrackable;
	protected Session<T,IT> trackingChannel;
	protected List <Integer> selectedIdList;
	protected Policy <T,IT> policy;
	protected ControlWindow<IT> controllWindow;
	
	public void click(long[] pos, MouseEvent e){
		selectedSequenceId= policy.click(pos, e, model, selectedIdList, trackingChannel, selectedSequenceId);
		return;
	}
	
	private boolean currentlyTracking=false;
	
	public void setSelectionList(List <Integer> selectedIds){
		if(selectedIds.size()>0)
			selectedSequenceId=selectedIds.get(0);
		else
			selectedSequenceId=-1;
		selectedIdList.clear();
		for(Integer i: selectedIds){
			if(trackingChannel.getSeqs().keySet().contains(i)) selectedIdList.add(i);
		}
	}
	
	public void optimizeFrame(int frameNumber){
		trackingChannel.optimizeFrame(frameNumber,false, selectedIdList);
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
		//		System.out.println("trackingFrame:"+i);
				optimizeFrame( i);
				if(!currentlyTracking) break;
				
				List<T> newTrackables= trackingChannel.getFrame(i).cloneTrackablesForFrame(i+1);
				
				for(T t: newTrackables){
					
					if(selectedIdList.contains( t.sequenceId))
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
		
		
		
	}
	
	public void deleteSequence(){
		if(selectedIdList.isEmpty()) return;
		GenericDialog gd = new GenericDialog("delete files?");
		gd.addMessage("Shuold the files be deleted as well?");
		gd.enableYesNoCancel("Yes", "No");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		
		for(Integer seqId: this.selectedIdList){
			if(gd.wasOKed()){
				
				File toDel= new File(model.getProjectDirectory()+"/"+trackingChannel.getSequence(seqId).getPath());
				IJ.error(toDel.getPath());
				toDel.delete();		
			}
			trackingChannel.deleteSequence(seqId);
			
		}
		selectedIdList.clear();
		
		selectedSequenceId=-1;
		
		
		
	}
	
	public void trimSequence(int frameNumber){
		for(Integer seqId: this.selectedIdList){
			
			trackingChannel.splitSequenence(seqId, -1, frameNumber);
			trackingChannel.deleteSequence(-1);
		}
		
	}
	
	
	public void mergeSequenences(){
	
		int newSid=model.getNextSequqnceId();
		for(Integer sid: this.selectedIdList){
			Sequence<T> s=trackingChannel.getSequence(sid);
			for(int i= s.getFirstFrame();i<=s.getLastFrame();i++){
				
				TrackingFrame<T,IT> tf= trackingChannel.getFrame(0);
				T t=policy.copy( s.getTrackableForFrame(i) );
				t.sequenceId=newSid;
				trackingChannel.addTrackable(t);
	//			System.out.println("adding frame:"+ t.frameId);
			}		
		}
		
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
			FileWriter fileWriter= new FileWriter(model.getProjectDirectory()+"/"+seq.getPath());
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
	
	public void deleteAllSequences(){
		this.selectedIdList.clear();
		for(Sequence <T> seq: trackingChannel.getSeqsCollection()){
			selectedIdList.add(seq.getId());
		}
		this.deleteSequence();
		
	}
	
	public void setSequenceLabel(int id, String newLabel){
		Sequence<T> s= this.trackingChannel.getSequence(id);
		if(s!=null) s.setLabel(newLabel);
		
	}
	
	
	
}
