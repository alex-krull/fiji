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

	@Override
	public Blob loadTrackableFromString(String s) {
		System.out.println(s);
	
		String[] values=s.split("\t");
		System.out.println(values.length);
		
		System.out.println(values[1]);
		System.out.println(values[2]);
		System.out.println(values[0]);
		int sId= Integer.valueOf(values[0]);
		int fNum= Integer.valueOf(values[1]);
		double x= Double.valueOf(values[2]);
		double y= Double.valueOf(values[3]);
		double z= Double.valueOf(values[4]);
		double sigma= Double.valueOf(values[5]);
		double sigmaZ= Double.valueOf(values[6]);
		
		return new Blob(sId, fNum, x, y, z, sigma, this.getId());
	}
	
	

}
