package frameWork;




import java.awt.event.MouseEvent;

import java.util.SortedMap;
import java.util.TreeMap;

import blobTracking.Blob;
import blobTracking.BlobController;
import blobTracking.BlobTrackingChannel;



import net.imglib2.type.NativeType;

import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;


public class  Controller< IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {


	
	protected double xyToZ=3.5;
	public int selectedSequenceId;
	
	protected Model<IT> model;
	private SortedMap <Integer, ChannelController <? extends Trackable,IT> > channelControllers;

	 
	public Controller( Model<IT> mod){
		model =mod;
		channelControllers=	new TreeMap<Integer, ChannelController<? extends Trackable,IT>>();
		
		TrackingChannel<Blob,IT> tc= new BlobTrackingChannel<IT>(model.getMovieChannel(0) );
		BlobController<IT> bc= new BlobController<IT>(model,tc);
		channelControllers.put(0, bc);
		model.addTrackingChannel(tc,0);
		
		for(int j=0;j<tc.getNumberOfFrames();j++){
			tc.addTrackable(new Blob(2,j,20 +Math.cos(j/15.0f)*25,70+ Math.sin(j/35.0f)*25,15,4, 0));			 	   
		}
	}
	


public void click(long[] position, MouseEvent e){
	ChannelController<? extends Trackable,IT> cc= channelControllers.get((int)position[4]);
	System.out.println("click1");
	if (cc!=null){
		System.out.println("click2");
		cc.click(position, e);
	}
}

}
