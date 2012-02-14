package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import ij.ImageListener;
import ij.ImagePlus;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class MainWindow  < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<IT>
implements ImageListener, MouseListener, MouseMotionListener{

	private int currentFrameNumber;
	private int currentSliceNumber;
	private int currentChannelNumber;
		
	protected MainWindow(ImagePlus imagePlus, Model< IT> mod, ViewModel< IT> vm) {
		super(mod, mod.getImage(), imagePlus.getWindow().getTitle(), vm, imagePlus);
		
		
		currentFrameNumber= imp.getFrame()-1;
		currentSliceNumber= imp.getSlice()-1;
		currentChannelNumber=imp.getChannel()-1;
		imp.getCanvas().addMouseListener(this);
		imp.getCanvas().addMouseMotionListener(this);
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
	public synchronized void imageUpdated(ImagePlus arg0) {
		
		
		if(!arg0.equals(imp)) return;
		
		if(!model.hasSwitchedDimension()){
		
			if(currentFrameNumber != imp.getFrame()-1){
				currentFrameNumber= imp.getFrame()-1;
				viewModel.setPosition(3, currentFrameNumber);
		
				return;
			}
		
			if(currentSliceNumber!= imp.getSlice()-1){
				currentSliceNumber= imp.getSlice()-1;
				viewModel.setPosition(2, currentSliceNumber);
		
				return;
			}
		
		}else{
			
			if(currentFrameNumber!= imp.getSlice()-1){
				currentFrameNumber=imp.getSlice()-1;
				viewModel.setPosition(3, currentFrameNumber);
			}
		}
		
		
		
		if(currentChannelNumber!= imp.getChannel()-1){
			currentChannelNumber= imp.getChannel()-1;
			viewModel.setPosition(4, currentChannelNumber);
			
			return;
		}
			
	
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
		int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
		System.out.println("x:"+ x +"  y:"+y);
		
		
			long[] pos= viewModel.getPosition();
			pos[0]=x;
			pos[1]=y; 			
		return pos;
	}



}
