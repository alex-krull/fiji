package frameWork;

import ij.gui.Overlay;
import ij.gui.Roi;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Sequence<T extends Trackable> {
	protected int id; 
	protected String label;
	protected SortedMap <Integer,T> trackables;
	protected List<List<T>> pieces;
	
	public Sequence(int ident, String lab){
		id = ident;
		label=lab;
		trackables=new TreeMap<Integer,T>();
	//	pieces= getPieces();
	}
	
	public void addTrackable(T trackable){
		trackables.put(trackable.frameId, trackable);
	//	pieces= getPieces();
		
	}
	
	public T getTrackableForFrame(int frameNumber){
		return trackables.get(frameNumber);
	}
	
	public abstract void getKymoOverlayX(Overlay ov, double scaleX, double scaleY);
	public abstract void getKymoOverlayY(Overlay ov, double scaleX, double scaleY);
	
	private List<List<T>> getPieces(){
		List<List<T>> result =new ArrayList <List<T>>();
		
		List<T> localList=null;
		for(int i=trackables.firstKey();i<trackables.lastKey();i++){
			T currentT = trackables.get(i);
			if (currentT!=null){
				if(localList==null)	localList=new ArrayList<T>();			
				localList.add(currentT);
			}else{
				if(localList!=null) result.add(localList);
				localList=null;
			}
		}
		
		return result;
	}
}
