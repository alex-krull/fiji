/*******************************************************************************
 * This software implements the tracking method descibed in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2013 Alexandar Krull
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
package frameWork;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;


public class MovieChannel <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> {
	private final	List<MovieFrame <IT>> frames;
	
	private RandomAccessibleInterval<IT> zProjections = null;
	private RandomAccessibleInterval<IT> xProjections = null;
	private RandomAccessibleInterval<IT> yProjections = null;
	private RandomAccessibleInterval<IT> xtProjections= null;
	private RandomAccessibleInterval<IT> ytProjections= null; 
	private final RandomAccessibleInterval<IT> image;
	private final long numOfFrames;
	private final int MovieChannelId;
	private final boolean isVolume;
	private final double xyToZ;
	public boolean isVolume() {
		return isVolume;
	}

	private final boolean isTimeSeries;
	
	

	public class ProjectionThreadKymographs extends Thread{
		private final MovieChannel <IT> channel;
		ProjectionThreadKymographs(MovieChannel <IT> chan){
			channel=chan;
		}
		
		@Override
		public void run() {
			try{
	       channel.getXTProjections();
	       channel.getYTProjections();
			}catch(Exception e){
				e.printStackTrace(Model.errorWriter);
				Model.errorWriter.flush();
			}
	    }
	}
	
	public class ProjectionThread extends Thread{
		private final MovieChannel <IT> channel;
		ProjectionThread(MovieChannel <IT> chan){
			channel=chan;
		}

		@Override
		public void run() {
			try{
				Model.getInstance().rwLock.writeLock().lock();

				for(int i=0;i<channel.getNumberOfFrames();i++){
					MovieFrame<IT> f=channel.getMovieFrame(i);
					f.getXProjections();
					f.getYProjections();
					f.getZProjections(); 
					
				}
				Model.getInstance().rwLock.writeLock().unlock();
			}catch(Exception e){
				e.printStackTrace(Model.errorWriter);
				Model.errorWriter.flush();
			}

		}
	}

	
	
	public MovieChannel(RandomAccessibleInterval<IT> view, int id, int nOfFrames, int cBackGround, double zRatio){
		
		
		//GaussNativeType<IT> gaussianFilter= new GaussNativeType<IT>(sigma, view, view, new  ArrayImgFactory<IT>(), view.randomAccess().get());
			//	ImageJFunctions.show(gaussianFilter.);
		
		xyToZ=zRatio;
		MovieChannelId=id;
		image=view;
		numOfFrames=nOfFrames;
		isTimeSeries = numOfFrames>1;
		isVolume = (image.numDimensions()>3|| (!isTimeSeries && image.numDimensions()>2) );
		
		
		frames= new ArrayList<MovieFrame<IT>>();
		
		for(int i=0;i<numOfFrames;i++){
			MovieFrame<IT> f=new MovieFrame<IT>(i, getFrameView(i),cBackGround);
			frames.add(f);
			
		}
		
		if(isVolume){
			ProjectionThread pt= new ProjectionThread(this);
			pt.setPriority(Thread.MIN_PRIORITY);
			pt.start();
		}
		
		
		ProjectionThreadKymographs ptk= new ProjectionThreadKymographs(this);
		ptk.setPriority(Thread.MIN_PRIORITY);
		ptk.start();
	}
	
	public RandomAccessibleInterval<IT> getXProjections(){
		if(xProjections==null) xProjections=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(image,0),0,1) ),0  ) ); 
		return xProjections;
	}

	public RandomAccessibleInterval<IT> getYProjections(){
		if(yProjections==null) yProjections=ImglibTools.projection(image,1);
		return yProjections;
	}

	public RandomAccessibleInterval<IT> getZProjections(){
		if(zProjections==null){
			if(isVolume) zProjections=ImglibTools.projection(image,2);
			else zProjections=image;
			
		}
		return zProjections;
	}
	
	public RandomAccessibleInterval<IT> getXTProjections(){
		if(xtProjections==null) xtProjections=Views.zeroMin( Views.invertAxis(    Views.rotate(ImglibTools.projection(getZProjections(),0),0,1 ),0 ) ) ;
		return xtProjections;
	}
	
	public RandomAccessibleInterval<IT> getYTProjections(){
		if(ytProjections==null) ytProjections=Views.zeroMin(    ImglibTools.projection(getZProjections(),1) )  ;
		return ytProjections;
	}

		
	
	private  RandomAccessibleInterval<IT> getFrameView(int frameNumber){
//		System.out.println("fn:"+frameNumber);
		if(isTimeSeries) return Views.hyperSlice(image, image.numDimensions()-1, frameNumber);
		else return image;
		
	}

	public MovieFrame<IT> getMovieFrame(int frame){
		
		return frames.get(frame);
	}
	
	public long getNumberOfFrames(){
		return numOfFrames;
	}
	
	public int getId(){
		return MovieChannelId;
	}
}
