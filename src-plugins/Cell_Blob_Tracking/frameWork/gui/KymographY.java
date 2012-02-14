package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class KymographY <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<IT> implements MouseListener{

	
	
	public KymographY(Model< IT> mod, RandomAccessibleInterval<IT> img, ViewModel<IT> vm) {
		super(mod, img,vm);
		this.imp.getCanvas().addMouseListener(this);
	}
	
	public void rePaint(long[] position, boolean rePaintImage){
		scaleY=timeScale;
		
		this.clearOverlay();
		this.addKymoYOverlayes();
		this.addYLineOverlay(position[3]);
		toDraw=model.getYTProjections((int)position[4]);
		super.rePaint(position, rePaintImage);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
		System.out.println("x:"+ x +"  y:"+y);
		if(e.getButton()==MouseEvent.BUTTON2) viewModel.setPosition(3,y);
		
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