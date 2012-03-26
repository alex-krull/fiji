package frameWork;

import ij.gui.Overlay;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import tools.OtherTools;

public class Sequence<T extends Trackable> {
	protected int id;
	protected String label;
	protected SortedMap <Integer,T> trackables;

	protected Color color;
	protected Properties properties;
	protected Policy<T,?> policy;
	
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

	public Sequence(int ident, String lab, Policy<T,?> pol){
		policy=pol;
		color=new Color(255,0,0,255);
		id = ident;
		label=lab;
		trackables=new TreeMap<Integer,T>();
		properties=new Properties();
		properties.setProperty("this", "property");
		properties.setProperty("is", "property");
		properties.setProperty("a", "property");
		properties.setProperty("test", "property");
		
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
	
	public String getTypeName(){
		return policy.getTypeName();
	}
	
	public int getId(){
		return id;
	}
	
	public boolean writeToFile(FileWriter fileWriter) throws IOException{
		fileWriter.write("%-sequence properties-\n");
		OtherTools.writeProperties(fileWriter, getProperties());		
		fileWriter.write("%-data-\n");
		for(T trackable: trackables.values()){
			fileWriter.write(trackable.toSaveString()+"\n");
		}
		
		fileWriter.flush();
	
	
	
	
	return true;
	}
	
	public Properties getProperties(){
		Properties props= new Properties();
		props.setProperty("seqId:",String.valueOf(getId()));
		return props;
	}
	
	
	
	public void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected){
		policy.getKymoOverlayX(ov, scaleX, scaleY, transX, transY, selected, trackables, color);
	}
	
	public void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected){
		policy.getKymoOverlayY(ov, scaleX, scaleY, transX, transY, selected, trackables, color);
	}
	

}
