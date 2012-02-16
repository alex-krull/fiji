package frameWork.gui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.LinkedBlockingQueue;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.gui.ViewWindow.UpdateTask;


public abstract class KymoWindow <  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<IT> implements MouseWheelListener {

	protected RandomAccessibleInterval<IT> originalImage;
	protected double timeScale;
	protected double tics=0;
	
	public KymoWindow(Model<IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<IT> vm){
		super(mod, img, "kymograph", vm, null, 3);
		
		timeScale=1;
		originalImage=img;
		
		
		imp.getCanvas().addMouseWheelListener(this);
		
				
	}
	
	public void rePaint(long[] position, boolean rePaintImage){
		reDraw(position, rePaintImage);
	}
	
	@Override
	public synchronized void  mouseWheelMoved(MouseWheelEvent e) {
	
	//	if(e.isControlDown()){
	//		this.transX++;
	//		viewModel.setPosition(-1, -1);
	//		return;
	//	}
		
		
		tics+= e.getWheelRotation();
		if(tics<1){
			tics=1;
			return;
		}
	//	timeScale=Math.pow(1.1, tics);
		
		timeScale=timeScale*Math.pow(1.1, e.getWheelRotation());
		//rePaint(viewModel.getPosition(),true);
		//image = ImglibTools.scaleByFactor(this.originalImage,1,this.timeScale);
		viewModel.setPosition(-1, -1);
		
		//long[] pos= {0,0,0,0,0};
		
		//if(viewModel.getMutex()){
		//	rePaint(pos);
		//	viewModel.releaseMutex();
		//}
		
		
		System.out.println("timeScale:"+timeScale);
		
		// TODO Auto-generated method stub
		
		
		
	}

	
	
}
