package frameWork;

import ij.IJ;
import ij.ImagePlus;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

import blobTracking.Blob;
import blobTracking.BlobFactory;

import tools.ImglibTools;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Model <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Observable{ 
	




public double xyToZ=3.5;
private boolean isVolume=false;
private boolean isTimeSequence=false;
private boolean isMultiChannel=false;
private int numberOfChannels;
private int numberOfFrames;
private int numberOfSlices;
private	RandomAccessibleInterval<IT> image;
private SortedMap <Integer, MovieChannel <IT> > channels;
private SortedMap <Integer, TrackingChannel <? extends Trackable,IT> > trackingChannels;

public void setVolume(boolean isVolume) {
	this.isVolume = isVolume;
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
	
	numberOfFrames=imp.getNFrames();
	numberOfChannels=imp.getNChannels();
	numberOfSlices=imp.getNSlices();
	
	setTimeSequence(imp.getNFrames()>1);
	setMultiChannel(imp.getNChannels()>1);
	setVolume(imp.getNSlices()>1);	
	
	if(isVolume&&!isTimeSequence){
 	   isVolume=false;
 	   isTimeSequence=true;
 	   System.out.println("SWITCHING DIMENSIONS");
    }
	
	
	if(isMultiChannel()){
       image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,image.numDimensions()-1),2) );
    if(image.numDimensions()==5)  	   
       image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,3),2 ));  
    }   
	   
	channels=new TreeMap<Integer,MovieChannel <IT> >();
	trackingChannels= new TreeMap<Integer, TrackingChannel <? extends Trackable,IT> >();
	
	if(isMultiChannel){
		for(int i=0;i<numberOfChannels;i++){
			MovieChannel<IT> chann= new MovieChannel<IT>(Views.hyperSlice(image, image.numDimensions()-1, i));
			channels.put(i, chann);
			TrackingChannel<Blob,IT> tc=new TrackingChannel<Blob,IT>(new BlobFactory<IT>(chann), chann.getNumberOfFrames() );
			trackingChannels.put(i, tc);
			
			for(int j=0;j<tc.getNumberOfFrames();j++){

				if(i%2==0) tc.addTrackable(new Blob(2,j,20 +Math.cos(j/15.0f)*25,70+ Math.sin(j/35.0f)*25,15,4, i));
				else tc.addTrackable(new Blob(2,j,20 +Math.cos(j/15.0f)*25,70- Math.sin(j/35.0f)*25,15,4, i));
				//model.addTrackable(new Blob(0,i,20 +Math.sin(i/15.0f)*25,20+ Math.sin(i/15.0f)*25,15,4, 0), );
			    //model.addTrackable(new Blob(1,i,70 +Math.sin(i/15.0f)*25,20+ Math.sin(i/45.0f)*25,15,4,0),0);
			    //tc.addTrackable(new Blob(2,i,20 +Math.cos(i/15.0f)*25,70+ Math.sin(i/35.0f)*25,15,4, 0));
			    
			 	   
			}
			
			
		}
	}else{
		MovieChannel<IT> chann= new MovieChannel<IT>( image);
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
	return trackingChannels.get(channel).getTrackablesForFrame(frame);
}

public Trackable getTrackable(int seqId, int frameId, int channel){
	return trackingChannels.get(channel).getTrackable(seqId, frameId);	
}

public Sequence<? extends Trackable> getSequence(int id, int channel){
	return trackingChannels.get(channel).getSequence(id);
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

	
}
