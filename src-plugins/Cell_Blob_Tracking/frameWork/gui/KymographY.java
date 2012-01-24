package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class KymographY <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<T,IT> implements MouseListener{

	public KymographY(Model<T, IT> mod, RandomAccessibleInterval<IT> img, ViewModel<T,IT> vm) {
		super(mod, img,vm);
		this.imp.getCanvas().addMouseListener(this);
	}
	
	public void rePaint(long[] position){
		this.clearOverlay();
		this.addKymoYOverlayes();
		this.addYLineOverlay(position[3]);
		super.rePaint(position);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x=imp.getCanvas().offScreenX(e.getX());
		int y=imp.getCanvas().offScreenY(e.getY());
		System.out.println("x:"+ x +"  y:"+y);
		viewModel.setPosition(3,y);
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}