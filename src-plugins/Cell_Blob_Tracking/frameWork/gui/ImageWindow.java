package frameWork.gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.plugin.ContrastEnhancer;

import java.util.List;
import java.util.SortedMap;

import org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID;

import tools.ImglibTools;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Sequence;
import frameWork.Trackable;

public abstract class ImageWindow  <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<T,IT>{
	
	protected ImagePlus imp;
	protected RandomAccessibleInterval<IT> image;
	protected ImageCanvas canvas;
	protected Overlay ov;
	protected double scaleX=1;
	protected double scaleY=1;
	protected double transX=0;
	protected double transY=0;

	protected RandomAccessibleInterval<IT> toDraw;
	
	public ImageWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img, String title, ViewModel<T,IT> vm){
		super(mod, title,vm);
		ov= new Overlay();
		image=img;
		
	}
	
	public void rePaint(){
		
		toDraw=ImglibTools.scaleByFactor(toDraw, 0, scaleX);
		toDraw=ImglibTools.scaleByFactor(toDraw, 1, scaleY);
		ImagePlus impl=ImageJFunctions.wrap( toDraw , caption);
	//	transX=(scaleX*(double)impl.getProcessor().getWidth()- (double)impl.getProcessor().getWidth())/2.0;
	//	transY=(scaleY*(double)impl.getProcessor().getHeight()- (double)impl.getProcessor().getHeight())/2.0;
	    ContrastEnhancer ce= new ContrastEnhancer();
    	ce.stretchHistogram(impl.getProcessor(), 0.5); 
    	
    //	impl.getProcessor().translate(transX/scaleX,transY/scaleY);
    //	impl.getProcessor().scale(scaleX, scaleY);
    	
    	this.imp.setProcessor(impl.getProcessor());
    	//imp.setImage(impl);
    	
		imp.setOverlay(ov);
		
		//imp.setProcessor(imp.getProcessor().resize((int)(imp.getProcessor().getWidth()*scaleX), (int)(imp.getProcessor().getWidth()*scaleY)) );
		//imp.setProcessor( );

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
		   
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			
			   t.addShapeX(ov,false);
			   
		   }
		   
	}
	
	protected void addYOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
		 
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			
			   t.addShapeY(ov,false);
			   
		   }
		   
	}
	
	protected void addZOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
	
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			
			   t.addShapeZ(ov,false);
			   
		   }
		   
	}
	
	
	
	protected void addYLineOverlay(double position){
		   
		
		   ov.add(new Line(0*scaleX,(position+0.5)*scaleY,this.image.dimension(0)*scaleX ,(position+0.5)*scaleY));		  			   
		   
	}
	
	protected void addXLineOverlay(double position){
		 
		
		   ov.add(new Line((position+0.5)*scaleX,0*scaleY,(position+0.5)*scaleX,this.image.dimension(1) *scaleY) );		  			   
		  
	}
}

