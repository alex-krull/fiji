package frameWork.gui;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class KymographY <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<IT> implements MouseListener , ComponentListener{

	
	private boolean buisy=false;
	
	public KymographY(Model< IT> mod, RandomAccessibleInterval<IT> img, ViewModel<IT> vm) {
		super(mod, img,vm);
		ySize=(int)model.getYTProjections(0).max(1);
	    if(ySize<100){
	    	ySize=100;
	    	timeScale=(double)ySize/(double)model.getYTProjections(0).max(1);
	    }
	    
		this.imp.getCanvas().addMouseListener(this);
		imp.getWindow().addComponentListener(this);
	}
	
	public void rePaint(long[] position, boolean rePaintImage){
		scaleY=timeScale;	
		transY=Math.max(0,(int)(scaleY*position[3])-ySize/2);
		System.out.println("position:" + transY + "  Maximum:"+  ((int)(model.getYTProjections((int)position[4]).max(1)*scaleY-ySize) ) + "ySize:" +ySize);
		transY=Math.min( transY, (int)((1+model.getYTProjections((int)position[4]).max(1))*scaleY)-ySize );
		
		this.clearOverlay();
	//	this.addKymoYOverlayes();
		this.addYLineOverlay(position[3]);
		toDraw=model.getYTProjections((int)position[4]);
		super.rePaint(position, rePaintImage);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int x=(int)((double)(imp.getCanvas().offScreenX(e.getX())+transX)/this.scaleX);
		int y=(int)((double)(imp.getCanvas().offScreenY(e.getY())+transY)/this.scaleY);
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

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void componentResized(ComponentEvent arg0) {
	//	if(buisy){
	//		buisy=false;
	//		return;
	//	}
	//	buisy =true;
	//	ySize=imp.getWindow().getHeight()-30;
    //	timeScale=(double)ySize/(double)model.getYTProjections(0).max(1);
    //	rePaint(viewModel.getPosition(),true);	
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
			
	}
	


}