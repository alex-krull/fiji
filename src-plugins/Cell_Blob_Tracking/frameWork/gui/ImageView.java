package frameWork.gui;

import frameWork.Model;
import frameWork.Sequence;
import frameWork.Session;
import frameWork.Trackable;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.ContrastEnhancer;

import java.awt.Color;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
	
	
	protected class MyWindowListener implements WindowListener{

		@Override
		public void windowOpened(WindowEvent e) {
			imp.getWindow().windowOpened(e);
			
			
		}

		@Override
		public void windowClosing(WindowEvent e) {
			close();
		}

		@Override
		public void windowClosed(WindowEvent e) {
			imp.getWindow().windowClosed(e);
		}

		@Override
		public void windowIconified(WindowEvent e) {
			imp.getWindow().windowIconified(e);
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			imp.getWindow().windowDeiconified(e);
			
		}

		@Override
		public void windowActivated(WindowEvent e) {
			imp.getWindow().windowActivated(e);
			
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			imp.getWindow().windowDeactivated(e);
			
		}
		
	}
	
	protected class MyCanvas extends ImageCanvas{
		
		
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
	 	
	 	initWindow();
	 	
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
			ContrastEnhancer ce= new ContrastEnhancer();
			ce.stretchHistogram(impl.getProcessor(), 0.5); 
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
		List <Session<? extends Trackable,IT>> tcs = viewModel.getSessionsToBeDisplayed();
	
		for(Session<? extends Trackable,IT> tc: tcs){
		
			if(tc==null) return;
			SortedMap <Integer,? extends Sequence< ? extends Trackable>> seqs= tc.getSeqs();
			if(seqs!=null&& !seqs.isEmpty()) for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<? extends Trackable> seq = seqs.get(i);
			if(seq!=null){			
				seq.getKymoOverlayX(ovTemplate,scaleX,scaleY, transX, transY, viewModel.isSelected(seq.getId()));
			}
			} 
		}
	}
	
	/**
	 * add the Overlays of the traces for the y-projection-kymographs
	 */
	protected void addKymoYOverlayes(){
		List <Session<? extends Trackable,IT>> tcs = viewModel.getSessionsToBeDisplayed();
		
		for(Session<? extends Trackable,IT> tc: tcs){
		if(tc==null) return;
		SortedMap <Integer,? extends Sequence< ? extends Trackable>> seqs= tc.getSeqs();
	
		if(seqs!=null && !seqs.isEmpty()) for(int i=seqs.firstKey();i<=seqs.lastKey();i++){
			Sequence<? extends Trackable> seq = seqs.get(i);
			
			if(seq!=null){			
				seq.getKymoOverlayY(ovTemplate,scaleX,scaleY, transX, transY, viewModel.isSelected(seq.getId()));
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
		List <Session<? extends Trackable,IT>> tcs = viewModel.getSessionsToBeDisplayed();
		for(Session<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		   
		   if(trackables!=null) for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId,viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeX(ovTemplate,viewModel.isSelected(t.sequenceId),c);
			   
		   }
		   
		}
		   
	}
	
	/**
	 * add the overlays for the maximum-y-projections of a specific Frame
	 * 
	 * @param frameNumber the number of the frame to be used
	 */
	protected void addYOverlayes(int frameNumber){
		List <Session<? extends Trackable,IT>> tcs = viewModel.getSessionsToBeDisplayed();
		for(Session<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		
		 if(trackables!=null) for(Trackable t : trackables){	
	//		   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId, viewModel.getCurrentChannelNumber()).getColor();
			   t.addShapeY(ovTemplate,viewModel.isSelected(t.sequenceId),c);
			   
		   }
		}
	}
	
	/**
	 * add the overlays for the maximum-z-projections of a specific Frame
	 * 
	 * @param frameNumber the number of the frame to be used
	 */
	protected void addZOverlayes(int frameNumber){
		List <Session<? extends Trackable,IT>> tcs = viewModel.getSessionsToBeDisplayed();
		for(Session<? extends Trackable,IT> tc: tcs){	
		List<? extends Trackable> trackables= tc.getTrackablesForFrame(frameNumber);
		 
		 if(trackables!=null)for(Trackable t : trackables){	
			//   System.out.println("selectedSequenceId:"+selectedSequenceId +"  t.sequenceId:"+t.sequenceId);
			   Color c= model.getSequence(t.sequenceId).getColor();
			   t.addShapeZ(ovTemplate,viewModel.isSelected(t.sequenceId),c);
			   
		   }
		} 
	}
	
	
	
	/**
	 * Adds a vertical line Overlay from top to bottom.
	 * @param position the x-position of the line
	 */
	protected void addYLineOverlay(double position){
		   
		Line l= new Line(0 ,(position+0.5)*scaleY-transY
				,this.xSize ,(position+0.5)*scaleY-transY);
		l.setStrokeWidth(1);
		l.setStrokeColor(new Color (255,255,0));
		ovTemplate.add(l);		  			   
		   
	}
	
	/**
	 * Adds a horizontal line Overlay from the left to the right border.
	 * @param position the y-position of the line
	 */
	protected void addXLineOverlay(double position){
		Line l= new Line((position+0.5)*scaleX-transX,0
				,(position+0.5)*scaleX-transX,this.ySize) ;
		l.setStrokeWidth(1);
		l.setStrokeColor(new Color (255,255,0));
		   ovTemplate.add(l);		  			   
		  
	}
	
	/**
	 * Adds a vertical line Overlay from top to bottom.
	 * @param position the x-position of the line
	 */
	protected void addYShortLineOverlay(double position, double pos2, double length){
		   
		Line l= new Line((pos2+0.5)*scaleX-length -transX,(position+0.5)*scaleY-transY
				,(pos2+0.5)*scaleX+length -transX,(position+0.5)*scaleY-transY);
		l.setStrokeWidth(1);
		l.setStrokeColor(new Color (255,255,0));
		ovTemplate.add(l);		  			   
		   
	}
	
	/**
	 * Adds a horizontal line Overlay from the left to the right border.
	 * @param position the y-position of the line
	 */
	protected void addXShortLineOverlay(double position, double pos2, double length){
		Line l= new Line((position+0.5)*scaleX-transX,(pos2+0.5)*scaleY-transY-length
				,(position+0.5)*scaleX-transX,(pos2+0.5)*scaleY+length-transY) ;
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
	
	@Override
	public void initWindow() {
		synchronized (this){
	//	new ImageWindow(imp,new MyCanvas(imp));	
			new ImageWindow(imp,new MyCanvas(imp));
		WindowListener [] wListeners=imp.getWindow().getWindowListeners();
		for(int i=0;i<wListeners.length;i++){
			imp.getWindow().removeWindowListener(wListeners[i]);
		}
		
		imp.getWindow().addWindowListener(new MyWindowListener());
		viewModel.setPosition(-1, -1);		}
	
	}
	
	@Override
	public void open(){
	//	if(imp.getWindow()==null)
	//		initWindow();
		imp.getWindow().setVisible(true);
		viewModel.setPosition(-1, -1);
	}
	


	@Override
	public void close() {
		synchronized (this){
			if(!this.isOpen()) return;
		imp.getWindow().setVisible(false);
		viewModel.setPosition(-1, -1);
		}
	}





	@Override
	public boolean isOpen() {
		return (imp.getWindow()!=null && imp.getWindow().isVisible());
		
	}
	

}

