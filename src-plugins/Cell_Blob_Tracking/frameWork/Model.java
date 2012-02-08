package frameWork;

import ij.IJ;
import ij.ImagePlus;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

import tools.ImglibTools;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Model<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Observable{ 
	
public class ProjectionThread extends Thread{
	private Model <T,IT> model;
	ProjectionThread(Model <T,IT> mod){
		model=mod;
	}
	
	public void run() {
       for(int i=0;i<model.getNumberOfFrames();i++){
    	   Frame<T,IT> f=model.getFrame(i);
    	   f.getXProjections();
    	   f.getYProjections();
    	   f.getZProjections(); 
       }
    }
}


private	List<Frame <T,IT>> frames;
private	RandomAccessibleInterval<IT> image;
private Factory<T,IT> factory;
private SortedMap <Integer, Sequence<T>> Sequences;
public T selected;
public double xyToZ=3.5;
private boolean isVolume=false;
private boolean isTimeSequence=false;
private boolean isMultiChannel=false;

private RandomAccessibleInterval<IT> zProjections = null;
private RandomAccessibleInterval<IT> xProjections = null;
private RandomAccessibleInterval<IT> yProjections = null;
private RandomAccessibleInterval<IT> xtProjections= null;
private RandomAccessibleInterval<IT> ytProjections= null; 

public int getNumberOfFrames(){
	return frames.size();
}

public synchronized RandomAccessibleInterval<IT> getXProjections(){
	if(xProjections==null) xProjections=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(image,0),0,1) ),0  ) ); 
	return xProjections;
}

public synchronized RandomAccessibleInterval<IT> getYProjections(){
	if(yProjections==null) yProjections=ImglibTools.projection(image,1);
	return yProjections;
}

public synchronized RandomAccessibleInterval<IT> getZProjections(){
	if(zProjections==null) zProjections=ImglibTools.projection(image,2);
	return zProjections;
}

public boolean isVolume() {
	return isVolume;
}


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


public void setMultiChannel(boolean isMultiChannel) {
	this.isMultiChannel = isMultiChannel;
}

public Model(ImagePlus imp , Factory<T,IT> fact){
	Sequences= new TreeMap<Integer, Sequence<T>>();
	image=ImagePlusAdapter.wrap(imp);
	factory=fact;
	frames= new ArrayList<Frame<T,IT>>();
	
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
	   
	   
	
	for(int i=0;i<50;i++){
		Frame<T,IT> f=factory.produceFrame(i,getFrameView(i,0));
		frames.add(f);
		
	}
	
	ProjectionThread pt= new ProjectionThread(this);
	pt.start();
	
}


public SortedMap <Integer, Sequence<T>> getSeqs(){
	return Sequences;
}

public Sequence<T> getSequence(int id){
	return Sequences.get(id);
}

public void optimizeFrame(int frameId){
	Frame<T,IT> f= frames.get(frameId);
	f.optimizeFrame();
}

public int selectAt(int x, int y, int z, int frameId, int channel){
	Frame<T,IT> f= frames.get(frameId);
	return f.selectAt(x, y, z);
}

public void addTrackable(T trackable){
	
	frames.get(trackable.frameId).addTrackable(trackable);
	Sequence<T> sequence= Sequences.get(trackable.sequenceId);
	if(sequence==null){
		
		sequence=factory.produceSequence(trackable.sequenceId, Integer.toString(trackable.sequenceId));
		System.out.println("Adding Seq!");
		Sequences.put(trackable.sequenceId, sequence);
	}
	sequence.addTrackable(trackable);
}

private  RandomAccessibleInterval<IT> getFrameView(int frameNumber, int channelNumber){
//	System.out.println("fn:"+frameNumber);
	return Views.hyperSlice(image, 3, frameNumber);
}

public Frame<T,IT> getFrame(int frame){
	return frames.get(frame);
}

public List<T> getTrackablesForFrame(int frame){
	return frames.get(frame).getTrackables();
}

public T getTrackable(int seqId, int frameId){
	Sequence<T> sequence= Sequences.get(seqId);	
	if (sequence==null){
		
		return null;
	}
	return sequence.getTrackableForFrame(frameId);
	
}

public RandomAccessibleInterval<IT> getImage(){
	return image;
}

public void makeChangesPublic(){
	setChanged();
	notifyObservers();
}


	
	
}
