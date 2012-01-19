package frameWork.gui;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public abstract class ImageWindow  <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<T,IT>{
	
	protected ImagePlus imp;
	protected RandomAccessibleInterval<IT> image;
	protected ImageCanvas canvas;
	
	public ImageWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img, String title){
		super(mod, title);
		image=img;
	}


	protected void addXOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
		   Overlay ovX=new Overlay();
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			
			   t.addShapeX(ovX,false);
			   
		   }
		   imp.setOverlay(ovX);
	}
	
	protected void addYOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
		   Overlay ovY=new Overlay();
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			
			   t.addShapeY(ovY,false);
			   
		   }
		   imp.setOverlay(ovY);
	}
	
	protected void addZOverlayes(int frameNumber){
		List<T> trackables= model.getTrackablesForFrame(frameNumber);
		   Overlay ovZ=new Overlay();
		   for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			
			   t.addShapeZ(ovZ,false);
			   
		   }
		   imp.setOverlay(ovZ);
	}
}
