package frameWork;


import ij.ImagePlus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Model <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Observable{ 
	




public double xyToZ=3.5;
private boolean isVolume=false;
private boolean isTimeSequence=false;
private boolean isMultiChannel=false;
private boolean switchedDimensions=false;
private final int numberOfChannels;
private int numberOfFrames;
private final int numberOfSlices;
private	RandomAccessibleInterval<IT> image;
private final SortedMap <Integer, MovieChannel <IT> > channels;
private final SortedMap <Integer, TrackingChannel <? extends Trackable,IT> > trackingChannels;
private int nextSequqnceId=0;
private int nextTCId=0;

public synchronized int getNextSequqnceId(){
	nextSequqnceId++;
	return nextSequqnceId-1;
}

public synchronized int getNextTCId(){
	nextTCId++;
	return nextTCId-1;
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

public Model(ImagePlus imp){
	
	image=ImagePlusAdapter.wrap(imp);
	

	
	
	setTimeSequence(imp.getNFrames()>1);
	setMultiChannel(imp.getNChannels()>1);
	setVolume(imp.getNSlices()>1);	
	
	System.out.println("iv:" + isVolume+ "  its:" +isTimeSequence + "  imc:"+ isMultiChannel);
	
	if(isVolume&&!isTimeSequence && !imp.isHyperStack()){
 	   isVolume=false;
 	   isTimeSequence=true;
 	  switchedDimensions=true;
 	   System.out.println("SWITCHING DIMENSIONS");
    }
	
	if(isMultiChannel)numberOfChannels=imp.getNChannels();
	else numberOfChannels=1;
	
	
	
	if(isMultiChannel()){
       image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,image.numDimensions()-1),2) );
    if(image.numDimensions()==5)  	   
       image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,3),2 ));  
    }   
	   
	channels=new TreeMap<Integer,MovieChannel <IT> >();
	trackingChannels= new TreeMap<Integer, TrackingChannel <? extends Trackable,IT> >();
	
	if(!switchedDimensions){
		numberOfFrames=imp.getNFrames();
		numberOfSlices=imp.getNSlices();
	}
	else {
		numberOfFrames=imp.getNSlices();
		numberOfSlices=imp.getNFrames();
	}
	
	if(isMultiChannel){
		for(int i=0;i<numberOfChannels;i++){
			MovieChannel<IT> chann= new MovieChannel<IT>(Views.hyperSlice(image, image.numDimensions()-1, i),i,numberOfFrames );
			channels.put(i, chann);
				
			
		}
	}else{
		MovieChannel<IT> chann= new MovieChannel<IT>( image,0, numberOfFrames);
		channels.put(0, chann);
	}
	
	
	
	System.out.println("numOfFrames:" +numberOfFrames);
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
	TrackingChannel<? extends Trackable, IT> tc=trackingChannels.get(channel);
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

public synchronized RandomAccessibleInterval<IT> getXTProjections(int channel){
	return channels.get(channel).getXTProjections();
}

public synchronized RandomAccessibleInterval<IT> getYTProjections(int channel){
	return channels.get(channel).getYTProjections();
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

public TrackingChannel<? extends Trackable, IT> getTrackingChannel(int id){
	return trackingChannels.get(id);
}

public void addTrackingChannel(TrackingChannel<? extends Trackable,IT> tc, int key){
	trackingChannels.put(key, tc);
}

public List<TrackingChannel<? extends Trackable,IT>> getTCsAssociatedWithChannel(int id){
	List<TrackingChannel<? extends Trackable,IT>> result= new ArrayList<TrackingChannel<? extends Trackable,IT>>();
	Collection <TrackingChannel<? extends Trackable,IT>> coll= this.trackingChannels.values();
	for(TrackingChannel<? extends Trackable,IT> tc: coll){
		if(tc.isAssociatedWithMovieChannel(id)) result.add(tc);
	}
	return result;
}

public List<Sequence<? extends Trackable>> getAllSequencies(){
	List <Sequence<? extends Trackable>>results=new ArrayList<Sequence<? extends Trackable>>();
	for(TrackingChannel<? extends Trackable,IT> tc: this.trackingChannels.values()){
		results.addAll(tc.getSeqsCollection());
	}
	return results;
}



}
