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
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.gui.ViewModel;
import frameWork.gui.controlWindow.ControlWindow;


public class  Controller< IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {
	
	
	protected ControlWindow<IT> controllWindow;
	protected double xyToZ=3.5;
	private int selectedTCId;
	
	protected Model<IT> model;

	private final SortedMap <Integer, ChannelController <? extends Trackable,IT> > channelControllers;
	private final SortedMap<String,Policy<? extends Trackable,IT>> policies;
	 
	public Controller( Model<IT> mod){
		
		policies= new TreeMap<String,Policy<? extends Trackable,IT>>();
		
		model =mod;
		channelControllers=	new TreeMap<Integer, ChannelController<? extends Trackable,IT>>();
		
		
	}

	public void addPolicy(Policy<? extends Trackable,IT> policy){
		policies.put(policy.getTypeName(), policy);
		
	}
	
private List <String> getFilesFromDirectory(String directory){
	File dir = new File(directory);
	
	FilenameFilter filter = new FilenameFilter() {
	    @Override
		public boolean accept(File dir, String name) {
	        if (name.startsWith(".")) return false;
	        return name.endsWith(".trcT");
	    }
	};
	
	String[] listOfFiles= dir.list(filter);
	
	
	List <String> results= new ArrayList<String>();
	for(int i=0;i<listOfFiles.length;i++)
		results.add(listOfFiles[i]);
	
	return results;
}

private void processFile(String fName){
		
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
		//	  System.out.println(strLine);
		  }
		  
		  
		  Properties sessionProps= new Properties();	//Get SessionProperties
		 
		  String forReader="";
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			  if(strLine.equals("%-sequence properties-")) break;
			  forReader= forReader+ strLine.replace("%","")+"\n";  
	//		  System.out.println(strLine);
		  }
		  Reader reader= new StringReader(forReader);
		  sessionProps.load(reader);
		  ChannelController<? extends Trackable, IT> cc = this.findOrCreateController(sessionProps);
	  
		  Properties sequenceProps= new Properties();	//Get SequenceProperties
		  forReader="";
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			  if(strLine.equals("%-data-")) break;
			  forReader= forReader+ strLine.replace("%","")+"\n";
		//	  System.out.println(strLine);
		  }
		  reader= new StringReader(forReader);
		  sequenceProps.load(reader);
		  cc.CheckOrCreateSequence(sequenceProps);
		  		  
		  
		  while ((strLine = br.readLine()) != null)   {	// get data		  
			  cc.processLineFromFile(strLine);
		//	  System.out.println (strLine);
		  }
		  //Close the input stream
		  in.close();
		    }catch (IOException e){//Catch exception if any
		  IJ.error("Error: " + e.getMessage());
		  }
	
}

private <T extends Trackable> ChannelController<? extends Trackable,IT> findOrCreateController(Properties sessionProps){
	String s=sessionProps.getProperty("sessionId");
	int id=Integer.valueOf(s);
	
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(id);
	if(cc==null){
		Policy<?, IT> policy= policies.get(sessionProps.getProperty("typeName"));
	//	Policy<?, IT> policy= policies.get("Blob");
		cc=policy.produceControllerAndChannel(sessionProps, model);
		
		this.channelControllers.put(cc.trackingChannel.getId(), cc); 
		if(selectedTCId==-1)this.selectedTCId=cc.getId();
	}
	return cc;
	
}


	

/**
 * Processes a MoseEvent generated at one of the ViewWindows 
 *
 * @param position the position of the event as vector in 5D 
 * @param e the original MouseEvent
 */
public void click(long[] position, int tChannel, MouseEvent e){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	
	if (cc!=null){
		
		cc.click(position, e);
	}
	}
}

public boolean isTracking(){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	if(cc!=null) return cc.isTracking();
	return false;
}

public void optimizeFrame(int frameNumber ){
	synchronized (model){
		ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
		if(cc!=null) cc.optimizeFrame(frameNumber);		
	}
	model.makeStructuralChange();
}

public void StartTracking(int frameNumber ){
//	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.startTracking(frameNumber);
//	model.makeStructuralChange();
	
//	}
}

public void StopTracking(){
//	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.stopTracking();
//	model.makeStructuralChange();
//	}
}

public void toggleTracking(int frameId){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null)
		if(!cc.isTracking()) cc.startTracking(frameId);
		else cc.stopTracking();
	model.makeStructuralChange();
}

public void splitSequence(int frameId){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.splitSequnce(frameId);
	model.makeStructuralChange();
	model.makeChangesPublic();
	}
}

public void deleteSequence(){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.deleteSequence();
	model.makeStructuralChange();
	model.makeChangesPublic();
	}
}

public void trimSequence(int frameId){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.trimSequence(frameId);
	model.makeStructuralChange();
	model.makeChangesPublic();
	}
}

public void saveAll(){
	for(ChannelController<? extends Trackable,IT> cc: this.channelControllers.values()){
		cc.saveAll();
	}
}

public void mergeSequenences(){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.mergeSequenences();
	model.makeStructuralChange();
	model.makeChangesPublic();	
	}
}

//public int getSelectedSeqId(){
//	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
//	return cc.getSelectedSeqId();
//}

public void setColor(Color c){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.setColor(c);
	model.makeStructuralChange();
	model.makeChangesPublic();
}

public boolean isSeletced(int sId){
	
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	if(cc==null) return false;
	return cc.isSelected(sId);
}

public void processDirectory(String directory){
	String dir=directory;
	if(!dir.endsWith("/")) dir=dir+"/";
	List <String> files= getFilesFromDirectory(dir);
	for(String fName: files){
		this.processFile(dir+fName);
	}
	model.makeStructuralChange();
	
}

public synchronized void setSelectionList(List <Integer> selectedIds){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.setSelectionList(selectedIds);
	model.makeStructuralChange();
	model.makeChangesPublic();
	}
	
}

public void addSession(String typeName, String label, int channelID, ViewModel<IT> viewModel){
	synchronized (model){
	Properties sessionProps= new Properties();
	sessionProps.setProperty("typeName", typeName);
	sessionProps.setProperty("sessionLabel", label);
	sessionProps.setProperty("sessionId", String.valueOf(model.getNextTCId()));
	sessionProps.setProperty("channelId", String.valueOf(channelID));
	ChannelController <? extends Trackable,IT> cc= this.findOrCreateController(sessionProps);
	this.channelControllers.put(cc.getId(), cc);
	if(this.selectedTCId==-1) this.selectedTCId=cc.getId();
	}
	model.makeStructuralChange();
	viewModel.reFreshSessionToBeDisplayed();
	model.makeChangesPublic();
	
}

public String[] getPossibleSessionTypes(){
	Set<String> keySet=policies.keySet();
	String[] results= new String[keySet.size()];
	int c=0;
	for(String key:keySet){
		results[c]=key;
		c++;
	}
	return results;
}

public String getWorkspace(){
	return model.getProjectDirectory();
}

public void setWorkspace(String path){
	synchronized (model){
	model.setProjectDirectory(path);
	model.makeChangesPublic();
	}
}

public List<Session<? extends Trackable, IT>> getSessions(){
	return new ArrayList<Session<? extends Trackable, IT>> (model.getSessions());
}

public void load(ViewModel<IT> viewModel){
	synchronized (model){
	channelControllers.clear();
	this.selectedTCId=-1;
	model.clearSessions();
	
	
	this.processDirectory(model.getProjectDirectory());
	
	model.makeStructuralChange();
	viewModel.reFreshSessionToBeDisplayed();
	model.makeChangesPublic();
	
	}
}

public void setCurrentSession(int id, ViewModel<IT> viewModel){
//	IJ.error("old:"+String.valueOf(selectedTCId));
	selectedTCId=id;
//	IJ.error("new:"+String.valueOf(selectedTCId));
	model.makeStructuralChange();
	viewModel.reFreshSessionToBeDisplayed();
	model.makeChangesPublic();
	
}

public int getCurrentSessionId(){
//	IJ.error("asking:"+String.valueOf(selectedTCId));
	return selectedTCId;

}

public Session<? extends Trackable, IT> getCurrentSession(){
	return model.getTrackingChannel(selectedTCId);

}

public void deleteSession(ViewModel<IT> viewModel){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	if(!channelControllers.isEmpty()) selectedTCId=channelControllers.firstKey();
	else selectedTCId=-1;
	if(cc!=null){
	cc.deleteAllSequences();
	channelControllers.remove(cc.getId());
	model.deleteSession(cc.getId());
	viewModel.reFreshSessionToBeDisplayed();
	}
	if(!channelControllers.isEmpty()) this.setCurrentSession(channelControllers.firstKey(), viewModel);
	model.makeStructuralChange();
	model.makeChangesPublic();
	
	}
	


}

public void reFresh(){
	model.makeStructuralChange();
	model.makeChangesPublic();
}

public void setSqequenceLabel(int id, String newLabel){
	synchronized (model){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.setSequenceLabel(id,newLabel);
	model.makeStructuralChange();
	model.makeChangesPublic();
	}
		
}







}
