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


public abstract class KymoWindow <  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<IT> {

	protected RandomAccessibleInterval<IT> originalImage;
	protected double timeScale=1;
	protected double baseTimeScale=1;
	protected double tics=0;
	protected volatile Scrollbar sb;
	
	public KymoWindow(Model<IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<IT> vm){
		super(mod, img, "kymograph", vm, null, 5);
		
		timeScale=1;
		originalImage=img;
		
		
		imp.getCanvas().addMouseWheelListener(new MyListener());
		
	//	sb=new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 1, model.getNumberOfFrames()+1);
	//	imp.getWindow().add(sb);
	//	sb.addAdjustmentListener(this);
	//	rePaint(viewModel.getPosition(),true);	
	}
	
	@Override
	public void rePaint(long[] position, boolean rePaintImage){
	//	long time0= System.nanoTime();
//		if(sb!=null){
//			if(sb.getValue()!=position[3]+1 )  sb.setValue((int) (position[3]+1));
			
//		}
		
		reDraw(position, rePaintImage);
	//	long time1= System.nanoTime();
	//	System.out.println("Time taken:"+((time1-time0)/1000000));
		
	}
	
	

	public void adjustmentValueChanged(AdjustmentEvent e) {
		System.err.println(" SLIDER HAS CHANGED !  ! ! !  ");
			if(sb.hasFocus());
			viewModel.setPosition(3,e.getValue()-1);
	}

	
	public class MyListener implements  MouseWheelListener{


		@Override
		public synchronized void  mouseWheelMoved(MouseWheelEvent e) {
			
				if(e.isShiftDown()){
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
				//timeScale=baseTimeScale*Math.pow(1.1, tics);
				
				//timeScale=timeScale*Math.pow(1.1, e.getWheelRotation());
				//rePaint(viewModel.getPosition(),true);
				//image = ImglibTools.scaleByFactor(this.originalImage,1,this.timeScale);
				timeScale=baseTimeScale*Math.pow(1.1, tics);
				viewModel.setPosition(-1, -1);
				
				//long[] pos= {0,0,0,0,0};
				
				//if(viewModel.getMutex()){
				//	rePaint(pos);
				//	viewModel.releaseMutex();
				//}
				
				
				System.out.println("baseTimeScale:"+baseTimeScale);
				System.out.println("timeScale:"+timeScale);
				
				
				// TODO Auto-generated method stub
				
				
				
			}
		
	}
	
	
}
