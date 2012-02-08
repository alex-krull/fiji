package frameWork;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.SortedMap;
import java.util.TreeMap;

import tools.ImglibTools;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public class Channel <T extends Trackable ,IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends Observable{
	private	List<Frame <T,IT>> frames;
	
	private Factory<T,IT> factory;
	private SortedMap <Integer, Sequence<T>> Sequences;
	private RandomAccessibleInterval<IT> zProjections = null;
	private RandomAccessibleInterval<IT> xProjections = null;
	private RandomAccessibleInterval<IT> yProjections = null;
	private RandomAccessibleInterval<IT> xtProjections= null;
	private RandomAccessibleInterval<IT> ytProjections= null; 
	private RandomAccessibleInterval<IT> image;
	
	

	public class ProjectionThreadKymographs extends Thread{
		private Channel <T,IT> channel;
		ProjectionThreadKymographs(Channel <T,IT> chan){
			channel=chan;
		}
		
		public void run() {
	       for(int i=0;i<channel.getNumberOfFrames();i++){
	    	   Frame<T,IT> f=channel.getFrame(i);
	    	   f.getXProjections();
	    	   f.getYProjections();
	    	   f.getZProjections(); 
	       }
	    }
	}
	
	public class ProjectionThread extends Thread{
		private Channel <T,IT> channel;
		ProjectionThread(Channel <T,IT> chan){
			channel=chan;
		}
		
		public void run() {
	       for(int i=0;i<channel.getNumberOfFrames();i++){
	    	   Frame<T,IT> f=channel.getFrame(i);
	    	   f.getXProjections();
	    	   f.getYProjections();
	    	   f.getZProjections(); 
	       }
	    }
	}
	
	
	
	public Channel(Factory<T,IT> fact, RandomAccessibleInterval<IT> view){
		image=view;
		Sequences= new TreeMap<Integer, Sequence<T>>();
		factory=fact;
		frames= new ArrayList<Frame<T,IT>>();
		
		for(int i=0;i<50;i++){
			Frame<T,IT> f=factory.produceFrame(i,getFrameView(i,0));
			frames.add(f);
			
		}
		
		//ProjectionThread pt= new ProjectionThread(this);
		//pt.start();
	}
	
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
	
	public synchronized RandomAccessibleInterval<IT> getXTProjections(){
		if(xtProjections==null) xtProjections=Views.zeroMin( Views.invertAxis(    Views.rotate(ImglibTools.projection(getZProjections(),0),0,1 ),0 ) ) ;

		return xtProjections;
	}
	
	public synchronized RandomAccessibleInterval<IT> getYTProjections(){
		if(ytProjections==null) ytProjections=Views.zeroMin(    ImglibTools.projection(getZProjections(),1) )  ;
		return ytProjections;
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
//		System.out.println("fn:"+frameNumber);
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
	
	public void makeChangesPublic(){
		setChanged();
		notifyObservers();
	}
}
