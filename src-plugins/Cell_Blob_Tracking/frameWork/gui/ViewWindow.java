package frameWork.gui;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;

public abstract class ViewWindow < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > implements Runnable{
	protected Model<IT> model;
	protected String caption;
	protected ViewModel<IT> viewModel;
	protected Thread thread;
	protected volatile UpdateTask currentUpdateTask=null;
	protected ViewWindow(Model<IT> mod, String title, ViewModel<IT> vm, int capacity){
		
		
		
		viewModel=vm;
		model=mod;
		caption= title;
		
		
		thread= new Thread(this);
	//.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	
	/**
	 * update the view
	 *
	 * The ViewWindow is updated and redrawn, considering the position given.
	 *
	 * @param position The position the ViewWindow should switch to.
	 * @param rePaintImage if true, the image in the View will be redrawn.
	 **/
	public abstract void rePaint(long[] position, boolean rePaintImage) ;
	
	
	/**
	 * update the view using its own thread 
	 *
	 * The task of updating the window is, added to the job-queue of the ViewWindows thread
	 * 
	 * @param position The position the ViewWindow should switch to.
	 * @param rePaintImage if true, the image in the View will be redrawn.
	 **/
	public void upDate(long[] position, boolean rePaintImage){

		UpdateTask udt= new UpdateTask(position,rePaintImage);
		synchronized( this){
			currentUpdateTask=udt;
			notify();
		}
	}
	
	private class UpdateTask{
		public long[] position;
		public boolean rePaintImage;
		public UpdateTask(long[] pos, boolean rpImage){
			position=pos;
			rePaintImage=rpImage;
		}
	}
	
	public void run(){
		UpdateTask udt=null;
		while(true){
			
			synchronized( this){
			if (currentUpdateTask!=null){
				
			
				udt=currentUpdateTask;
				currentUpdateTask=null;
			
			}else{
				try {
		//			long time0= System.nanoTime();
					wait();
		//			long time1= System.nanoTime();
		//			System.out.println("]]]]]]]]]]]]]]]]]]Time sleeping:"+((time1-time0)/1000));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			}
			if(udt!=null)rePaint(udt.position,udt.rePaintImage);
		}
	}
	
}
