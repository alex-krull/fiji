package frameWork;


import ij.IJ;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import blobTracking.Blob;
import blobTracking.BlobController;
import blobTracking.BlobTrackingChannel;


public class  Controller< IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {
	
	
	
	protected double xyToZ=3.5;
	public int selectedTCId;
	
	protected Model<IT> model;

	private final SortedMap <Integer, ChannelController <? extends Trackable,IT> > channelControllers;

	 
	public Controller( Model<IT> mod){
		
		
		model =mod;
		channelControllers=	new TreeMap<Integer, ChannelController<? extends Trackable,IT>>();
		
		TrackingChannel<Blob,IT> tc= new BlobTrackingChannel<IT>(model.getMovieChannel(0),model.getNextTCId() );
		BlobController<IT> bc= new BlobController<IT>(model,tc);
		channelControllers.put(tc.getId(), bc);
		model.addTrackingChannel(tc,0);
		selectedTCId=tc.getId();
		
		//for(int j=0;j<tc.getNumberOfFrames();j++){
//			tc.addTrackable(new Blob(2,500,20 +Math.cos(0/15.0f)*25,70+ Math.sin(0/35.0f)*25,15,4, 0));
//			tc.addTrackable(new Blob(1,500,20 +Math.cos(0/15.0f)*25,70+ Math.sin(0/35.0f)*25,15,4, 0));
//			tc.addTrackable(new Blob(3,500,20 +Math.cos(0/15.0f)*25,70+ Math.sin(0/35.0f)*25,15,4, 0));
		//}
		
		//readFile("/home/alex/Desktop/test.txt");
		processFile("/home/alex/workspace/fiji/seq1.txt");
	}
	
private List <String> getFilesFromDirectory(String directory){
	File dir = new File(directory);
	
	FilenameFilter filter = new FilenameFilter() {
	    public boolean accept(File dir, String name) {
	        if (name.startsWith(".")) return false;
	        return name.endsWith(".trc");
	    }
	};
	
	String[] listOfFiles= dir.list(filter);
	
	
	List <String> results= new ArrayList<String>();
	for(int i=0;i<listOfFiles.length;i++)
		results.add(listOfFiles[i]);
	
	return results;
}

private void processFile(String fName){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	
	
	try{
		  // Open the file that is the first 
		  // command line parameter
		  FileInputStream fstream = new FileInputStream(fName);
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
		  //Read File Line By Line
		  
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			  if(strLine.equals("%-session properties-")) break;	//skip through initial comments
			  System.out.println(strLine);
		  }
		  
		  
		  Properties sessionProbs= new Properties();	//Get SessionProperties
		  String forReader="";
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			  if(strLine.equals("%-sequence properties-")) break;
			  forReader= forReader+ strLine.replace("%","")+"\n";  
			  System.out.println(strLine);
		  }
		  Reader reader= new StringReader(forReader);
		  sessionProbs.load(reader);
		  IJ.error(forReader);
		  IJ.error(sessionProbs.toString());
	  
		  Properties sequenceProbs= new Properties();	//Get SequenceProperties
		  forReader="";
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			  if(strLine.equals("%-data-")) break;
			  forReader= forReader+ strLine.replace("%","")+"\n";
			  System.out.println(strLine);
		  }
		  reader= new StringReader(forReader);
		  sequenceProbs.load(reader);
		  IJ.error(forReader);
		  IJ.error(sequenceProbs.toString());
		  
		  
		  System.out.println(sessionProbs.toString());
		  System.out.println(sequenceProbs.toString());
		  		  
		  
		  while ((strLine = br.readLine()) != null)   {	// get data		  
			  cc.processLineFromFile(strLine);
			  System.out.println (strLine);
		  }
		  //Close the input stream
		  in.close();
		    }catch (IOException e){//Catch exception if any
		  IJ.error("Error: " + e.getMessage());
		  }
	model.makeChangesPublic();
}


	

/**
 * Processes a MoseEvent generated at one of the ViewWindows 
 *
 * @param position the position of the event as vector in 5D 
 * @param e the original MouseEvent
 */
public void click(long[] position, int tChannel, MouseEvent e){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(tChannel);
	
	if (cc!=null){
		
		cc.click(position, e);
	}
}

public boolean isTracking(){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	return cc.isTracking();
}

public void optimizeFrame(int frameNumber ){
		ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
		if(cc!=null) cc.optimizeFrame(frameNumber);
			
}

public void StartTracking(int frameNumber ){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.startTracking(frameNumber);
}

public void StopTracking(){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.stopTracking();
}

public void toggleTracking(int frameId){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null)
		if(!cc.isTracking()) cc.startTracking(frameId);
		else cc.stopTracking();
}

public void splitSequence(int frameId){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.splitSequnce(frameId);
}

public void deleteSequence(){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.deleteSequence();
}

public void trimSequence(int frameId){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.trimSequence(frameId);
}

public void saveAll(){
	for(ChannelController<? extends Trackable,IT> cc: this.channelControllers.values()){
		cc.saveAll();
	}
}

public void mergeSequenences(){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.mergeSequenences();
}

//public int getSelectedSeqId(){
//	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
//	return cc.getSelectedSeqId();
//}

public void setColor(Color c){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.setColor(c);
	model.makeChangesPublic();
}

public boolean isSeletced(int sId, int cId){
	if(cId!=this.selectedTCId) return false;
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	return cc.isSelected(sId);
}

public void loadSequqnceFromFile(String fileName){
	
}

public void setSelectionList(List <Integer> selectedIds){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.setSelectionList(selectedIds);
	model.makeChangesPublic();
}

}
