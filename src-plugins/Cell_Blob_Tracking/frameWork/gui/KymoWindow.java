package frameWork.gui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public abstract class KymoWindow <  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<IT> implements MouseWheelListener {

	protected RandomAccessibleInterval<IT> originalImage;
	protected double timeScale;
	protected double tics=0;
	
	public KymoWindow(Model<IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<IT> vm){
		super(mod, img, "kymograph", vm, null);
		timeScale=1;
		originalImage=img;
		
		
		imp.getCanvas().addMouseWheelListener(this);
		
	}
	
	public void rePaint(long[] position, boolean rePaintImage){
		reDraw(position, rePaintImage);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	
		
	
		tics+= e.getWheelRotation();
		timeScale=Math.pow(1.05, tics);
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
