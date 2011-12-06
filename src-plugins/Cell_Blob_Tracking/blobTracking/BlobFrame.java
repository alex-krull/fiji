package blobTracking;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.Type;
import frameWork.Frame;

public class BlobFrame <IT extends Type<IT>> extends Frame<Blob, IT>{

	@Override
	public void optimizeFrame() {
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public Frame<Blob,IT> createFrame(int FrameNumber, RandomAccessibleInterval<IT> view) {
		
		frameView=view;
		return new BlobFrame<IT>();
	}

}
