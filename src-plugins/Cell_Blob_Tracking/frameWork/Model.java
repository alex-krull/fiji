package frameWork;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Model<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Observable{ 

private	List<Frame <T,IT>> frames;
private	RandomAccessibleInterval<IT> image;
private Factory<T,IT> factory;
private SortedMap <Integer, Sequence<T>> Sequences;
public T selected;
public double xyToZ=3.5;

public Model(RandomAccessibleInterval<IT> img , Factory<T,IT> fact){
	Sequences= new TreeMap<Integer, Sequence<T>>();
	image=img;
	factory=fact;
	frames= new ArrayList<Frame<T,IT>>();
	for(int i=0;i<40000;i++){
		Frame<T,IT> f=factory.produceFrame(i,getFrameView(i,0));
		frames.add(f);
		
	}
	
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
	return Views.hyperSlice(image, 2, frameNumber);
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
