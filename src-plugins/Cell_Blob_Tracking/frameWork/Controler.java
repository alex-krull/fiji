package frameWork;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.Type;

public class Controler<T extends Trackable, IT extends Type<IT>> { 

private	List<Frame <T,IT>> frames;
private	RandomAccessibleInterval<IT> image;
private Frame<T,IT> factory;

public Controler(RandomAccessibleInterval<IT> img , Frame<T,IT> fact){
	image=img;
	factory=fact;
	frames= new ArrayList<Frame<T,IT>>();
	frames.add(factory.createFrame(0,getFrameView(0,0)));
}

public void addTrackable(T trackable, int frameNumber){
	frames.get(frameNumber).addTrackable(trackable);
}

private  RandomAccessibleInterval<IT> getFrameView(int frameNumber, int channelNumber){
	return null;
}




	
	
}
