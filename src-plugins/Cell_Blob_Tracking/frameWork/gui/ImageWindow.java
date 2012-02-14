package frameWork.gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.plugin.ContrastEnhancer;

import java.awt.Color;
import java.util.List;
import java.util.SortedMap;


import tools.ImglibTools;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import frameWork.Model;
import frameWork.Sequence;
import frameWork.Trackable;
import frameWork.TrackingChannel;

public abstract class ImageWindow  < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<IT>{
	
	protected ImagePlus imp=null;
	protected RandomAccessibleInterval<IT> image;
	protected ImageCanvas canvas;
	protected Overlay ov;
	protected double scaleX=1;
	protected double scaleY=1;
	protected double transX=0;
	protected double transY=0;
	protected int xSize=-1;
	protected int ySize=-1;

	protected RandomAccessibleInterval<IT> toDraw;
	
	public ImageWindow(Model<IT> mod, RandomAccessibleInterval<IT> img, String title, ViewModel<IT> vm, ImagePlus imagePlus){
		super(mod, title,vm);
		imp=imagePlus;
		
		ov= new Overlay();
		image=img;
	 	if(imp==null) rePaint(vm.getPosition(),true);
	}
	
	public void reDraw(long[] position, boolean rePaintImage){
		if(rePaintImage){
			if (toDraw.numDimensions()>2) toDraw=Views.hyperSlice(toDraw,2,position[4]);
			if(imp==null) 
				imp= ImageJFunctions.show(toDraw,caption);
			else{
						
		//	long[] mins= {(long)Math.min(transX,0), (long)Math.min(transY,0)};
		//	long[] maxs= {(long)transX+xSize, (long)transY+ySize};
			
			if(xSize<0 )xSize= (int)(scaleX*((int)toDraw.max(0)- (int)toDraw.min(0)));
			if(ySize<0 )ySize= (int)(scaleY*((int)toDraw.max(1)- (int)toDraw.min(1)));	
			
			long minX=(long)((double)(transX)/(double)scaleX)  ;
			long minY=(long)((double)(transY)/(double)scaleY)  ;
			long maxX=minX+(long)((double)xSize/(double)scaleX)  +2;
			long maxY=minY+(long)((double)ySize/(double)scaleY ) +2;
			
			minX=Math.max(minX, 0);
			minY=Math.max(minY, 0);
			maxX=Math.min(maxX, toDraw.max(0));
			maxY=Math.min(maxY, toDraw.max(1));
			
			long[] minsP= {minX, minY};
			long[] maxsP= {maxX,maxY};
			
			
			
			
			RandomAccessibleInterval<IT> temp=  Views.zeroMin(Views.interval(toDraw, minsP, maxsP));
			
			temp=ImglibTools.scaleByFactor(temp, 0, scaleX);
			temp=ImglibTools.scaleByFactor(temp, 1, scaleY);
				
			long[] mins= {(long)transX-(long)((double)minX*scaleX), (long)transY-(long)((double)minY*scaleY)};
			long[] maxs= {Math.min(xSize+mins[0], temp.max(0)), Math.min(ySize+mins[1], temp.max(1))};
				
								
			toDraw= Views.zeroMin( Views.interval(temp, mins, maxs) );
			//if(toDraw.max(0)!=xSize) toDraw= ImglibTools.scaleByFactor(toDraw, 0, (double)xSize/(double)toDraw.max(0));
		//	if(toDraw.max(1)!=ySize) toDraw= ImglibTools.scaleByFactor(toDraw, 0, (double)ySize/(double)toDraw.max(1));
			
			ImagePlus impl=ImageJFunctions.wrap( toDraw , caption);
			//	transX=(scaleX*(double)impl.getProcessor().getWidth()- (double)impl.getProcessor().getWidth())/2.0;
			//	transY=(scaleY*(double)impl.getProcessor().getHeight()- (double)impl.getProcessor().getHeight())/2.0;
			ContrastEnhancer ce= new ContrastEnhancer();
			ce.stretchHistogram(impl.getProcessor(), 0.5); 
			
    	
			
	
			this.imp.setProcessor(impl.getProcessor());
			}
	
		}
		imp.setOverlay(ov);
		imp.updateAndDraw();
    	
	}

	protected void clearOverlay(){
		ov= new Overlay();
	}
	
	protected void addKymoXOverlayes(){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
	
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){
		
			if(tc==null) return;
			SortedMap <Integer,? extends Sequence< ? extends Trackable>> seqs= tc.getSeqs();
			if(seqs!=null) for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<? extends Trackable> seq = seqs.get(i);
			if(seq!=null) seq.getKymoOverlayX(ov,scaleX,scaleY, transX, transY);
			} 
		}
	}
	
	protected void addKymoYOverlayes(){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){
		if(tc==null) return;
		SortedMap <Integer,? extends Sequence< ? extends Trackable>> seqs= tc.getSeqs();
	
		if(seqs!=null) for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<? extends Trackable> seq = seqs.get(i);
			if(seq!=null) seq.getKymoOverlayY(ov,scaleX,scaleY);
		}
		}
	}

	protected void addXOverlayes(int frameNumber){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		   int selected =viewModel.getSelectedSequenceId();
		   if(trackables!=null) for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId,viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeX(ov,t.sequenceId==selected,c);
			   
		   }
		   
		}
		   
	}
	
	protected void addYOverlayes(int frameNumber){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		 int selected =viewModel.getSelectedSequenceId();
		 if(trackables!=null) for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId, viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeY(ov,t.sequenceId==selected,c);
			   
		   }
		}
	}
	
	protected void addZOverlayes(int frameNumber){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		 int selected =viewModel.getSelectedSequenceId();
		 if(trackables!=null)for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId, viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeZ(ov,t.sequenceId==selected,c);
			   
		   }
		} 
	}
	
	
	
	protected void addYLineOverlay(double position){
		   
		
		   ov.add(new Line(0*scaleX,(position+0.5)*scaleY-transY,this.model.getImage().dimension(0)*scaleX ,(position+0.5)*scaleY-transY));		  			   
		   
	}
	
	protected void addXLineOverlay(double position){
		 
		
		   ov.add(new Line((position+0.5)*scaleX-transX,0*scaleY,(position+0.5)*scaleX-transX,model.getImage().dimension(1) *scaleY) );		  			   
		  
	}
}

