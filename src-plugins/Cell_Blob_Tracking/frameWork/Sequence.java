package frameWork;

import ij.gui.Overlay;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Sequence<T extends Trackable> {
	protected int id;
	protected String label;
	protected SortedMap <Integer,T> trackables;
	protected List<List<T>> pieces;
	protected Color color;
	
	public int getFirstFrame(){
		return trackables.firstKey();
	}
	
	public int getLastFrame(){
		return trackables.lastKey();
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Sequence(int ident, String lab){
		color=new Color(255,0,0,255);
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
	
	public Sequence<T> splitSequence(int frameNumber, Sequence<T> secondPart){
		if(frameNumber>trackables.lastKey() || frameNumber<trackables.firstKey()||trackables.size()<=1)
			return null;
		for(int i=frameNumber;i<=trackables.lastKey();i++ ){
			T trackable = trackables.get(i);
			trackable.sequenceId=secondPart.getId();
			System.out.println("new id:"+ trackable.sequenceId);
			secondPart.addTrackable(trackable);
			trackables.remove(i);
			
		}
		return secondPart;
	}
	
	public abstract String getTypeName();
	
	public int getId(){
		return id;
	}
	
	public boolean writeToFile(String fName){
	try{
		FileWriter fileWriter= new FileWriter(fName);
		
		for(T trackable: trackables.values()){	
			fileWriter.write(trackable.toSaveString()+"\n");
		}
		
		fileWriter.flush();
	}catch(IOException e){
		return false;
	}
	
	
	
	return true;
	}
	
	
	public abstract void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected);
	public abstract void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected);
	

}
