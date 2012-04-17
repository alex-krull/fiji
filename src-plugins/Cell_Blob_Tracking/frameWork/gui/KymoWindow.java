package frameWork.gui;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public abstract class KymoWindow <  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> {

	protected RandomAccessibleInterval<IT> originalImage;
	protected double timeScale=1;
	protected double baseTimeScale=1;
	protected double tics=0;
	protected volatile Scrollbar sb;
	
	public KymoWindow(Model<IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<IT> vm, String label){
		super(mod, img, label,  vm, null);
		
		timeScale=1;
		originalImage=img;
		
		
		imp.getCanvas().addMouseWheelListener(new MyListener());
		
	//	sb=new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 1, model.getNumberOfFrames()+1);
	//	imp.getWindow().add(sb);
	//	sb.addAdjustmentListener(this);
	//	rePaint(viewModel.getPosition(),true);	
	}
	

	
	

	public void adjustmentValueChanged(AdjustmentEvent e) {
		System.err.println(" SLIDER HAS CHANGED !  ! ! !  ");
			if(sb.hasFocus());
			viewModel.setPosition(3,e.getValue()-1);
	}

	
	public class MyListener implements  MouseWheelListener{


		@Override
		public synchronized void  mouseWheelMoved(MouseWheelEvent e) {
			
				if(!e.isShiftDown()){
					int newPos= (int)(viewModel.getPosition()[3]+ e.getWheelRotation());
					newPos=Math.min(Math.max(newPos, 0), model.getNumberOfFrames()-1);
					viewModel.setPosition(3,newPos);
					return;
				}
				
				
				tics+= e.getWheelRotation();
				
				if(tics<0){
					tics=0;
			//		return;
				}
		
				
		//		System.out.println(xSize/baseTimeScale*Math.pow(1.1, tics));
		//		while(xSize/baseTimeScale*Math.pow(1.1, tics)<5.0)
		//			tics--;
				
				timeScale=baseTimeScale*Math.pow(1.1, tics);
				viewModel.setPosition(-1, -1);				
				
		//		System.out.println("baseTimeScale:"+baseTimeScale);
		//		System.out.println("timeScale:"+timeScale);
				
				
				// TODO Auto-generated method stub
				
				
				
			}
		
	}
	
	
}
