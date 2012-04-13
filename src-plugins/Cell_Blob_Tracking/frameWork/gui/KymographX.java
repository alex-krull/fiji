package frameWork.gui;

import ij.IJ;
import ij.plugin.ContrastEnhancer;

import java.awt.Point;
import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import frameWork.Model;


public class KymographX <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<IT> {

	
	private final boolean buisy =false;
	
	public KymographX(Model<IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<IT> vm, MainWindow<IT> mainWindow) {
		super(mod, img,vm, "Kymograph X");
		xSize=(int)model.getXTProjections(0).dimension(0);
		
		if(xSize>500) xSize=500;
		if(xSize<100) xSize=100;
		baseTimeScale=(double)xSize/(double)model.getNumberOfFrames();	
		timeScale=baseTimeScale*Math.pow(1.1, tics);
		
		
	//	MyListener ml=new MyListener();
	//	this.imp.getCanvas().addMouseListener(ml);
	//	this.imp.getCanvas().addMouseMotionListener(ml);
	//	sb.setOrientation(Scrollbar.HORIZONTAL);
		Point p= mainWindow.getWindow().getLocation();
		
		
		if(imp.getWindow().getParent()!= null){
			IJ.error(imp.getWindow().getParent().getClass().getName());
			imp.getWindow().getParent().setLocation(0, 0);
			
		}
		
		if(mainWindow.getWindow().getParent()!= null){
			mainWindow.getWindow().getParent().setLocation(0, 0);
			IJ.error(mainWindow.getWindow().getParent().getClass().getName());
		}
	//	IJ.error("mw x:"+ String.valueOf(mainWindow.getWindow().getX()) +
	//			 "kw x:"+ imp.getWindow().getX());
	}
	
	@Override
	public void reFresh(long[] position, boolean rePaintImage){
		scaleX=timeScale;
		transX=(int)Math.min(Math.max(0,(int)(scaleX*position[3])-xSize/2),(1+model.getXTProjections((int)position[4]).max(0))*scaleX-xSize);
		//System.out.println("timeScale:"+timeScale);
		//System.out.println("transX:"+transX);
		this.clearOverlay();
		this.addKymoXOverlayes();
		this.addXLineOverlay(position[3]);
		if(viewModel.mouseIsInWindow) this.addYShortLineOverlay(position[1], position[3],10);
		RandomAccessibleInterval<IT> toDraw=model.getXTProjections((int)position[4]);
		reDraw(position, rePaintImage,toDraw);
		
		
	}


	
	@Override
	public long[] positionFromEvent(MouseEvent e){
		int t=(int)((imp.getCanvas().offScreenX(e.getX())+transX)/this.scaleX);
		int y=(int)((imp.getCanvas().offScreenY(e.getY())+transY)/this.scaleY);
	//	System.out.println("x:"+ x +"  y:"+y);
		
		
			long[] pos= viewModel.getPosition();
			pos[0]=-1;
			pos[1]=y; 
			pos[3]=t;
		return pos;
	}

	

}
