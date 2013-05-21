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
package frameWork;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;

public class MovieFrame <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> {
	protected int frameNumber;
	
	private RandomAccessibleInterval<IT> zProjection = null;
	private RandomAccessibleInterval<IT> xProjection = null;
	private RandomAccessibleInterval<IT> yProjection = null;
	protected RandomAccessibleInterval<IT> frameView;
	protected int constBackground;
	protected boolean isVolume;
	
	public boolean isVolume(){
		return this.isVolume;
	}
	
	public int getNumberOfPlanes(){
		if(frameView.numDimensions()<3) return 1;
		return (int)frameView.dimension(2);
	}
	
	

	public RandomAccessibleInterval<IT> getFrameView() {
		return frameView;
	}

	public void setFrameView(RandomAccessibleInterval<IT> frameView) {
		this.frameView = frameView;
	}

	public MovieFrame(int frameNum, RandomAccessibleInterval<IT> view, int cBackGround){
	
		
		constBackground=cBackGround;
		frameView=view;
		frameNumber=frameNum;
		isVolume=(frameView.numDimensions()>2);
	}
	
	public synchronized RandomAccessibleInterval<IT> getXProjections(){
		if(!this.isVolume) return this.getFrameView();
		if(xProjection==null) xProjection=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(frameView,0),0,1) ),0  ) ); 
		return xProjection;
	}

	public synchronized RandomAccessibleInterval<IT> getYProjections(){
		if(!this.isVolume) return this.getFrameView();
		if(yProjection==null) yProjection=ImglibTools.projection(frameView,1);
		return yProjection;
	}

	public synchronized RandomAccessibleInterval<IT> getZProjections(){
		if(!this.isVolume) return this.getFrameView();
		if(zProjection==null) zProjection=ImglibTools.projection(frameView,2);
		return zProjection;
	}
	
	
}
