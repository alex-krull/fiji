package frameWork.gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;

import java.util.List;
import java.util.SortedMap;

import net.imglib2.RandomAccessibleInterval;
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
	
	public ImageWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img, String title, ViewModel<T,IT> vm){
		super(mod, title,vm);
		ov= new Overlay();
		image=img;
		
	}
	
	public void rePaint(){
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
			if(seq!=null) seq.getKymoOverlayX(ov);
		}
	}
	
	protected void addKymoYOverlayes(){
		SortedMap <Integer, Sequence<T>> seqs= model.getSeqs();
		for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<T> seq = seqs.get(i);
			if(seq!=null) seq.getKymoOverlayY(ov);
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
		   
		
		   ov.add(new Line(0,position,this.image.dimension(0) ,position));		  			   
		   
	}
	
	protected void addXLineOverlay(double position){
		 
		
		   ov.add(new Line(position,0,position,this.image.dimension(1) ) );		  			   
		  
	}
}

