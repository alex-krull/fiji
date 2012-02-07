package frameWork.gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.plugin.ContrastEnhancer;

import java.awt.Color;
import java.util.List;
import java.util.SortedMap;

import org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID;

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

public abstract class ImageWindow  <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<T,IT>{
	
	protected ImagePlus imp=null;
	protected RandomAccessibleInterval<IT> image;
	protected ImageCanvas canvas;
	protected Overlay ov;
	protected double scaleX=1;
	protected double scaleY=1;
	protected double transX=0;
	protected double transY=0;

	protected RandomAccessibleInterval<IT> toDraw;
	
	public ImageWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img, String title, ViewModel<T,IT> vm, ImagePlus imagePlus){
		super(mod, title,vm);
		imp=imagePlus;
		
		ov= new Overlay();
		image=img;
	 	if(imp==null) rePaint(vm.getPosition(),true);
	}
	
	public void reDraw(long[] position, boolean rePaintImage){
		if(rePaintImage){
			System.out.println("dimensions:"+toDraw.numDimensions());
			System.out.println("cn:"+position[4]);
			if (toDraw.numDimensions()>2) toDraw=Views.hyperSlice(toDraw,2,position[4]);
			System.out.println("dimensions after:"+toDraw.numDimensions());
			toDraw=ImglibTools.scaleByFactor(toDraw, 0, scaleX);
			toDraw=ImglibTools.scaleByFactor(toDraw, 1, scaleY);
			if(imp==null) imp= ImageJFunctions.show(toDraw,caption);
			ImagePlus impl=ImageJFunctions.wrap( toDraw , caption);
			//	transX=(scaleX*(double)impl.getProcessor().getWidth()- (double)impl.getProcessor().getWidth())/2.0;
			//	transY=(scaleY*(double)impl.getProcessor().getHeight()- (double)impl.getProcessor().getHeight())/2.0;
			ContrastEnhancer ce= new ContrastEnhancer();
			ce.stretchHistogram(impl.getProcessor(), 0.5); 
    	
			
	
			this.imp.setProcessor(impl.getProcessor());
	
		}
		imp.setOverlay(ov);
		imp.updateAndDraw();
    	
	}

	protected void clearOverlay(){
		ov= new Overlay();
	}
	
	protected void addKymoXOverlayes(){
		SortedMap <Integer, Sequence<T>> seqs= model.getSeqs();
		for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<T> seq = seqs.get(i);
			if(seq!=null) seq.getKymoOverlayX(ov,scaleX,scaleY);
		}
	}
	
	protected void addKymoYOverlayes(){
		SortedMap <Integer, Sequence<T>> seqs= model.getSeqs();
		for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<T> seq = seqs.get(i);
			if(seq!=null) seq.getKymoOverlayY(ov,scaleX,scaleY);
		}
	}

	protected void addXOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
		   int selected =viewModel.getSelectedSequenceId();
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId).getColor();
			   t.addShapeX(ov,t.sequenceId==selected,c);
			   
		   }
		   
	}
	
	protected void addYOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
		 int selected =viewModel.getSelectedSequenceId();
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId).getColor();
			   t.addShapeY(ov,t.sequenceId==selected,c);
			   
		   }
		   
	}
	
	protected void addZOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
		 int selected =viewModel.getSelectedSequenceId();
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId).getColor();
			   t.addShapeZ(ov,t.sequenceId==selected,c);
			   
		   }
		   
	}
	
	
	
	protected void addYLineOverlay(double position){
		   
		
		   ov.add(new Line(0*scaleX,(position+0.5)*scaleY,this.model.getImage().dimension(0)*scaleX ,(position+0.5)*scaleY));		  			   
		   
	}
	
	protected void addXLineOverlay(double position){
		 
		
		   ov.add(new Line((position+0.5)*scaleX,0*scaleY,(position+0.5)*scaleX,model.getImage().dimension(1) *scaleY) );		  			   
		  
	}
}

