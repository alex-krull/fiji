package frameWork;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.ContrastEnhancer;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import blobTracking.Blob;
import blobTracking.BlobController;
import blobTracking.BlobFactory;

import tools.ImglibTools;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class  Controller< IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {


	
	protected double xyToZ=3.5;
	public int selectedSequenceId;
	
	protected Model<IT> model;
	private SortedMap <Integer, ChannelController <? extends Trackable,IT> > channelControllers;

	 
	public Controller( Model<IT> mod){
		model =mod;
		channelControllers=	new TreeMap<Integer, ChannelController<? extends Trackable,IT>>();
		
		TrackingChannel<Blob,IT> tc= new TrackingChannel<Blob,IT>(new BlobFactory <IT>(model.getMovieChannel(0)),model.getNumberOfFrames() );
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
