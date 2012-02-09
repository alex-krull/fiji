package frameWork;

import java.util.List;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public abstract class TrackingChannel<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Observable {
	
	
	private SortedMap <Integer, Sequence<T>> Sequences;
	private	List<Frame <T,IT>> frames;
	private Factory<T,IT> factory;
	
	public TrackingChannel(Factory <T,IT> fact){
		factory=fact;
		Sequences= new TreeMap<Integer, Sequence<T>>();
	}
	
	public List<T> getTrackablesForFrame(int frame){
		return frames.get(frame).getTrackables();
	}

	public T getTrackable(int seqId, int frameId){
		Sequence<T> sequence= Sequences.get(seqId);	
		if (sequence==null){
			
			return null;
		}
		return sequence.getTrackableForFrame(frameId);
		
	}
	
	public SortedMap <Integer, Sequence<T>> getSeqs(){
		return Sequences;
	}

	public Sequence<T> getSequence(int id){
		return Sequences.get(id);
	}

	public void optimizeFrame(int frameId){
		Frame<T,IT> f= frames.get(frameId);
		f.optimizeFrame();
	}

	public int selectAt(int x, int y, int z, int frameId, int channel){
		Frame<T,IT> f= frames.get(frameId);
		return f.selectAt(x, y, z);
	}

	public void addTrackable(T trackable){
		
		frames.get(trackable.frameId).addTrackable(trackable);
		Sequence<T> sequence= Sequences.get(trackable.sequenceId);
		if(sequence==null){
			
			sequence=factory.produceSequence(trackable.sequenceId, Integer.toString(trackable.sequenceId));
			System.out.println("Adding Seq!");
			Sequences.put(trackable.sequenceId, sequence);
		}
		sequence.addTrackable(trackable);
	}
	
	public void makeChangesPublic(){
		setChanged();
		notifyObservers();
	}
}
