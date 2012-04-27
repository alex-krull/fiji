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
import java.awt.Graphics;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
public abstract class ImageView  < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<IT>
implements MouseListener, MouseMotionListener{
	
	protected ImagePlus imp=null;
	protected RandomAccessibleInterval<IT> image=null;
	protected ImageCanvas canvas;
	protected volatile Overlay ovTemplate;
	protected double scaleX=1;
	protected double scaleY=1;
	protected int transX=0;
	protected int transY=0;
	protected int xSize=-1;
	protected int ySize=-1;

//	protected RandomAccessibleInterval<IT> toDraw;
	
	
	protected class MyWindowListener implements WindowListener{

		@Override
		public void windowOpened(WindowEvent e) {
			if(!terminate) imp.getWindow().windowOpened(e);
			
			
		}

		@Override
		public void windowClosing(WindowEvent e) {
			if(!terminate) close();
		}

		@Override
		public void windowClosed(WindowEvent e) {
			if(!terminate) imp.getWindow().windowClosed(e);
		}

		@Override
		public void windowIconified(WindowEvent e) {
			if(!terminate) imp.getWindow().windowIconified(e);
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			if(!terminate) imp.getWindow().windowDeiconified(e);
			
		}

		@Override
		public void windowActivated(WindowEvent e) {
			if(!terminate) imp.getWindow().windowActivated(e);
			
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			if(!terminate) imp.getWindow().windowDeactivated(e);
			
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
		
		@ Override
		public void update(Graphics g){
	
			synchronized (ovTemplate){
			super.update(g);
			}

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
		
	 	if(imp==null){
	 		reFresh(vm.getPosition(),true);
	 		initWindow();
	 	}
	 	
	 	
	 	//imp.getCanvas().addComponentListener(new myPropChangeListener());
	// 	imp.getCanvas().addPropertyChangeListener(new myPropChangeListener());
	
	// 	imp.addImageListener(new myPropChangeListener(imp));
	 	
	}
	
	@Override
	public void startThread(){
		
		super.startThread();
	}
	
	public synchronized void initImp(RandomAccessibleInterval<IT> toDraw){
		imp= ImageJFunctions.show(toDraw,caption);
		ContrastEnhancer ce= new ContrastEnhancer();		
		ce.stretchHistogram(imp, 0.5);
	}
	
	public void reDraw(long[] position, boolean rePaintImage, RandomAccessibleInterval<IT> toDraw){
		model.rwLock.readLock().lock();
		
		if(rePaintImage){
			
			if (toDraw.numDimensions()>2) toDraw=Views.hyperSlice(toDraw,2,position[4] );
			if(imp==null) {
				initImp(toDraw);	
			}
			else{
						

			if(xSize<0 )xSize= (int)(scaleX*((int)toDraw.dimension(0)));
			if(ySize<0 )ySize= (int)(scaleY*((int)toDraw.dimension(1)));	
			
			RandomAccessibleInterval<IT> temp = ImglibTools.scaleAndShift(toDraw, transX, transY, scaleX, scaleY, xSize, ySize);
			ImagePlus impl=ImageJFunctions.wrap( temp , caption);
			
			double min= imp.getProcessor().getMin();
			double max= imp.getProcessor().getMax();
			System.out.println(this.getClass().getName()+":  min:"+min +"  max:"+max);
			
			
			this.imp.setProcessor(impl.getProcessor());
			impl.getProcessor().setMinAndMax(min, max);
			}
	
		}
		
		
		imp.updateAndDraw();
		upDateOverlay();
		
		
		
		
		model.rwLock.readLock().unlock();
	}

	protected void upDateOverlay(){
		synchronized (ovTemplate){
		
			
		if(viewModel.getDrawOverLays()){
			Overlay ov=ovTemplate.duplicate();
			for(int i=0;i<ov.size();i++){
				Roi roi=ov.get(i);
				roi.setStrokeWidth(Math.min(3, roi.getStrokeWidth()/imp.getCanvas().getMagnification() ));
			}
			
			imp.setOverlay(ov);
			
		}
		else {
			
				imp.setOverlay(null);	
			
		
		}
		
		}
	}
	
	protected void clearOverlay(){
		
		ovTemplate.clear();
		
		
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
			   Color c= model.getSequence(t.sequenceId).getColor();
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
			   Color c= model.getSequence(t.sequenceId ).getColor();
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
			   t.addShapeZ(ovTemplate,viewModel.isSelected(t.sequenceId),c, viewModel.isDrawNumbers());
			   
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
	
	public double getZoom(){
		return imp.getCanvas().getMagnification();
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
		imp.getCanvas().addMouseListener(this);
		imp.getCanvas().addMouseMotionListener(this);
		viewModel.setPosition(-1, -1);		}
		
		
		MouseWheelListener [] wheelListeners=imp.getWindow().getMouseWheelListeners();
		for(int i=0;i<wheelListeners.length;i++){
			imp.getWindow().removeMouseWheelListener(wheelListeners[i]);
		}
		
		imp.getWindow().addMouseWheelListener(new MyWheelListener());
		
	
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
	public void terminate(){
		super.terminate();
		imp.close();
	}





	@Override
	public boolean isOpen() {
		return (imp.getWindow()!=null && imp.getWindow().isVisible());
		
	}
	
	public abstract long[] positionFromEvent(MouseEvent e);

	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		viewModel.mouseAtPosition(positionFromEvent(e), e);	
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		viewModel.mouseAtPosition(positionFromEvent(e), e);	
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		viewModel.mouseAtPosition(positionFromEvent(e), e);	
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		
		viewModel.mouseAtPosition(positionFromEvent(e), e);			
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		viewModel.mouseAtPosition(positionFromEvent(e), e);	
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		viewModel.mouseAtPosition(positionFromEvent(e), e);		
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		viewModel.mouseAtPosition(positionFromEvent(e), e);
		
	}
	
	
	public class MyWheelListener implements  MouseWheelListener{


		@Override
		public synchronized void  mouseWheelMoved(MouseWheelEvent e) {
			
					if(e.isShiftDown()){
						int newPos= (int)(viewModel.getPosition()[2]+ e.getWheelRotation());
						newPos=Math.min(Math.max(newPos, 0), model.getNumberOfFrames()-1);
						viewModel.setPosition(2,newPos);
						return;
					}else{
						int newPos= (int)(viewModel.getPosition()[3]+ e.getWheelRotation());
						newPos=Math.min(Math.max(newPos, 0), model.getNumberOfFrames()-1);
						viewModel.setPosition(3,newPos);
						return;
					}
					
				
			
		}
		
	}
	

	
}

