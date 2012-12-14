package frameWork;


import ij.gui.GenericDialog;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import blobTracking.Blob;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import tools.OtherTools;
import frameWork.gui.ViewModel;
import frameWork.gui.controlWindow.ControlWindow;

public class ChannelController<T extends Trackable,  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected Model<IT> model;
	protected int selectedSequenceId;
	protected T selectedTrackable;
	protected Session<T,IT> trackingChannel;
	protected List <Integer> selectedIdList;
	protected Policy <T,IT> policy;
	protected ControlWindow<IT> controllWindow;
	private Random rand;
	
	public String getLabel(){
		return trackingChannel.getLabel();
	}
	
	public void click(long[] pos, MouseEvent e, ViewModel<IT> vm){
		try{
		if (model.isCurrentlyTracking()) return;
		selectedSequenceId= policy.click(pos, e, model, selectedIdList, trackingChannel, selectedSequenceId, vm);
		}catch(Exception ex){
			ex.printStackTrace(Model.errorWriter);
			Model.errorWriter.flush();
		}
		
		return;
	}	
	
	public List <Integer> getSelectionList(){
		return selectedIdList;
	}
	
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
	
	
//	protected int getSelectedSeqId(){
//		return selectedSequenceId;
//	}
	
	public ChannelController( Model<IT> mod,Session<T,IT> tc, Policy<T,IT> pol ){
		rand = new SecureRandom();
		selectedIdList=new ArrayList<Integer>();
		model =mod;
		trackingChannel=tc;
		policy=pol;
	}
	
	public List<T> getSelectedTrackables(int frameNumber){
		List<T> results= new ArrayList<T>();
		for(Integer i:this.selectedIdList){
			results.add(trackingChannel.getTrackable(i, frameNumber));
		}
		return results;
	}

	
	
	private class TrackingThread extends Thread{
		private final int startingFrame;
		private final int stoppingFrame;
		private final boolean multiscale;
		private final boolean autoS;
		TrackingThread(int frameToStart, boolean multi, boolean autosave, int frameToStop){
			stoppingFrame=frameToStop;
			startingFrame=frameToStart;
			multiscale=multi;
			autoS=autosave;
		}
		
		@Override
		public void run(){
			try{
			
			
			
			synchronized (trackingChannel){
			
				List<T> trackingCandidates=	getSelectedTrackables( startingFrame );
				if(trackingCandidates.isEmpty()) return;
				
				Model.getInstance().setCurrentlyTracking(true);
					
			
			for(int i= startingFrame; i<trackingChannel.getNumberOfFrames()&& i<=stoppingFrame;i++){
				
				
			//	Model.getInstance().rwLock.writeLock().lock();
			// newTrackables=trackingChannel.getTrackablesForFrame(i);
				
			 if(i!=startingFrame){
				 trackingCandidates.clear();
				 List<T> newTrackables= trackingChannel.getFrame(i-1).cloneTrackablesForFrame(i);		
				 for(T t: newTrackables){
						
					if(selectedIdList.contains( t.sequenceId))
							trackingCandidates.add(t);
					}
			 }
				
			
				for(T t: trackingCandidates){
					Blob b = (Blob)t;
	//				b.xPos=0.5*rand.nextGaussian()+(double)(model.getFrame(0, 0).getFrameView().dimension(0)-1)/2.0;
//		
	//				b.yPos=0.5*rand.nextGaussian()+(double)(model.getFrame(0, 0).getFrameView().dimension(1)-1)/2.0;
//					b.pK=(0.5/(double)trackingCandidates.size());
//					b.pK=rand.nextDouble();
				}
			 
				
		//			Model.getInstance().rwLock.writeLock().unlock();
			 if(i%1==0)System.out.println("frame:"+i);
				policy.optimizeFrame(multiscale, trackingCandidates, trackingChannel.getFrame(i).getMovieFrame(),
						trackingChannel.qualityThreshold, trackingChannel);
			
				Model.getInstance().rwLock.writeLock().lock();
				
				
				for(T t: trackingCandidates){
					trackingChannel.addTrackable(t);
				}
				
				Model.getInstance().makeStructuralChange();
				Model.getInstance().makeChangesPublic(Math.max(i,startingFrame));
				Model.getInstance().rwLock.writeLock().unlock();
				
		//		optimizeFrame( i, multiscale);
				
		//		System.out.println("trackingFrame:"+i);
				
				
				if(autoS) saveAll();
				
				if(!Model.getInstance().isCurrentlyTracking()){			
					break;
				}
				
				
			//	model.makeStructuralChange();
			//	model.makeChangesPublic();
				
				
				
				
				
			}
			
			
			
			Model.getInstance().setCurrentlyTracking(false);
			}
			
		}catch(Exception e){
			e.printStackTrace(Model.errorWriter);
			Model.errorWriter.flush();
		}
		
	}
	}
	

	public void startTracking(int frameId, boolean multiscale, boolean autosave, int lastFrame){
		Thread thread= new TrackingThread(frameId, multiscale, autosave, lastFrame);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public void startTrackingSingleThread(int frameId, boolean multiscale, boolean autosave, int lastFrame){
		Thread thread= new TrackingThread(frameId, multiscale, autosave, lastFrame);
		thread.run();
	}
	
	public void stopTracking(){
	
		Model.getInstance().setCurrentlyTracking(false);
		
	}
	

	
	public void splitSequnce(int frameNumber){
		
		for(Integer seqId: this.selectedIdList){			
			
			trackingChannel.splitSequenence(seqId, model.getNextSequqnceId(), frameNumber);
		}
		
		
		
	}
	
	public void deleteSequence(){
		if(selectedIdList.isEmpty()) return;
		GenericDialog gd = new GenericDialog("delete files?");
		gd.addMessage("Should the files be deleted as well?");
		gd.enableYesNoCancel("Yes", "No");
		gd.showDialog();
		if (gd.wasCanceled()) return;
		
		
		for(Integer seqId: this.selectedIdList){
			if(gd.wasOKed()){
				
				
				File toDel= new File(model.getProjectDirectory()+"/"+trackingChannel.getSequence(seqId).getPath());
				
				if(toDel.delete()){
					Model.getInstance().depositMsg("deleted file:"+ toDel.getPath());
				}else{
					Model.getInstance().depositMsg("deletion failed:"+ toDel.getPath());
				}
				Model.getInstance().makeChangesPublic();
			}
			
			Model.getInstance().depositMsg("deleting sequence "+trackingChannel.getSequence(seqId).getLabel());
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
				
			}		
		}
		trackingChannel.getSequence(newSid).setLabel("merge");
		
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
	
	public void saveSequence(Sequence<T> seq) throws IOException{
		
		model.rwLock.writeLock().lock();
		File toDel= new File(model.getProjectDirectory()+"/"+seq.getPath());
		
		if(toDel.delete()){
			Model.getInstance().depositMsg("deleted file:"+ toDel.getPath());
		}else{
			Model.getInstance().depositMsg("deletion failed:"+ toDel.getPath());
		}
		
		seq.createFileName();
		model.rwLock.writeLock().unlock();
			
			FileWriter fileWriter= new FileWriter(model.getProjectDirectory()+seq.getPath());
			fileWriter.write("%-global properties-\n");
			OtherTools.writeProperties(fileWriter, Model.getInstance().getProperties());
			
			fileWriter.write("%-session properties-\n");
			OtherTools.writeProperties(fileWriter, trackingChannel.getProperties());	
			
			
			
			seq.writeToFile(fileWriter);
		
		
		Model.getInstance().depositMsg("saved file:"+ model.getProjectDirectory()+seq.getPath());
		Model.getInstance().makeChangesPublic();
	}
	
	public void saveAll() throws IOException{
		
		for(Sequence<T> seq: trackingChannel.getSeqsCollection()){
			saveSequence(seq);
		}
	}
	
	public boolean CheckOrCreateSequence(Properties seqProps, String filePath){
		int id=Integer.valueOf(seqProps.getProperty("seqId"));
		Sequence<T> seq= trackingChannel.getSequence(id);
		if (seq==null){
			seq=policy.produceSequence(id, "init", trackingChannel, filePath);
		//	seq=trackingChannel.produceSequence(id, "");
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
	//	for(Sequence <T> seq: trackingChannel.getSeqsCollection()){
	//		if(newLabel.equals(seq.getLabel())) return;
	//	}
		Sequence<T> s= this.trackingChannel.getSequence(id);
		if(s!=null) s.setLabel(newLabel);
		
	}
	
	public void showObjectOptions(int frameNumber){
		List<T> list= trackingChannel.getTrackablesForFrame(frameNumber);
		T first=null;
		for(T t: list){
			if(this.selectedIdList.contains(t.sequenceId)){
				if(first==null){
					first=t;
					trackingChannel.showObjectPropertiesDialog(first);
					continue;
				}
				policy.copyOptions(first, t);
				
			}
			
		}
	}
	
	
	
}
