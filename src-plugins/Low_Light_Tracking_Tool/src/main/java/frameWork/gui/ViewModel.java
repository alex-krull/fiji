/*******************************************************************************
 * This software implements the tracking method described in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexandar Krull
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
package frameWork.gui;

import ij.IJ;
import ij.gui.GenericDialog;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import tools.ImglibTools;
import frameWork.Controller;
import frameWork.Model;
import frameWork.Sequence;
import frameWork.Session;
import frameWork.Trackable;

public class ViewModel < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > implements Observer{
	

	protected RandomAccessibleInterval<IT> image;	
	
			
	protected int currentFrameNumber=0;
	public int getCurrentFrameNumber() {
		return currentFrameNumber;
	}

	public void setCurrentFrameNumber(int currentFrameNumber) {
		this.currentFrameNumber = currentFrameNumber;
	}

	protected int currentSliceNumber=0;
	protected int currentChannelNumber=0;
	protected int currentTrackingChannel=0;
	protected int mouseX=0;
	protected int mouseY=0;
	protected int mouseZ=0;
	protected volatile boolean mouseIsInWindow=false;
	
	protected boolean drawOverlays=true;
	protected boolean drawNumbers=false;
	

	protected HotKeyListener hotKeyListener;
	
	
	protected Model<IT> model;
	protected Controller<IT> controller;
	public List<Session<? extends Trackable,IT>> sessionsToBeDisplayed;
	
	protected List <ViewWindow<IT>> views;
	protected boolean sessionsUpdate=true;
	

	private MainWindow<IT> mainWindow;


	private MaxProjectionZ<IT> maxZWindow;
	
	public void exportImages(){
		GenericDialog gd = new GenericDialog("export images");
		gd.addNumericField("magnification:", 4.0, 2);
		gd.showDialog();
		if(gd.wasCanceled()) return;
		double mag= gd.getNextNumber();
		
		for(int i=0;i<model.getNumberOfFrames();i++){
			this.setPosition(3, i);	
			for(ViewWindow vw: views){
				vw.reFresh(this.getPosition(), true);
				vw.saveWindow(mag);
			}
		}
	}
	
	public void toggleDrawNumbers() {
	
		
		
		drawNumbers=!drawNumbers;
		
	}
	
	public boolean isDrawNumbers() {
		
		return drawNumbers;
	}

	public void setDrawNumbers(boolean drawNumbers) {
		this.drawNumbers = drawNumbers;
	}
	
	class ProjectionJob	implements Callable<RandomAccessibleInterval<IT> >{
		RandomAccessibleInterval<IT> image;
		int dim;
		
		ProjectionJob(RandomAccessibleInterval<IT> img, int d){
			image= img;
			dim=d;
		}
		@Override
		public RandomAccessibleInterval<IT> call() throws Exception {
			
			return ImglibTools.projection(image, dim);
		}

		
	}
 
	/**
	 * Creates a new ViewModel
	 *
	 * The ViewModel from a Model and a Controller
	 *
	 * @param mod the Model to be used.
	 * @param contr the Controller to be used.
	 **/
	public ViewModel(Model<IT> mod, Controller<IT> contr){
		sessionsToBeDisplayed= new ArrayList<Session<? extends Trackable, IT>>();
	hotKeyListener=new HotKeyListener(this);
	   controller=contr;
		
	   model=mod;
	   //sessionsToBeDisplayed=model.getTCsAssociatedWithChannel(this.currentChannelNumber);   
            
       image = model.getImage();
       
       System.out.println("channels:" +model.getNumberOfChannels()+ "  frames:"+model.getNumberOfFrames()+ "  slices:"+model.getNumberOfSlices());
       
       
    
  	     
       
       
 		System.out.println("dimensions:" + image.numDimensions());
   
       
 	  views= new ArrayList<ViewWindow<IT>>();
 	 reFreshSessionToBeDisplayed();
    }

public void toggleDrawOverlays(){
	drawOverlays=!drawOverlays;
	System.out.println("drawOverlays:"+drawOverlays);
	this.setPosition(-1,-1);
}

	/**
	 * set the ViewModel to a different position
	 *
	 * All ViewWindows will be set to the position automatically as well.
	 *
	 * @param dim the dimension that should be changed
	 * @param pos the new value for the chosen dimension.
	 **/
public void setPosition(int dim, int pos){
	//long time0= System.nanoTime();
	
	if(dim==2){
		if(currentSliceNumber==pos) return;
		this.currentSliceNumber= Math.min(Math.max(pos,0), model.getNumberOfSlices()-1);
		
	}
	if(dim==3){
		if(currentFrameNumber==pos) return;
		this.currentFrameNumber= Math.min(Math.max(pos,0), model.getNumberOfFrames()-1);
	}
	if(dim==4){
		if(currentChannelNumber==pos) return;
		this.currentChannelNumber= Math.min(Math.max(pos,0), model.getNumberOfChannels()-1);
	//	sessionsToBeDisplayed.clear();
	//	sessionsToBeDisplayed=model.getTCsAssociatedWithChannel(currentChannelNumber);
		reFreshSessionToBeDisplayed();
	}
	
	upDateImages(currentFrameNumber, currentSliceNumber, currentChannelNumber, true );

	//long time1= System.nanoTime();
	
}

/**
 * Gives the current position in form of a long[]
 *
 * @return the position
 **/
public long[] getPosition(){
	long[] result= {0,0,currentSliceNumber, currentFrameNumber, currentChannelNumber};
	return result;
}

public void mouseAtPosition(long [] pos, MouseEvent me){
		
	boolean redraw=false;
	this.mouseX=(int)pos[0];
	this.mouseY=(int)pos[1];
	if(me.getButton()==MouseEvent.BUTTON2 && me.getID()==MouseEvent.MOUSE_CLICKED){
		if(pos[2]>=0) this.currentSliceNumber=(int)pos[2];
		if(pos[3]>=0) this.currentFrameNumber=(int)pos[3];
		if(pos[4]>=0) this.currentChannelNumber=(int)pos[4];
		redraw=true;
	}
	
	if(me.getID()==MouseEvent.MOUSE_EXITED) this.mouseIsInWindow=false;
	else this.mouseIsInWindow=true;
	
	
	controller.click(pos, currentTrackingChannel, me, this);
	
	upDateImages(this.currentFrameNumber,this.currentSliceNumber, this.currentChannelNumber, redraw);
}

public boolean isMouseInWindow(){
	return mouseIsInWindow;
}

protected void upDateImages(int frame, int slice, int channel, boolean init){
	
	long[] pos= {this.mouseX,this.mouseY,slice, frame, channel};
	for(ViewWindow<IT> vw:views){	
		vw.upDate(pos, init);
	}
 
				
	}



@Override
public synchronized void update(Observable arg0, Object arg1) {
	if(arg1!=null)
		this.setPosition(3, ((Integer)arg1).intValue());
	else	
		upDateImages(currentFrameNumber, this.currentSliceNumber, this.currentChannelNumber, arg1!=null );
}

//public int getSelectedSequenceId(){
//	return this.controller.getSelectedSeqId();
//}

public boolean isSelected(int sId){
	return controller.isSeletced(sId);
}

public int getCurrentChannelNumber(){
	return currentChannelNumber;
}

public int getCurrentSliceNumber(){
	return currentSliceNumber;
}

public void addViewWindow( ViewWindow<IT> vw){
	model.rwLock.writeLock().lock();
	views.add(vw);
	vw.addKeyListener(hotKeyListener);
	
	this.upDateImages(0, 0, 0,true);

	model.rwLock.writeLock().unlock();
}

public List<Session<? extends Trackable,IT>> getSessionsToBeDisplayed(){
	
	return this.sessionsToBeDisplayed;
//	List <Session<? extends Trackable,IT>> results=new ArrayList<Session<? extends Trackable,IT>>();
//	if(controller.getCurrentSession()!=null) results.add(controller.getCurrentSession());
	//for (Session<? extends Trackable,IT> s: model.getSessions()){
	//	if(s.isAssociatedWithMovieChannel(this.currentChannelNumber)) results.add(s);
	//}
//	return results;
}

public boolean isSessionVisible(int id){
	for(Session<? extends Trackable,IT> s: getSessionsToBeDisplayed()){
		if(s.getId()==id) return true;
	}
	return false;
}

public boolean getDrawOverLays(){
	return drawOverlays;
}

public void toggleTracking(boolean multiscale){
	controller.toggleTracking(currentFrameNumber, multiscale, model.getNumberOfFrames());
}

public void deleteSequence(){
	controller.deleteSequence();
}

public void trimSequence(){
	controller.trimSequence(currentFrameNumber);
}

public void splitSequence(){
	controller.splitSequence(currentFrameNumber);
}

public boolean isTracking(){
return controller.isTracking();
}

public void saveAll(){
	try {
		controller.saveAll();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace(Model.errorWriter);
		Model.errorWriter.flush();
	}
}

public void setColor(Color c){
	controller.setColor(c);
}

public  List <Sequence<? extends Trackable>> getVisibleSequences(){
	List <Sequence<? extends Trackable>> results = new ArrayList <Sequence<? extends Trackable>>();
	for(Session<? extends Trackable ,IT> tc: this.getSessionsToBeDisplayed()){
		results.addAll(tc.getSeqsCollection());
	}
	
	return results;
}
 
public void setSelectionList(List <Integer> selectedIds){
	controller.setSelectionList(selectedIds);
}

public void mergeSequenences(){
	controller.mergeSequenences();
}

public Controller<IT> getController(){
	return controller;
}

public void reFreshSessionToBeDisplayed(){
	if(sessionsToBeDisplayed==null) sessionsToBeDisplayed= new ArrayList<Session<? extends Trackable,IT>>();
	sessionsToBeDisplayed.clear();
//	sessionsToBeDisplayed=model.getTCsAssociatedWithChannel(this.currentChannelNumber);  
	Session<? extends Trackable,IT> ses=controller.getCurrentSession();
	if(ses!=null) sessionsToBeDisplayed.add(ses);
}

public void toggleSessionTobeDisplayed(Session<? extends Trackable,IT> ses){
	if(!sessionsToBeDisplayed.contains(ses)) sessionsToBeDisplayed.add(ses);
	else sessionsToBeDisplayed.remove(ses);
	model.makeStructuralChange();
	this.setPosition(-1, -1);
}

public List<ViewWindow<IT>> getViewWindows(){
	return this.views;
}

public void addMainWindow(MainWindow<IT> mWindow){
	 mainWindow = mWindow;
	 this.addViewWindow(mainWindow);
}

public void addMaxZWindow(MaxProjectionZ<IT> maxZ){
	maxZWindow = maxZ;
	this.addViewWindow(maxZWindow);
}
public void resetWindowsPositions(){
	
	//for(int i=0;i<2;i++){
	
//	for(ViewWindow<IT> vw:  views)
//		vw.close();
	model.rwLock.writeLock().lock();
	
	for(ViewWindow<IT> vw:  views)
		vw.setZoom(mainWindow.getZoom());
	
	try {
		Thread.sleep(50);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	model.rwLock.writeLock().unlock();
	
	
//for(ViewWindow<IT> vw:  views)
//		vw.open();
	
	for(ViewWindow<IT> vw:  views)
		vw.setWindowPosition(mainWindow, maxZWindow);
	
	//}
	
	mainWindow.getWindow().toFront();
	
	
}
}