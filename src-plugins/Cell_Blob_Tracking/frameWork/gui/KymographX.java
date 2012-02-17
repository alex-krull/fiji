package frameWork.gui;

import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class KymographX <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<IT> implements MouseListener{

	
	private boolean buisy =false;
	
	public KymographX(Model<IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<IT> vm) {
		super(mod, img,vm);
		xSize=(int)model.getXTProjections(0).dimension(0);
		
		if(xSize>500) xSize=500;
		if(xSize<100) xSize=100;
		baseTimeScale=(double)xSize/(double)model.getNumberOfFrames();	
		timeScale=baseTimeScale*Math.pow(1.1, tics);
		
		this.imp.getCanvas().addMouseListener(this);
	//	sb.setOrientation(Scrollbar.HORIZONTAL);
		
	}
	
	public void rePaint(long[] position, boolean rePaintImage){
		scaleX=timeScale;
		transX=(int)Math.min(Math.max(0,(int)(scaleX*position[3])-xSize/2),(1+model.getXTProjections((int)position[4]).max(0))*scaleX-xSize);
		System.out.println("timeScale:"+timeScale);
		System.out.println("transX:"+transX);
		this.clearOverlay();
		this.addKymoXOverlayes();
		this.addXLineOverlay(position[3]);
		toDraw=model.getXTProjections((int)position[4]);
		super.rePaint(position, rePaintImage);
		//imp.updateAndDraw();
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x=(int)(((double)imp.getCanvas().offScreenX(e.getX())+transX)/this.scaleX);
		int y=(int)(((double)imp.getCanvas().offScreenY(e.getY())+transY)/this.scaleY);
		System.out.println("x:"+ x +"  y:"+y);
		if(e.getButton()==MouseEvent.BUTTON2) viewModel.setPosition(3,x);
		
		
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
