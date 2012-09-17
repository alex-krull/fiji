package blobTracking;



import net.imglib2.IterableInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.MovieFrame;
import frameWork.TrackingFrame;

public class BlobFrame <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends TrackingFrame<Blob, IT>{
	
	
	
	private final double backProb=0.1;
	private IterableInterval<IT> iterableFrame;
	public BlobFrame(int frameNum, MovieFrame<IT> mv, BlobPolicy<IT> pol){	
		super(frameNum, pol,mv);
		
	}


	
	


}

