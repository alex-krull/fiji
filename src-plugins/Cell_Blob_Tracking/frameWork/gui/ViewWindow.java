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
	protected ViewWindow(Model<IT> mod, String title, ViewModel<IT> vm, int capacity){
		blockingQueue= new ArrayBlockingQueue<UpdateTask>(10);
		
		UpdateThread udt= new UpdateThread();	
		udt.start();
		
		//UpdateThread udt2= new UpdateThread();
		//udt2.start();
		viewModel=vm;
		model=mod;
		caption= title;
		
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
	public synchronized void upDate(long[] position, boolean rePaintImage){
		
		try {
			while(!blockingQueue.offer(new UpdateTask(position,rePaintImage))){
				UpdateTask udt=blockingQueue.peek();
				if(udt!=null) blockingQueue.remove(udt);				
			}
	
		} catch (Exception e) {
			e.printStackTrace();
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
	
	private class UpdateThread extends Thread{	
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
