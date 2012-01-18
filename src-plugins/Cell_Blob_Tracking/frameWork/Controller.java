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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

public abstract class Controller<T extends Trackable > {


	protected int currentFrameNumber=0;
	protected int currentSliceNumber=0;
	protected int currentChannelNumber=0;
	protected boolean buisy=false;
	protected double xyToZ=3.5;
	protected double mouseX=0;
	protected double mouseY=0;
	protected double mouseZ=0;
	protected int selectedSequenceId;
	
	protected Model<T,?> model;

	 
	protected Controller( Model<T,?> mod){
	
	}
    
	
private synchronized void updatePosition(int x,int y, int slice ,int frame, int channel){

	if(buisy) return;
	buisy=true;
	System.out.println("oframe:"+ currentFrameNumber+ "   oslice:"+ currentSliceNumber+ "  ochannel:"+currentChannelNumber );
	
	currentSliceNumber=slice;
	currentFrameNumber=frame;
	currentChannelNumber=channel;
	//if (mainImage.getNFrames()<=1 && mainImage.getNSlices()>1) mainImage.setPosition(channel+1, frame+1, slice+1); // switch dimensions
	//else mainImage.setPosition(channel+1, slice+1, frame+1);
	//this.upDateImages(currentFrameNumber, currentSliceNumber, currentChannelNumber, false);
	System.out.println("nframe:"+ currentFrameNumber+ "   nslice:"+ currentSliceNumber+ "  nchannel:"+currentChannelNumber );
	//mainImage.setSlice(slice+1);
	
	buisy=false;

}

public abstract void click(long[] position);


}
