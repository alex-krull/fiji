package frameWork;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Sequence<T extends Trackable> {
	private int id; 
	private String label;
	private SortedMap <Integer,T> trackables;
	
	public Sequence(int ident, String lab){
		id = ident;
		label=lab;
		trackables=new TreeMap<Integer,T>();
	}
	
	public void addTrackable(T trackable){
		trackables.put(trackable.frameId, trackable);
		
	}
	
	public T getTrackableForFrame(int frameNumber){
		return trackables.get(frameNumber);
	}
}
