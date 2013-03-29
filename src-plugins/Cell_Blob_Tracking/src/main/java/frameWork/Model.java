/*******************************************************************************
 * This software implements the tracking method descibed in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2013 Alexandar Krull
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


import ij.ImagePlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Model <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Observable{ 
	



private static Model<?> instance;

private boolean isVolume=false;
private boolean isTimeSequence=false;
private boolean isMultiChannel=false;
private boolean switchedDimensions=false;
private int numberOfChannels;
private int numberOfFrames;
private int numberOfSlices;
private	RandomAccessibleInterval<IT> image;
private SortedMap <Integer, MovieChannel <IT> > channels;
private SortedMap <Integer, Session <? extends Trackable,IT> > trackingChannels;
private String imageFileName;
private String imageDrirectory;
private String projectDirectory;
private boolean structuralChange=true;
private int intensityOffset=0;
private double xyToZ=3.5;
public ReentrantReadWriteLock rwLock;
private boolean currentlyTracking=false;
private long startedTrackingAt;

private volatile StringBuffer msgBuffer=new StringBuffer();


public boolean isCurrentlyTracking() {
	return currentlyTracking;
}

public void setCurrentlyTracking(boolean currentlyTracking) {
	this.currentlyTracking = currentlyTracking;
	if(currentlyTracking)startedTrackingAt=System.currentTimeMillis();
	else{
		long ct=System.currentTimeMillis();
		System.out.println("timeTaken:" +(ct-startedTrackingAt)/1000.0+"\n");
	}
	
}

public static Model<?> getInstance(){
	return instance;
}

public double getXyToZ() {
	return xyToZ;
}

public void setXyToZ(double xyToZ) {
	this.xyToZ = xyToZ;

}

public int getIntensityOffset() {
	return intensityOffset;
}

public void setIntensityOffset(int intensityOffset) {
	this.intensityOffset = intensityOffset;

}




public boolean isStruckturalChange(){
	synchronized(trackingChannels){
		boolean temp=structuralChange;
		structuralChange=false;
		return temp;
	}
}

public void makeStructuralChange(){
	synchronized(trackingChannels){
		structuralChange=true;
	}
}

public String getImageFileName() {
	return imageFileName;
}

public String getImageFileNameNoEnding() {

	String[] a= imageFileName.split("\\.");
	return a[0];
}

public void setImageFileName(String imageFileName) {
	this.imageFileName = imageFileName;
}

public String getImageDrirectory() {
	return imageDrirectory;
}

public void setImageDrirectory(String imageDrirectory) {
	this.imageDrirectory = imageDrirectory;
}


public String getProjectDirectory() {
	return projectDirectory;
}

public void setProjectDirectory(String projectDirectory) {
	this.projectDirectory = projectDirectory;
}

public synchronized int getNextSequqnceId(){
	for(int i=1;i<10000;i++){
		if(this.getSequence(i)==null)
			return i;
	}
	return -1;
}

public synchronized int getNextTCId(){
	
	for(int i=0;i<10000;i++){
		if(this.getTrackingChannel(i)==null){
			this.trackingChannels.put(new Integer(i), null);
			return i;
		}
			
	}
	return -1;
}

public void setVolume(boolean isVolume) {
	this.isVolume = isVolume;
}

public boolean hasSwitchedDimension(){
	return switchedDimensions;
}

public boolean isTimeSequence() {
	return isTimeSequence;
}


public void setTimeSequence(boolean isTimeSequence) {
	this.isTimeSequence = isTimeSequence;
}


public boolean isMultiChannel() {
	return isMultiChannel;
}

public boolean isVolume() {
	return isVolume;
}

public void setMultiChannel(boolean isMultiChannel) {
	this.isMultiChannel = isMultiChannel;
}

public static PrintWriter errorWriter;

public Model(ImagePlus imp){
	boolean sDims=imp.getNSlices()>1&&!(imp.getNFrames()>1)&& !imp.isHyperStack();
	 RandomAccessibleInterval<IT> temp= ImagePlusAdapter.wrap(imp);
	initModel(imp.getOriginalFileInfo().fileName, imp.getOriginalFileInfo().directory, 
			temp,
			imp.getNFrames(), imp.getNSlices(), imp.getNChannels(),sDims );
}

public Model(String fName, String dName, RandomAccessibleInterval<IT> img,
		int nFrames, int nSlices, int nChannels, boolean sDims){
	
	initModel(fName, dName, 
			img,
			nFrames, nSlices, nChannels,sDims );
}

public void initModel(String fName, String dName, RandomAccessibleInterval<IT> img,
		int nFrames, int nSlices, int nChannels, boolean sDims){

	switchedDimensions=sDims;
	rwLock= new ReentrantReadWriteLock();
	
	instance=this;
	
	imageFileName=fName;
	imageDrirectory=dName;
	projectDirectory=imageDrirectory; // default projectDirectory is fileDirectory
	image=img;	
	
	try {
		errorWriter= new PrintWriter(new File(imageDrirectory+"/errors.txt"));
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	
	errorWriter.write("no errors up to here:\n");
	errorWriter.flush();
	
	setTimeSequence(nFrames>1);
	setMultiChannel(nChannels>1);
	setVolume(nSlices>1);	
	
	//System.out.println("iv:" + isVolume+ "  its:" +isTimeSequence + "  imc:"+ isMultiChannel);
	
	
//	if(isVolume&&!isTimeSequence && !imp.isHyperStack()){
	if(switchedDimensions){
 	   isVolume=false;
 	   isTimeSequence=true;
 	 
 //	   System.out.println("SWITCHING DIMENSIONS");
    }
	
	if(isMultiChannel)numberOfChannels=nChannels;
	else numberOfChannels=1;
	
	
	
	if(isMultiChannel()){
		
       image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,image.numDimensions()-1),2) );
	
    if(image.numDimensions()==5)  	   
       image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,3),2 ));
    	
    
    }   
	
	   
	channels=new TreeMap<Integer,MovieChannel <IT> >();
	trackingChannels= new TreeMap<Integer, Session <? extends Trackable,IT> >();
	
	if(!switchedDimensions){
		numberOfFrames=nFrames;
		numberOfSlices=nSlices;
	}
	else {
		numberOfFrames=nSlices;
		numberOfSlices=nFrames;
	}
	
	if(isMultiChannel){
		for(int i=0;i<numberOfChannels;i++){
			MovieChannel<IT> chann= new MovieChannel<IT>(Views.hyperSlice(image, image.numDimensions()-1, i),i,numberOfFrames, this.intensityOffset, xyToZ );
//			MovieChannel<IT> chann= new MovieChannel<IT>(Views.hyperSlice(image, 2, i),i,numberOfFrames );

			channels.put(i, chann);
				
			
		}
	}else{
		MovieChannel<IT> chann= new MovieChannel<IT>( image,0, numberOfFrames, this.intensityOffset, xyToZ);
		channels.put(0, chann);
	}
	
	
	
}

public RandomAccessibleInterval<IT> getImage(){
	return image;
}


public int getNumberOfChannels() {
	return numberOfChannels;
}

public int getNumberOfSlices() {
	return numberOfSlices;
}

public int getNumberOfFrames() {
	return numberOfFrames;
}

public int selectAt(int x, int y, int z, int frameId, int channel){
	return trackingChannels.get(channel).selectAt(x, y, z, frameId, channel);
}

public List<? extends Trackable> getTrackablesForFrame(int frame, int channel){
	
	if(trackingChannels.get(channel)!=null) return trackingChannels.get(channel).getTrackablesForFrame(frame);
	else return new LinkedList<Trackable>();
	
}

public Trackable getTrackable(int seqId, int frameId, int channel){
	return trackingChannels.get(channel).getTrackable(seqId, frameId);	
}

public Sequence<? extends Trackable> getSequence(int id, int channel){
	Session<? extends Trackable, IT> tc=trackingChannels.get(channel);
	if(tc==null){
		
		return null;
	}
	else{
		
		return tc.getSequence(id);
	}
}

public MovieFrame<IT> getFrame(int frame, int channel){
	return channels.get(channel).getMovieFrame(frame);
}

public RandomAccessibleInterval<IT> getXTProjections(int channel){
	this.rwLock.readLock().lock();
	RandomAccessibleInterval<IT> r= channels.get(channel).getXTProjections();
	this.rwLock.readLock().unlock();
	return r;
}

public RandomAccessibleInterval<IT> getYTProjections(int channel){
	this.rwLock.readLock().lock();
	RandomAccessibleInterval<IT> r= channels.get(channel).getYTProjections();
	this.rwLock.readLock().unlock();
	return r;
}

public void makeChangesPublic(){
	setChanged();
	notifyObservers();
}

public void makeChangesPublic(Integer frameNumber){
	setChanged();
	notifyObservers(frameNumber);
}

public MovieChannel<IT> getMovieChannel(int id){

	return channels.get(id);
}

public Session<? extends Trackable, IT> getTrackingChannel(int id){
	return trackingChannels.get(id);
}

public void addTrackingChannel(Session<? extends Trackable,IT> tc, int key){
	trackingChannels.put(key, tc);
}

public List<Session<? extends Trackable,IT>> getTCsAssociatedWithChannel(int id){
	List<Session<? extends Trackable,IT>> result= new ArrayList<Session<? extends Trackable,IT>>();
	Collection <Session<? extends Trackable,IT>> coll= this.trackingChannels.values();
	for(Session<? extends Trackable,IT> tc: coll){
		if(tc.isAssociatedWithMovieChannel(id)) result.add(tc);
	}
	return result;
}

public List<Sequence<? extends Trackable>> getAllSequencies(){
	List <Sequence<? extends Trackable>>results=new ArrayList<Sequence<? extends Trackable>>();
	for(Session<? extends Trackable,IT> tc: this.trackingChannels.values()){
		results.addAll(tc.getSeqsCollection());
	}
	return results;
}

public Sequence<? extends Trackable> getSequence(int id) {
	for(Session<? extends Trackable, IT> tc:trackingChannels.values()){
		if(tc.getSequence(id)!=null) return tc.getSequence(id);
	}
	return null;
}

public Collection< Session <? extends Trackable,IT> > getSessions(){
	return this.trackingChannels.values();
}

public void clearSessions(){
	this.trackingChannels.clear();
}

public void deleteSession(int id){
	this.trackingChannels.remove(id);
}

public boolean setProperties(Properties props){
	String s;
	
	
	s= props.getProperty("imageName"); if(s!=null){	
		if(!this.getImageFileName().equals(s)) return false;
	}
	s= props.getProperty("intensityOffset"); if(s!=null)this.setIntensityOffset(Integer.valueOf(s) );
	s= props.getProperty("xyToZ");if(s!=null)this.setXyToZ(Double.valueOf(s) );
	return true;
}

public Properties getProperties(){
	Properties props= new Properties();
	props.setProperty("projectDirectory",this.getProjectDirectory());
	props.setProperty("imageName",this.getImageFileName());
	props.setProperty("intensityOffset",String.valueOf(this.getIntensityOffset()));
	props.setProperty("xyToZ",String.valueOf(this.getXyToZ()));

	
	return props;
}

public void depositMsg(String msg){
	synchronized (msgBuffer){
		msgBuffer.append(msg+ "\n");
	}
}

public String getMsg(){
	String result="";
	synchronized (msgBuffer){
		result=msgBuffer.toString();
		msgBuffer.delete(0, msgBuffer.length());
	}
	return result;
}

}
