package frameWork.gui;

import ij.IJ;

import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class KymographY <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<IT> {

	
	private boolean buisy=false;
	
	public KymographY(Model< IT> mod, RandomAccessibleInterval<IT> img, ViewModel<IT> vm, MainWindow<IT> mainWindow) {
		super(mod, img,vm, "Kymograph Y");
		ySize=(int)model.getYTProjections(0).dimension(1);
		if(ySize>500) ySize=500;
		if(ySize<100) ySize=100;
	    baseTimeScale=(double)ySize/(double)model.getNumberOfFrames();	
		timeScale=baseTimeScale*Math.pow(1.1, tics);
		Rectangle r= mainWindow.getWindow().getBounds();
	//	IJ.error("mw x:"+ String.valueOf(mainWindow.getWindow().getX()) +
	//			 "kw x:"+ imp.getWindow().getX());
		imp.getWindow().setLocation((int)r.getX(),(int)r.getMaxY());
	///	IJ.error("mw x:"+ String.valueOf(mainWindow.getWindow().getX()) +
	//			 "kw x:"+ imp.getWindow().getX());
		
	   
	    
	//	this.imp.getCanvas().addMouseListener(this);
	//	sb.setLocation(0, 0);
	//	sb.setOrientation(Scrollbar.VERTICAL);
	}
	
	public void reFresh(long[] position, boolean rePaintImage){
		
		scaleY=timeScale;	
		transY=Math.max(0,(int)(scaleY*position[3])-ySize/2);
		transY=Math.min( transY, (int)((1+model.getYTProjections((int)position[4]).max(1))*scaleY)-ySize );
		
		this.clearOverlay();
	    this.addKymoYOverlayes();
		this.addYLineOverlay(position[3]);
		if(viewModel.mouseIsInWindow)  this.addXShortLineOverlay(position[0], position[3],10);
		RandomAccessibleInterval<IT> toDraw=model.getYTProjections((int)position[4]);
		reDraw(position, rePaintImage,toDraw);
		//super.reFresh(position, rePaintImage);
	}

	
	
	@Override
	public long[] positionFromEvent(MouseEvent e){
		int x=(int)((double)(imp.getCanvas().offScreenX(e.getX())+transX)/this.scaleX);
		int t=(int)((double)(imp.getCanvas().offScreenY(e.getY())+transY)/this.scaleY);
	//	System.out.println("x:"+ x +"  y:"+y);
		
		
			long[] pos= viewModel.getPosition();
			pos[0]=x;
			pos[1]=-1; 
			pos[3]=t;
		return pos;
	}
	


}