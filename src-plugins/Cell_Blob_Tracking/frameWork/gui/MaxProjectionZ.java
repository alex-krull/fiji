package frameWork.gui;

import ij.gui.Overlay;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import tools.ImglibTools;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class MaxProjectionZ <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> 
implements MouseListener, MouseMotionListener {
	
	public MaxProjectionZ(Model<T,IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<T,IT> vm){
		super(mod, ImglibTools.projection(img,2), "max-Z-projection",3,  vm);
		imp.getCanvas().addMouseListener(this);
		imp.getCanvas().addMouseMotionListener(this);
	}
	
	public void rePaint(long[] position, boolean rePaintImage){	
		
		this.clearOverlay();
		addZOverlayes((int)position[3]);
		super.rePaint(position, rePaintImage);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
		
		System.out.println("x:"+ x +"  y:"+y);	
		long[] pos= viewModel.getPosition();
		pos[0]=x;
		pos[1]=y; 
		pos[2]=-1;
		viewModel.mouseAtPosition(pos, e);	
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
		int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
		if(e.getButton()==MouseEvent.BUTTON1){
			long[] pos= viewModel.getPosition();
			pos[0]=x;
			pos[1]=y; 
			pos[2]=-1;
			viewModel.mouseAtPosition(pos, e);
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
		
		System.out.println("x:"+ x +"  y:"+y);	
		long[] pos= viewModel.getPosition();
		pos[0]=x;
		pos[1]=y; 
		pos[2]=-1;
		viewModel.mouseAtPosition(pos, e);		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
