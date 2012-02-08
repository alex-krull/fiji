package frameWork;

import ij.gui.Overlay;
import ij.gui.Roi;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public abstract class Sequence<T extends Trackable> {
	protected int id;
	protected String label;
	protected SortedMap <Integer,T> trackables;
	protected List<List<T>> pieces;
	protected Color color;
	
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
	
	
	
	public abstract void getKymoOverlayX(Overlay ov, double scaleX, double scaleY);
	public abstract void getKymoOverlayY(Overlay ov, double scaleX, double scaleY);
	

}
