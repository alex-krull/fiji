package blobTracking;


import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.MovieChannel;
import frameWork.Sequence;
import frameWork.TrackingChannel;
import frameWork.TrackingFrame;

public class BlobTrackingChannel  <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> extends TrackingChannel <Blob, IT> {

	private final MovieChannel<IT> mChannel;
	public BlobTrackingChannel( MovieChannel<IT> mv, int iD){
		super(iD);
		mChannel=mv;
		initialize(mv.getNumberOfFrames());
	}
	
	@Override
	public Sequence<Blob> produceSequence(int ident, String lab) {
		return new BlobSequence( ident,  lab);
	}
	
	@Override
	public TrackingFrame<Blob,IT> produceFrame(int frameNum) {		
		return new BlobFrame<IT>(frameNum, mChannel.getMovieFrame(frameNum));
	}
	
	@Override
	public boolean isAssociatedWithMovieChannel(int id){
		return (mChannel.getId()==id);
	}

}
