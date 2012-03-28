package frameWork.gui;

import frameWork.Model;
import frameWork.Sequence;
import frameWork.Trackable;
import frameWork.TrackingChannel;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;

import java.awt.Color;
import java.awt.event.KeyListener;
import java.util.List;
import java.util.SortedMap;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;



/**
 * @author alex
 *
 * @param <IT>
 */
public abstract class ImageView  < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<IT>{
	
	protected ImagePlus imp=null;
	protected RandomAccessibleInterval<IT> image;
	protected ImageCanvas canvas;
	protected Overlay ovTemplate;
	protected double scaleX=1;
	protected double scaleY=1;
	protected int transX=0;
	protected int transY=0;
	protected int xSize=-1;
	protected int ySize=-1;

	protected RandomAccessibleInterval<IT> toDraw;
	
	private class MyCanvas extends ImageCanvas{
		
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public MyCanvas(ImagePlus arg0) {
			super(arg0);
		}
		@ Override
		public void setMagnification(double mag) {
			super.setMagnification(mag);
			upDateOverlay();
		}

		
	}
	
	@Override
	public void addKeyListener(HotKeyListener keyListener){
		KeyListener[] listeners= imp.getWindow().getKeyListeners();
		for(int i=0;i<listeners.length;i++){
			KeyListener kl=listeners[i];
			imp.getWindow().removeKeyListener(kl);
		}
		
		listeners= imp.getCanvas().getKeyListeners();
		for(int i=0;i<listeners.length;i++){
			KeyListener kl=listeners[i];
			imp.getCanvas().removeKeyListener(kl);
		}
		
		imp.getCanvas().addKeyListener(keyListener);
		imp.getWindow().addKeyListener(keyListener);
		
	}
	
	public ImageView(Model<IT> mod, RandomAccessibleInterval<IT> img, String title, ViewModel<IT> vm, ImagePlus imagePlus){
		super(mod, title,vm);
		imp=imagePlus;
		
		ovTemplate= new Overlay();
		image=img;
	 	if(imp==null) reFresh(vm.getPosition(),true);
	 	ImageWindow imw= imp.getWindow();
	 	new ImageWindow(imp, new MyCanvas(imp));
	 	
	 	//imp.getCanvas().addComponentListener(new myPropChangeListener());
	// 	imp.getCanvas().addPropertyChangeListener(new myPropChangeListener());
	
	// 	imp.addImageListener(new myPropChangeListener(imp));
	 	
	}
	
	public void reDraw(long[] position, boolean rePaintImage){
		if(rePaintImage){
			
			if (toDraw.numDimensions()>2) toDraw=Views.hyperSlice(toDraw,2,position[4]);
			if(imp==null) 
				imp= ImageJFunctions.show(toDraw,caption);
			else{
						

			if(xSize<0 )xSize= (int)(scaleX*((int)toDraw.dimension(0)));
			if(ySize<0 )ySize= (int)(scaleY*((int)toDraw.dimension(1)));	
			
			RandomAccessibleInterval<IT> temp = ImglibTools.scaleAndShift(toDraw, transX, transY, scaleX, scaleY, xSize, ySize);
			ImagePlus impl=ImageJFunctions.wrap( temp , caption);
	//		ContrastEnhancer ce= new ContrastEnhancer();
	//		ce.stretchHistogram(impl.getProcessor(), 0.5); 
			this.imp.setProcessor(impl.getProcessor());
			}
	
		}
		
		upDateOverlay();
		
		imp.updateAndDraw();
		
    	
	}

	protected void upDateOverlay(){
		if(viewModel.getDrawOverLays()){
			Overlay ov=ovTemplate.duplicate();
			for(int i=0;i<ov.size();i++){
				Roi roi=ov.get(i);
				roi.setStrokeWidth(Math.min(3, roi.getStrokeWidth()/imp.getCanvas().getMagnification() ));
	//			roi.setStrokeWidth(roi.getStrokeWidth()/2);
			}
			imp.setOverlay(ov);
		}
		else imp.setOverlay(null);	
		
	}
	
	protected void clearOverlay(){
		ovTemplate= new Overlay();
	}
	
	
	/**
	 * add the Overlays of the traces for the x-projection-kymographs
	 */
	protected void addKymoXOverlayes(){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
	
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){
		
			if(tc==null) return;
			SortedMap <Integer,? extends Sequence< ? extends Trackable>> seqs= tc.getSeqs();
			if(seqs!=null&& !seqs.isEmpty()) for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<? extends Trackable> seq = seqs.get(i);
			if(seq!=null){			
				seq.getKymoOverlayX(ovTemplate,scaleX,scaleY, transX, transY, viewModel.isSelected(seq.getId(), tc.getId()));
			}
			} 
		}
	}
	
	/**
	 * add the Overlays of the traces for the y-projection-kymographs
	 */
	protected void addKymoYOverlayes(){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){
		if(tc==null) return;
		SortedMap <Integer,? extends Sequence< ? extends Trackable>> seqs= tc.getSeqs();
	
		if(seqs!=null && !seqs.isEmpty()) for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<? extends Trackable> seq = seqs.get(i);
			
			if(seq!=null){			
				seq.getKymoOverlayY(ovTemplate,scaleX,scaleY, transX, transY, viewModel.isSelected(seq.getId(), tc.getId()));
			}
		}
		}
	}

	
	/**
	 * add the overlays for the maximum-x-projections of a specific Frame
	 * 
	 * @param frameNumber the number of the frame to be used
	 */
	protected void addXOverlayes(int frameNumber){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		   
		   if(trackables!=null) for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId,viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeX(ovTemplate,viewModel.isSelected(t.sequenceId, t.channel),c);
			   
		   }
		   
		}
		   
	}
	
	/**
	 * add the overlays for the maximum-y-projections of a specific Frame
	 * 
	 * @param frameNumber the number of the frame to be used
	 */
	protected void addYOverlayes(int frameNumber){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		
		 if(trackables!=null) for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId, viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeY(ovTemplate,viewModel.isSelected(t.sequenceId, t.channel),c);
			   
		   }
		}
	}
	
	/**
	 * add the overlays for the maximum-z-projections of a specific Frame
	 * 
	 * @param frameNumber the number of the frame to be used
	 */
	protected void addZOverlayes(int frameNumber){
		List <TrackingChannel<? extends Trackable,IT>> tcs = viewModel.getTCsToBeDisplayed();
		for(TrackingChannel<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		 
		 if(trackables!=null)for(Trackable t : trackables){	
			//   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId, viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeZ(ovTemplate,viewModel.isSelected(t.sequenceId, t.channel),c);
			   
		   }
		} 
	}
	
	
	
	/**
	 * Adds a vertical line Overlay from top to bottom.
	 * @param position the x-position of the line
	 */
	protected void addYLineOverlay(double position){
		   
		Line l= new Line(0*scaleX,(position+0.5)*scaleY-transY,this.model.getImage().dimension(0)*scaleX ,(position+0.5)*scaleY-transY);
		l.setStrokeWidth(1);
		l.setStrokeColor(new Color (255,255,0));
		ovTemplate.add(l);		  			   
		   
	}
	
	/**
	 * Adds a horizontal line Overlay from the left to the right border.
	 * @param position the y-position of the line
	 */
	protected void addXLineOverlay(double position){
		Line l= new Line((position+0.5)*scaleX-transX,0*scaleY,(position+0.5)*scaleX-transX,model.getImage().dimension(1) *scaleY) ;
		l.setStrokeWidth(1);
		l.setStrokeColor(new Color (255,255,0));
		   ovTemplate.add(l);		  			   
		  
	}
	
	@Override
	public void setZoom(double newZoom){
		
		
		
		canvas =imp.getCanvas();
		double cm=canvas.getMagnification();
		while(cm>newZoom){
			canvas.zoomOut(imp.getWidth()/2, imp.getHeight()/2);
			cm=canvas.getMagnification();
		}
			
		while(cm<newZoom){
			canvas.zoomIn(imp.getWidth()/2, imp.getHeight()/2);
			cm=canvas.getMagnification();
		}
		
		
		
		
	}
	

}

