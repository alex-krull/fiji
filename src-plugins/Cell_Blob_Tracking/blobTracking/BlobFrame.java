package blobTracking;

import java.util.ArrayList;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.Type;
import frameWork.Frame;

public class BlobFrame <IT extends Type<IT>> extends Frame<Blob, IT>{
	private double backProb=0.9;
	public BlobFrame(int frameNum, RandomAccessibleInterval<IT> view){
		super(frameNum, view);
	}

	@Override
	public void optimizeFrame() {
		// TODO Auto-generated method stub
		
	}
	
	private void doEStep(){
			
	}

	
	@Override
	public Frame<Blob,IT> createFrame(int frameNum, RandomAccessibleInterval<IT> view) {		
		return new BlobFrame<IT>(frameNum, view);
	}

}
