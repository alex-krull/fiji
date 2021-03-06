/*******************************************************************************
 * This software implements the tracking method described in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexander Krull
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Contributors:
 * 	Alexander Krull (Alexander.Krull@tu-dresden.de)
 *     Damien Ramunno-Johnson (GUI)
 *******************************************************************************/
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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import frameWork.Sequence;
import frameWork.Trackable;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.gui.ViewModel;
import frameWork.gui.ViewWindow;
import frameWork.gui.controlWindow.ControlWindow;
import frameWork.gui.controlWindow.NewSessionDialog;



public class  Controller< IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {
	
	
	
	
	protected ControlWindow<IT> controllWindow;
	protected double xyToZ=3.5;
	private int selectedTCId;
	private volatile boolean alternateMethodUsed=false;
	
	
	protected Model<IT> model;

	private final SortedMap <Integer, ChannelController <? extends Trackable,IT> > channelControllers;
	private final SortedMap<String,Policy<? extends Trackable,IT>> policies;
	private boolean autosave=true;
	private final  MyTool abstractTool;
	 
	
	public Controller( Model<IT> mod, MyTool at){
		abstractTool=at;
		policies= new TreeMap<String,Policy<? extends Trackable,IT>>();
		
		model =mod;
		channelControllers=	new TreeMap<Integer, ChannelController<? extends Trackable,IT>>();
		
		
	}
	
	public boolean isAutoSave(){
		return autosave;
	}
	
	public void toggleAutosave(){
		autosave=!autosave;
	}

	public void addPolicy(Policy<? extends Trackable,IT> policy){
		policies.put(policy.getTypeName(), policy);
		
	}
	
	public void addPolicy(String s, Policy<? extends Trackable,IT> policy){
		policies.put(s, policy);
		
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
		Model.getInstance().depositMsg("opening: "+fName);
		  // Open the file that is the first 
		  // command line parameter
		  FileInputStream fstream = new FileInputStream(fName);
		  // Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
		  //Read File Line By Line
		  
		  String forReader="";
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			 
		
			  
			  if(strLine.equals("%-global properties-")){
				  
				  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
					  forReader= forReader+ strLine.replace("%","")+"\n";  
					  if(strLine.equals("%-session properties-")) break;	
				  }
				  Properties globalProps= new Properties();
				  globalProps.load(new StringReader(forReader));
				  if(!model.setProperties(globalProps)) return;
				  break;
			  }
			  
			  if(strLine.equals("%-session properties-")) break;
		  }
		  
		  
		  
		  
		  
		  Properties sessionProps= new Properties();	//Get SessionProperties
		  
		  forReader="";
		  
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			  if(strLine.equals("%-sequence properties-")) break;
			  forReader= forReader+ strLine.replace("%","")+"\n";  
	//		  System.out.println(strLine);
		  }
		  
		  sessionProps.load(new StringReader(forReader));
		  ChannelController<? extends Trackable, IT> cc = this.findOrCreateController(sessionProps);
	  
		  Properties sequenceProps= new Properties();	//Get SequenceProperties
		  forReader="";
		  while ((strLine = br.readLine()) != null&& strLine.startsWith("%")){
			  if(strLine.equals("%-data-")) break;
			  forReader= forReader+ strLine.replace("%","")+"\n";
		//	  System.out.println(strLine);
		  }
		  
		  sequenceProps.load(new StringReader(forReader));
		  File f= new File(fName);
		  cc.CheckOrCreateSequence(sequenceProps, f.getName());
		  		  
		  
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
		
	
		if(policy==null){
			policy= policies.get("GaussianML");	// interpret unknown session as GaussianML
			
		}
		
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
public synchronized void click(long[] position, int tChannel, MouseEvent e, ViewModel<IT> vm){
	//if(!IJ.getToolName().equals("trackingTool")){
	if(!abstractTool.getToolName().equals(IJ.getToolName())){
		System.out.println("Wrong Tool");
		return;
	}
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	
	if (cc!=null){
		
		cc.click(position, e,  vm);
	}
	model.rwLock.writeLock().unlock();
}

public boolean isTracking(){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	return Model.getInstance().isCurrentlyTracking();
}

public boolean isAlternateMethodUsed(){
	return alternateMethodUsed;
}

public void optimizeFrame(int frameNumber){
	toggleTracking(frameNumber,false,frameNumber);
}


public void optimizeAllFrames(ViewModel<IT> vm){
	
	
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	
	List<Sequence<? extends Trackable>> vs = vm.getVisibleSequences();
	for(Sequence<? extends Trackable> t: vs){
		
	ArrayList<Integer> sids= new ArrayList<Integer>();
	sids.add(new Integer(t.id));
	this.setSelectionList(sids);
	
	
		
		
			if(!Model.getInstance().isCurrentlyTracking()){
						
				cc.startTrackingSingleThread(0, false, autosave,model.getNumberOfFrames()-1);
			}
			
		
		model.makeStructuralChange();		
		model.makeChangesPublic();
	
	
	}
}
/*
public void optimizeAllFrames(){
	
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	
	
	for(int i=0;i<model.getNumberOfFrames();i++){
		
		
			if(!Model.getInstance().isCurrentlyTracking()){
						
				cc.startTrackingSingleThread(i, false, autosave,i);
			}
			
		
		model.makeStructuralChange();
		model.makeChangesPublic();
	}
}
*/
public ChannelController<? extends Trackable,IT> getCurrentSessionController(){
	return channelControllers.get(selectedTCId);
}

public void toggleTracking(int frameId, boolean multiscale, int lastFrame){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null)
		if(!Model.getInstance().isCurrentlyTracking()){
			cc.startTracking(frameId, multiscale, autosave,lastFrame);			
			alternateMethodUsed=multiscale;
		}
		else cc.stopTracking();
	
	model.makeStructuralChange();
	model.makeChangesPublic();
	
}

public void splitSequence(int frameId){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.splitSequnce(frameId);
	model.makeStructuralChange();
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
}

public void deleteSequence(){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.deleteSequence();
	model.makeStructuralChange();
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
}

public void trimSequence(int frameId){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);	
	if(cc!=null) cc.trimSequence(frameId);
	model.makeStructuralChange();
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
	
}

public void saveAll() throws IOException{
	for(ChannelController<? extends Trackable,IT> cc: this.channelControllers.values()){
		cc.saveAll();
	}
}

public void mergeSequenences(){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.mergeSequenences();
	model.makeStructuralChange();
	model.makeChangesPublic();	
	model.rwLock.writeLock().unlock();
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

public List<Integer> getSelectionList(){
	
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	if(cc==null) return new ArrayList<Integer>();
	return cc.getSelectionList();
	
}

public void setSelectionList(List <Integer> selectedIds){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.setSelectionList(selectedIds);
	model.makeStructuralChange();
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
	
}

public void addSession(String typeName, String label, int channelID, ViewModel<IT> viewModel){
	model.rwLock.writeLock().lock();
	Properties sessionProps= new Properties();
	sessionProps.setProperty("typeName", typeName);
	sessionProps.setProperty("sessionLabel", label);
	sessionProps.setProperty("sessionId", String.valueOf(model.getNextTCId()));
	sessionProps.setProperty("channelId", String.valueOf(channelID));
	ChannelController <? extends Trackable,IT> cc= this.findOrCreateController(sessionProps);
	this.channelControllers.put(cc.getId(), cc);
	if(this.selectedTCId==-1) this.selectedTCId=cc.getId();
	
	model.makeStructuralChange();
	if(viewModel!=null)viewModel.reFreshSessionToBeDisplayed();
	model.makeChangesPublic();
	
	model.rwLock.writeLock().unlock();
	
}

public String[] getPossibleSessionTypes(){
	
	Set<String> keySet=policies.keySet();
	int c=0;
	for(String key:keySet){
		if(!policies.get(key).isHidden())
			c++;		
	}
	
	
	String[] results= new String[c];
	c=0;
	for(String key:keySet){
		if(!policies.get(key).isHidden()){
			results[c]=key;
			c++;
		}
	}
	return results;
}

public String getWorkspace(){
	return model.getProjectDirectory();
}

public void setWorkspace(String path){
	model.rwLock.writeLock().lock();
	model.setProjectDirectory(path);
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
}

public List<Session<? extends Trackable, IT>> getSessions(){
	return new ArrayList<Session<? extends Trackable, IT>> (model.getSessions());
}

public void load(ViewModel<IT> viewModel){
	model.rwLock.writeLock().lock();
	channelControllers.clear();
	this.selectedTCId=-1;
	model.clearSessions();
	
	
	this.processDirectory(model.getProjectDirectory());
	
	model.makeStructuralChange();
	viewModel.reFreshSessionToBeDisplayed();
	model.makeChangesPublic();
	
	model.rwLock.writeLock().unlock();
}

public void setCurrentSession(int id, ViewModel<IT> viewModel){
	model.rwLock.writeLock().lock();
//	IJ.error("old:"+String.valueOf(selectedTCId));
	selectedTCId=id;
//	IJ.error("new:"+String.valueOf(selectedTCId));
	model.makeStructuralChange();
	viewModel.reFreshSessionToBeDisplayed();
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
	
}

public int getCurrentSessionId(){
//	IJ.error("asking:"+String.valueOf(selectedTCId));
	return selectedTCId;

}

public Session<? extends Trackable, IT> getCurrentSession(){
	return model.getTrackingChannel(selectedTCId);

}

public void deleteSession(ViewModel<IT> viewModel){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	
	if(cc!=null){
	cc.deleteAllSequences();
	channelControllers.remove(cc.getId());
	if(!channelControllers.isEmpty()) selectedTCId=channelControllers.firstKey();
	else selectedTCId=-1;
	
	model.deleteSession(cc.getId());
	viewModel.reFreshSessionToBeDisplayed();
	}
	if(!channelControllers.isEmpty()) this.setCurrentSession(channelControllers.firstKey(), viewModel);
	model.makeStructuralChange();
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
	
	


}

public void reFresh(){
	model.makeStructuralChange();
	model.makeChangesPublic();
}

public void setSqequenceLabel(int id, String newLabel){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	cc.setSequenceLabel(id,newLabel);
	model.makeStructuralChange();
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
		
}

public void showOjectOptions(int frameNumber){
	model.rwLock.writeLock().lock();
	ChannelController<? extends Trackable,IT> cc= channelControllers.get(selectedTCId);
	if(cc!=null){			
		cc.showObjectOptions(frameNumber);
	}
	model.makeChangesPublic();
	model.rwLock.writeLock().unlock();
}

public void newSession(ViewModel<IT> viewModel){

	NewSessionDialog<IT> newDialog = new NewSessionDialog<IT>(this, model, viewModel.getCurrentChannelNumber()+1);
	this.addSession(newDialog.getMethodChoice(), newDialog.getUserSessionName(), newDialog.getChannelChoice()-1, viewModel); 
	int newsessionID = viewModel.getController().getSessions().size()-1;
	viewModel.getController().setCurrentSession(newsessionID, viewModel);
	viewModel.getController().getCurrentSession().showPropertiesDialog();
	viewModel.setPosition(4, newDialog.getChannelChoice()-1);
}

public void shutdown(List<ViewWindow<IT>> windows){
	//abstractTool.shutDown();
	for(ViewWindow<IT> vw: windows){
		vw.terminate();		
	}
	
//	Toolbar.getInstance().restorePreviousTool();
	
//	Toolbar.getInstance().
}







}
