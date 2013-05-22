/*******************************************************************************
 * This software implements the tracking method described in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexander Krull
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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class KymographY <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends KymoWindow<IT> {

	
	private final boolean buisy=false;
	
	public KymographY(Model< IT> mod, RandomAccessibleInterval<IT> img, ViewModel<IT> vm, MainWindow<IT> mainWindow) {
		super(mod, img,vm, "Kymograph Y");
		ySize=model.getNumberOfFrames();
		if(ySize>300) ySize=300;
		if(ySize<100) ySize=100;
	    baseTimeScale=(double)ySize/(double)model.getNumberOfFrames();	
		timeScale=baseTimeScale*Math.pow(1.1, tics);
		Rectangle r= mainWindow.getWindow().getBounds();
	//	IJ.error("mw x:"+ String.valueOf(mainWindow.getWindow().getX()) +
	//			 "kw x:"+ imp.getWindow().getX());
	//	imp.getWindow().setLocation((int)r.getX(),(int)r.getMaxY());
	///	IJ.error("mw x:"+ String.valueOf(mainWindow.getWindow().getX()) +
	//			 "kw x:"+ imp.getWindow().getX());
		
	   
	    
	//	this.imp.getCanvas().addMouseListener(this);
	//	sb.setLocation(0, 0);
	//	sb.setOrientation(Scrollbar.VERTICAL);
		
		this.startThread();
	}
	
	@Override
	public void reFresh(long[] position, boolean rePaintImage){
		
		scaleY=timeScale;	
		transY=Math.max(0,(int)(scaleY*position[3])-ySize/2);
		transY=Math.min( transY, (int)((1+model.getYTProjections((int)position[4]).max(1))*scaleY)-ySize );
		
		this.clearOverlay();
	    this.addKymoYOverlayes();
		this.addYLineOverlay(position[3]);
		if(viewModel.mouseIsInWindow)  this.addXShortLineOverlay(position[0], position[3],10);
		RandomAccessibleInterval<IT> toDraw=model.getYTProjections((int)position[4]);
		reDraw(position, rePaintImage,toDraw);
	
	}

	
	
	@Override
	public long[] positionFromEvent(MouseEvent e){
		int x=(int)((imp.getCanvas().offScreenX(e.getX())+transX)/this.scaleX);
		int t=(int)((imp.getCanvas().offScreenY(e.getY())+transY)/this.scaleY);
	//	System.out.println("x:"+ x +"  y:"+y);
		
		
			long[] pos= viewModel.getPosition();
			pos[0]=x;
			pos[1]=-1; 
			pos[3]=t;
		return pos;
	}

	@Override
	public void setWindowPosition(MainWindow<IT> mainWindow,MaxProjectionZ<IT> maxZWindow) {
		Point windowLoc = mainWindow.getWindow().getLocation();
		
		
		imp.getWindow().setLocation(windowLoc.x, windowLoc.y+mainWindow.getWindow().getHeight());
		
	}
	


}