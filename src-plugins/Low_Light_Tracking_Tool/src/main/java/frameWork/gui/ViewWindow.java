/*******************************************************************************
 * This software implements the tracking method described in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexandar Krull
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Contributors:
 * 	Alexander Krull (Alexander.Krull@tu-dresden.de)
 *     Damien Ramunno-Johnson (GUI)
 *******************************************************************************/
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
	protected UpdateTask currentUpdateTask=null;
	protected int viewId;
	protected volatile boolean terminate=false;
	
	public static final int MODELCHANGED=0;
	public static final int MOUSEPOSCHANGED=1;
	public static final int VIEWMODELCHANGED=2;
	
	protected ViewWindow(Model<IT> mod, String title, ViewModel<IT> vm){
		
		
		
		viewModel=vm;
		model=mod;
		caption= title;
		
	}
	
	
	
	public void startThread(){
		thread= new Thread(this);
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
	 *iv:
	 * The task of updating the window is, added to the job-queue of the ViewWindows thread
	 * 
	 * @param position The position the ViewWindow should switch to.
	 * @param rePaintImage if true, the image in the View will be redrawn.
	 **/
	public void upDate(long[] position, boolean rePaintImage){
		
			if(!this.isOpen()) return;
		
	//	reFresh( position,  rePaintImage);
			synchronized( thread){
			UpdateTask udt= new UpdateTask(position,rePaintImage);
			if(currentUpdateTask!=null && currentUpdateTask.rePaintImage) udt.rePaintImage=true;
			currentUpdateTask=udt;
			
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
		try{
		long time0= System.nanoTime();
		int counter=1;
		UpdateTask udt=null;	
		while(!terminate){
			try {
			synchronized( thread){
				while (currentUpdateTask==null&& !terminate)
						thread.wait();	
	//			else{
					udt=currentUpdateTask;
					currentUpdateTask=null;
	//			}
					
		//		currentUpdateTask=null;
			}
			} catch (InterruptedException e) { 	e.printStackTrace();}
			
			
				
				if(udt!=null){
					
					
					model.rwLock.readLock().lock();	
					reFresh(udt.position,udt.rePaintImage);
					model.rwLock.readLock().unlock();
					
					
				}
				
			
				
		//		System.out.println(this.getClass().getName()+": "+counter);
				counter++;
				long time1= System.nanoTime();	
	//			System.out.println(this.getClass().getName()+": "+(1000000000*((double)counter/((double)(time1-time0)))));
		
		}
		}catch(Exception e){
			e.printStackTrace(Model.errorWriter);
			Model.errorWriter.flush();
		}
	}
	
	public String getCaption(){
		return caption;
	}
	
	public void setZoom(double newZoom){
		
	}
	
	public void terminate(){
		this.terminate=true;
		synchronized(thread){
			thread.notify();
		}
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public abstract void setWindowPosition(MainWindow<IT> mainWindow, MaxProjectionZ<IT> maxZWindow);
	public abstract void initWindow();
	public abstract void close();
	public abstract void open();
	public abstract boolean isOpen();
	public boolean showInWindowList(){
		return true;
	}
	
	public void saveWindow(double magnification){
		
	}
}
