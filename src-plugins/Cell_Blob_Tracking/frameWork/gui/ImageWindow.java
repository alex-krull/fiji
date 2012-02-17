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
	
	public ImageWindow(Model<IT> mod, RandomAccessibleInterval<IT> img, String title, ViewModel<IT> vm, ImagePlus imagePlus, int capacity){
		super(mod, title,vm, capacity);
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
				
			System.out.println("/////////////////////////////toDraw.dimension(0):"+toDraw.dimension(0));
			
			if(xSize<0 )xSize= (int)(scaleX*((int)toDraw.dimension(0)));
			if(ySize<0 )ySize= (int)(scaleY*((int)toDraw.dimension(1)));	
			
			long minX=(long)((double)(transX)/(double)scaleX) -4;
			long minY=(long)((double)(transY)/(double)scaleY) -4;
			
			
			minX=Math.max(minX, 0);
			minY=Math.max(minY, 0);
			
			long maxX=minX+8+(long)((double)(xSize)/(double)scaleX) ;
			long maxY=minY+8+(long)((double)(ySize)/(double)scaleY) ;
			
			if(maxX>=toDraw.max(0)){
				maxX=toDraw.max(0);
				minX=Math.max( (long)((double)maxX-(double)xSize/(scaleX)-8) , 0);
			}
			
			if(maxY>=toDraw.max(1)){
				maxY=toDraw.max(1);
				minY=Math.max( (long)((double)maxY-(double)ySize/(scaleY)-8) , 0);
			}
			
						
			System.out.println("			(maxX-minX)*scaleX:" + (maxX-minX)*scaleX);
			System.out.println("			 maxX:" + maxX*scaleX);
			System.out.println("			 minX:" + minX*scaleX);
			
			
			long[] minsP= {minX, minY};
			long[] maxsP= {maxX,maxY};
			
			
			
			
			toDraw=  Views.zeroMin(Views.interval(toDraw, minsP, maxsP));
			
			RandomAccessibleInterval<IT> temp=ImglibTools.scaleByFactor(toDraw, 0, scaleX);
			temp=ImglibTools.scaleByFactor(temp, 1, scaleY);
			
			minsP[0]=(long)(-scaleX*(double)minsP[0]);
			minsP[1]=(long)(-scaleY*(double)minsP[1]);
			
//			temp= Views.zeroMin(temp);
			
						
			
			minX=(long) transX+minsP[0];
			minY=(long) transY+minsP[1];
			
			minX=Math.max(minX, 0);
			minY=Math.max(minY, 0);
			
			maxX=minX+xSize-1 ;
			maxY=minY+ySize-1 ;
		
			if(maxX>temp.max(0)){
			
				maxX=temp.max(0);
				minX=Math.max( maxX-xSize+1 , 0);
			}
			
			if(maxY>=temp.max(1)){
				maxY=temp.max(1);
				minY=Math.max( maxY-ySize+1 , 0);
			}
			
			
			
			long[] mins= {minX,minY};
			
			
			long[] maxs= {maxX,maxY};
				
								
			
			//toDraw=temp;
			//if(toDraw.max(0)!=xSize) toDraw= ImglibTools.scaleByFactor(toDraw, 0, (double)xSize/(double)(toDraw.max(0)+1));
			//if(toDraw.max(1)!=ySize) toDraw= ImglibTools.scaleByFactor(toDraw, 0, (double)ySize/(double)(toDraw.max(1)+1));
		//	toDraw=ImglibTools.resizeTo(toDraw, xSize, ySize);
			System.out.println("            xSize:" +xSize + "  ySize:" + ySize);
			System.out.println("            minX:" +mins[0] + "  minY:" + mins[1]);
			System.out.println("            maxX:" +maxs[0] + "  maxY:" + maxs[1]);
			System.out.println("            tempMaxX:" +temp.max(0) + "  temMaxY:" + temp.max(1));
			System.out.println("            tempMinX:" +temp.min(0) + "  temMinY:" + temp.min(1));
			System.out.println("            transX:" +transX + "  transY:" + transY);
			
			temp= Views.zeroMin( Views.interval(temp, mins, maxs) );
			
			ImagePlus impl=ImageJFunctions.wrap( temp , caption);
		//		transX=(scaleX*(double)impl.getProcessor().getWidth()- (double)impl.getProcessor().getWidth())/2.0;
		//		transY=(scaleY*(double)impl.getProcessor().getHeight()- (double)impl.getProcessor().getHeight())/2.0;
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

