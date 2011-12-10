package frameWork;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.Type;
import net.imglib2.view.Views;

public class Controler<T extends Trackable, IT extends Type<IT>> { 

private	List<Frame <T,IT>> frames;
private	RandomAccessibleInterval<IT> image;
private Frame<T,IT> factory;
private SortedMap <Integer, Sequence<T>> Sequences;
public T selected;

public Controler(RandomAccessibleInterval<IT> img , Frame<T,IT> fact){
	Sequences= new TreeMap<Integer, Sequence<T>>();
	image=img;
	factory=fact;
	frames= new ArrayList<Frame<T,IT>>();
	for(int i=0;i<40000;i++){
		Frame<T,IT> f=factory.createFrame(i,getFrameView(0,0));
		frames.add(f);
		
	}
	
}

public void addTrackable(T trackable){
	
	frames.get(trackable.frameId).addTrackable(trackable);
	Sequence<T> sequence= Sequences.get(trackable.sequenceId);
	if(sequence==null){
		
		sequence=new Sequence<T>(trackable.sequenceId, Integer.toString(trackable.sequenceId));
		System.out.println("Adding Seq!");
		Sequences.put(trackable.sequenceId, sequence);
	}
	sequence.addTrackable(trackable);
}

private  RandomAccessibleInterval<IT> getFrameView(int frameNumber, int channelNumber){
	return Views.hyperSlice(image, 0, 0);
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



	
	
}
