package frameWork.gui;

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
	protected int viewId;
	
	protected ViewWindow(Model<IT> mod, String title, ViewModel<IT> vm){
		
		
		
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
	public abstract void reFresh(long[] position, boolean rePaintImage) ;
	
	
	/**
	 * update the view using its own thread 
	 *
	 * The task of updating the window is, added to the job-queue of the ViewWindows thread
	 * 
	 * @param position The position the ViewWindow should switch to.
	 * @param rePaintImage if true, the image in the View will be redrawn.
	 **/
	public void upDate(long[] position, boolean rePaintImage){
		
			if(!this.isOpen()) return;
		
	//		reFresh( position,  rePaintImage);
			UpdateTask udt= new UpdateTask(position,rePaintImage);
			currentUpdateTask=udt;
			synchronized( thread){
				thread.notify();
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
	
	public abstract void addKeyListener(HotKeyListener keyListener);
	
	
	@Override
	public void run(){
		int counter=1;
		UpdateTask udt=null;
		while(true){
			try {
			synchronized( thread){
				if (currentUpdateTask==null)
						thread.wait();	
	//			else{
					udt=currentUpdateTask;
					currentUpdateTask=null;
	//			}
					
		//		currentUpdateTask=null;
			}
			} catch (InterruptedException e) { 	e.printStackTrace();}
			
				if(udt!=null)reFresh(udt.position,udt.rePaintImage);
				
		//		System.out.println(this.getClass().getName()+": "+counter);
				counter++;
		
		}
	}
	
	public String getCaption(){
		return caption;
	}
	
	public void setZoom(double newZoom){
		
	}
	
	public abstract void initWindow();
	public abstract void close();
	public abstract void open();
	public abstract boolean isOpen();
	public boolean showInWindowList(){
		return true;
	}
}
