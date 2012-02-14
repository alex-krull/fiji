package frameWork;


import ij.gui.Overlay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class TrackingChannel<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> {
	
	
	private SortedMap <Integer, Sequence<T>> Sequences;
	private	List<TrackingFrame <T,IT>> frames;
	private long numOfFrames;
	
	protected void initialize(long numberOfFrames){
		numOfFrames=numberOfFrames;		
		Sequences= new TreeMap<Integer, Sequence<T>>();
		frames=new ArrayList<TrackingFrame<T,IT>>();
		for(int i=0;i<numOfFrames;i++){
			frames.add(produceFrame(i));
		}
		
	}
	
	public List<T> getTrackablesForFrame(int frame){
		System.out.println("frame:" + frame + "  nOfF:"+ frames.size());
		return frames.get(frame).getTrackables();
	}

	public T getTrackable(int seqId, int frameId){
		Sequence<T> sequence= Sequences.get(seqId);	
		if (sequence==null){
			
			return null;
		}
		return sequence.getTrackableForFrame(frameId);
		
	}
	
	public SortedMap <Integer,? extends Sequence< T>> getSeqs(){
		return Sequences;
	}
	
	public Collection <? extends Sequence<T>> getSeqsCollection(){
		
		return Sequences.values();
	}

	public Sequence<T> getSequence(int id){
		return Sequences.get(id);
	}

	public void optimizeFrame(int frameId){
		TrackingFrame<T,IT> f= frames.get(frameId);
		f.optimizeFrame();
	}

	public int selectAt(int x, int y, int z, int frameId, int channel){
		TrackingFrame<T,IT> f= frames.get(frameId);
		return f.selectAt(x, y, z);
	}

	public void addTrackable(T trackable){
		
		frames.get(trackable.frameId).addTrackable(trackable);
		Sequence<T> sequence= Sequences.get(trackable.sequenceId);
		if(sequence==null){
			
			sequence=produceSequence(trackable.sequenceId, Integer.toString(trackable.sequenceId));
			System.out.println("Adding Seq!");
			Sequences.put(trackable.sequenceId, sequence);
		}
		sequence.addTrackable(trackable);
	}
	
	public long getNumberOfFrames() {
		
		return numOfFrames;
	}
	
	public void addKymoOverlaysX(Overlay ov, double scaleX, double scaleY){
		for(int i=Sequences.firstKey();i<=Sequences.lastKey();i++){
			Sequence<T> seq = Sequences.get(i);
			if(seq!=null) seq.getKymoOverlayX(ov,scaleX,scaleY);
		}
	}
		
	protected abstract TrackingFrame<T,IT> produceFrame(int frameNum);
	protected abstract Sequence<T> produceSequence(int ident, String lab);
	protected abstract boolean isAssociatedWithMovieChannel(int id);
	
	
}
