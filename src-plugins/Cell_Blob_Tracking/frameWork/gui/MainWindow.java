package frameWork.gui;

import frameWork.Model;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.StackWindow;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;


public class MainWindow  < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<IT> 
implements ImageListener, MouseListener, MouseMotionListener{
	
	private class MyStackWindow extends StackWindow{	
		
		
		public MyStackWindow(ImagePlus imp) {
			super(imp);
		}
		
		
		@Override
		public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e){
//		public void updateImage(ImagePlus imp){	
			
			if(!model.hasSwitchedDimension()){
				
					System.out.println("no SwitchedDimension");
					
					//viewModel.setPosition(3, this.getImagePlus().getFrame()-1);
					if(model.isVolume()) viewModel.setPosition(2, sliceSelector.getValue()-1);
					if(model.isTimeSequence()) viewModel.setPosition(3, tSelector.getValue()-1);
					if(model.isMultiChannel()) viewModel.setPosition(4, cSelector.getValue()-1);
				
			
			}else{
				    System.out.println("SwitchedDimension");
		//			viewModel.setPosition(3, currentFrameNumber);
				    if(model.isTimeSequence()) viewModel.setPosition(3, sliceSelector.getValue()-1);
					if(model.isMultiChannel()) viewModel.setPosition(4, cSelector.getValue()-1);

			
			
			//	viewModel.setPosition(4, currentChannelNumber);
				
			}
				
		
			//super.adjustmentValueChanged(e);
			
			
		}
		
	}
	

	private final StackWindow stackWindow;
	private int currentFrameNumber;
	private int currentSliceNumber;
	private int currentChannelNumber;
		
	public MainWindow(ImagePlus imagePlus, Model< IT> mod, ViewModel< IT> vm) {
		super(mod, mod.getImage(), imagePlus.getWindow().getTitle(), vm, imagePlus, 1);
		
		
		currentFrameNumber= imp.getFrame()-1;
		currentSliceNumber= imp.getSlice()-1;
		currentChannelNumber=imp.getChannel()-1;
		//imp.getWindow().close();
		imp.setOpenAsHyperStack(true);
		stackWindow= new MyStackWindow(imp);
		
		stackWindow.getCanvas().addMouseListener(this);
		stackWindow.getCanvas().addMouseMotionListener(this);
		
		ImagePlus.addImageListener(this);
	}

	@Override
	public void rePaint(long[] position, boolean rePaintImage) {
	
		
		currentFrameNumber= (int)position[3];
		currentSliceNumber= (int)position[2];
		currentChannelNumber=(int)position[4];
		
		if(!model.hasSwitchedDimension()){
			if(currentFrameNumber != imp.getFrame()-1
				|| currentSliceNumber!= imp.getSlice()-1
				|| currentChannelNumber!= imp.getChannel()-1) 
					imp.setPosition(currentChannelNumber+1, currentSliceNumber+1, currentFrameNumber+1);
		}else
		{
			if(currentFrameNumber != imp.getSlice()-1					
					|| currentChannelNumber!= imp.getChannel()-1) 
						imp.setPosition(currentChannelNumber+1, currentFrameNumber+1, 0);
		}
		
		this.clearOverlay();
		addZOverlayes((int)position[3]);
		imp.setOverlay(ov);
		imp.updateAndDraw();

	}
	

	@Override
	public void imageClosed(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageOpened(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageUpdated(ImagePlus arg0) {
		
		
		if(!arg0.equals(imp)) return;
		
		if(!model.hasSwitchedDimension()){
		
			
				
				viewModel.setPosition(3, imp.getFrame()-1);
		
			
		
			
				
				viewModel.setPosition(2, imp.getSlice()-1);
		
			
		
		}else{
			
				
					
					viewModel.setPosition(3, imp.getSlice()-1);
				
			
		}
		
		
		
		
			
			viewModel.setPosition(4, imp.getChannel()-1);
			
			
		
			
	
		return;
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		viewModel.mouseAtPosition(positionFromEvent(e), e);	
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
		
		viewModel.mouseAtPosition(positionFromEvent(e), e);			
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
		viewModel.mouseAtPosition(positionFromEvent(e), e);		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	private long[] positionFromEvent(MouseEvent e){
		int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
		System.out.println("x:"+ x +"  y:"+y);
		
		
			long[] pos= viewModel.getPosition();
			pos[0]=x;
			pos[1]=y; 			
		return pos;
	}
	
	//public void UpDate(long[] position, boolean rePaintImage){
	//	rePaint(position, rePaintImage);
	//}



}
