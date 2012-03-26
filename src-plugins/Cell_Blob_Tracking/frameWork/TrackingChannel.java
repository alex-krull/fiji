package frameWork;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class TrackingChannel<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> {
	
	
	private SortedMap <Integer, Sequence<T>> Sequences;
	private	List<TrackingFrame <T,IT>> frames;
	private final int id;
	private long numOfFrames;
	private String label;
	protected Policy<T,IT> policy;
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	protected TrackingChannel(int newID, Policy<T,IT> pol){
		policy=pol;
		id=newID;
		label="session-"+String.valueOf(id);
	}
	
	public int getId(){
		return id;
	}
	
	
	protected void initialize(long numberOfFrames){
		numOfFrames=numberOfFrames;		
		Sequences= new TreeMap<Integer, Sequence<T>>();
		frames=new ArrayList<TrackingFrame<T,IT>>();
		for(int i=0;i<numOfFrames;i++){
			frames.add(produceFrame(i));
			System.out.println("producing frame:"+ i);
		}
		
	}
	
	public List<T> getTrackablesForFrame(int frame){
		System.out.println("frame:" + frame + "  nOfF:"+ frames.size());
		return frames.get(frame).getTrackables();
	}

	public T getTrackable(int seqId, int frameId){
		Sequence<T> sequence= Sequences.get(seqId);	
		if (sequence==null){
			System.out.println("sequence==null");
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

	public void optimizeFrame(int frameId, boolean cheap){
		TrackingFrame<T,IT> f= this.getFrame(frameId);
		f.optimizeFrame(cheap);
	}

	public int selectAt(int x, int y, int z, int frameId, int channel){
		TrackingFrame<T,IT> f= frames.get(frameId);
		return f.selectAt(x, y, z);
	}

	public void addTrackable(T trackable){
		if(trackable.frameId>=this.getNumberOfFrames()) return;
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
	
	public TrackingFrame<T,IT> getFrame(int frameNumber) {	
		System.out.println("size:"+ frames.size()+ "   fn:"+frameNumber);
		return frames.get(frameNumber);
	}
	
	public Sequence<T> splitSequenence(int SequenceId, int newSequqenceId,int frameNumber){
		System.out.println("                         splitting sequqnce");
		Sequence<T> s= Sequences.get(SequenceId);
		if(s!=null){
			Sequence<T> newS= s.splitSequence(frameNumber, produceSequence( newSequqenceId, Integer.toString(newSequqenceId)));		
			if(newS!=null)	this.Sequences.put(newS.getId(), newS);
			return newS;
		}
		return null;
	}
	

	
	public void deleteSequence(int SequenceId){
		Sequence<T> s= Sequences.get(SequenceId);
		if(s!=null) {
			for(int i=s.getFirstFrame();i<=s.getLastFrame();i++ ){
			frames.get(i).removeTrackable(SequenceId);
			}
			Sequences.remove(s.getId());
		}

	}
	
	public Properties getProperties(){
		Properties props= new Properties();
		props.setProperty("sessionProp1", "test1");
		props.setProperty("sessionProp2", "test2");
		props.setProperty("sessionId:",String.valueOf(getId()));
		return props;
	}
	
	public String getTypeName(){
		return produceSequence(-1,"").getTypeName();
	}
	
	protected Sequence<T> produceSequence(int ident, String lab){
		return policy.produceSequence(ident, lab);
	}
	
	protected abstract TrackingFrame<T,IT> produceFrame(int frameNum);
	
	protected abstract boolean isAssociatedWithMovieChannel(int id);
	public abstract T loadTrackableFromString(String s);
		
	
	
}
