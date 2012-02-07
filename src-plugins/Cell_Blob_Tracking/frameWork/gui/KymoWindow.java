package frameWork.gui;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import tools.ImglibTools;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public abstract class KymoWindow <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<T,IT> implements MouseWheelListener {

	protected RandomAccessibleInterval<IT> originalImage;
	protected double timeScale=1;
	protected double tics=0;
	
	public KymoWindow(Model<T,IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<T,IT> vm){
		super(mod, img, "kymograph", vm, null);
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
