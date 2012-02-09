package blobTracking;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Factory;
import frameWork.Frame;
import frameWork.MovieChannel;
import frameWork.Sequence;

public class BlobFactory  <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> implements Factory <Blob, IT> {

	private MovieChannel<IT> mChannel;
	public BlobFactory(MovieChannel<IT> mv){
		mChannel=mv;
	}
	
	@Override
	public Sequence<Blob> produceSequence(int ident, String lab) {
		return new BlobSequence( ident,  lab);
	}
	
	public Frame<Blob,IT> produceFrame(int frameNum) {		
		return new BlobFrame<IT>(frameNum, mChannel.getMovieFrame(frameNum));
	}

}
