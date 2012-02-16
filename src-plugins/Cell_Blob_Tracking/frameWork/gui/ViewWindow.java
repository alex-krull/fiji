package frameWork.gui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;

public abstract class ViewWindow < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	protected Model<IT> model;
	protected String caption;
	protected ViewModel<IT> viewModel;
	protected BlockingQueue<UpdateTask> blockingQueue;
	protected ViewWindow(Model<IT> mod, String title, ViewModel<IT> vm){
		blockingQueue= new LinkedBlockingQueue<UpdateTask>(1);
		UpdateThread udt= new UpdateThread();
		udt.start();
		viewModel=vm;
		model=mod;
		caption= title;
		
	}
	
	public abstract void rePaint(long[] position, boolean rePaintImage);
	
	public synchronized void upDate(long[] pos, boolean rpImage){
		try {
			blockingQueue.put(new UpdateTask(pos,rpImage));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected class UpdateTask{
		public long[] position;
		public boolean rePaintImage;
		public UpdateTask(long[] pos, boolean rpImage){
			position=pos;
			rePaintImage=rpImage;
		}
	}
	
	protected class UpdateThread extends Thread{
		long[] position;
		boolean rePaintImage;
		
		public UpdateThread(){
	
		}
		public void run() {
		   while(true){
			   try {
				UpdateTask item = blockingQueue.take();
				rePaint(item.position,item.rePaintImage);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   }
		}  
	}
}
