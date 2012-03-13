package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class MaxProjectionZ < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> 
implements MouseListener, MouseMotionListener {
	
	public MaxProjectionZ(Model<IT> mod, ViewModel<IT> vm){
		super(mod, null, "max-Z-projection", vm, null);
		imp.getCanvas().addMouseListener(this);
		imp.getCanvas().addMouseMotionListener(this);
	}
	
	@Override
	public void rePaint(long[] position, boolean rePaintImage){	
		
		this.clearOverlay();
		addZOverlayes((int)position[3]);
		int frameNumber= (int)position[3];
		toDraw=model.getFrame(frameNumber, viewModel.getCurrentChannelNumber()).getZProjections();
		reDraw( position ,rePaintImage);
		//super.rePaint(position, rePaintImage);
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
			pos[2]=-1;
		return pos;
	}

}
