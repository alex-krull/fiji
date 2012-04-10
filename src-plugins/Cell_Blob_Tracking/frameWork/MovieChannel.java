package frameWork;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;

public class MovieChannel <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> {
	private final	List<MovieFrame <IT>> frames;
	
	private RandomAccessibleInterval<IT> zProjections = null;
	private RandomAccessibleInterval<IT> xProjections = null;
	private RandomAccessibleInterval<IT> yProjections = null;
	private RandomAccessibleInterval<IT> xtProjections= null;
	private RandomAccessibleInterval<IT> ytProjections= null; 
	private final RandomAccessibleInterval<IT> image;
	private final long numOfFrames;
	private final int MovieChannelId;
	private final boolean isVolume;
	public boolean isVolume() {
		return isVolume;
	}

	private final boolean isTimeSeries;
	
	

	public class ProjectionThreadKymographs extends Thread{
		private final MovieChannel <IT> channel;
		ProjectionThreadKymographs(MovieChannel <IT> chan){
			channel=chan;
		}
		
		@Override
		public void run() {
	       channel.getXTProjections();
	       channel.getYTProjections();
	    }
	}
	
	public class ProjectionThread extends Thread{
		private final MovieChannel <IT> channel;
		ProjectionThread(MovieChannel <IT> chan){
			channel=chan;
		}
		
		@Override
		public void run() {
	       for(int i=0;i<channel.getNumberOfFrames();i++){
	    	   MovieFrame<IT> f=channel.getMovieFrame(i);
	    	   f.getXProjections();
	    	   f.getYProjections();
	    	   f.getZProjections(); 
	       }
	    }
	}
	
	
	
	public MovieChannel(RandomAccessibleInterval<IT> view, int id, int nOfFrames, int cBackGround){
		MovieChannelId=id;
		image=view;
		numOfFrames=nOfFrames;
		isTimeSeries = numOfFrames>1;
		isVolume = (image.numDimensions()>3|| (!isTimeSeries && image.numDimensions()>2) );
		
		
		frames= new ArrayList<MovieFrame<IT>>();
		
		for(int i=0;i<numOfFrames;i++){
			MovieFrame<IT> f=new MovieFrame<IT>(i, getFrameView(i),cBackGround);
			frames.add(f);
			
		}
		
		if(isVolume){
			ProjectionThread pt= new ProjectionThread(this);
			pt.setPriority(Thread.MIN_PRIORITY);
			pt.start();
		}
		
		ProjectionThreadKymographs ptk= new ProjectionThreadKymographs(this);
		ptk.start();
	}
	
	public RandomAccessibleInterval<IT> getXProjections(){
		if(xProjections==null) xProjections=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(image,0),0,1) ),0  ) ); 
		return xProjections;
	}

	public RandomAccessibleInterval<IT> getYProjections(){
		if(yProjections==null) yProjections=ImglibTools.projection(image,1);
		return yProjections;
	}

	public RandomAccessibleInterval<IT> getZProjections(){
		if(zProjections==null){
			if(isVolume) zProjections=ImglibTools.projection(image,2);
			else zProjections=image;
			
		}
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

		
	
	private  RandomAccessibleInterval<IT> getFrameView(int frameNumber){
//		System.out.println("fn:"+frameNumber);
		if(isTimeSeries) return Views.hyperSlice(image, image.numDimensions()-1, frameNumber);
		else return image;
		
	}

	public MovieFrame<IT> getMovieFrame(int frame){
		
		return frames.get(frame);
	}
	
	public long getNumberOfFrames(){
		return numOfFrames;
	}
	
	public int getId(){
		return MovieChannelId;
	}
}
