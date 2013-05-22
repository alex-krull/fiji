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
import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public class MaxProjectionZ < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> 
{
	
	public MaxProjectionZ(Model<IT> mod, ViewModel<IT> vm){
		super(mod, null, "max-Z-projection", vm, null);
		startThread();
	
	}
	
	@Override
	public void reFresh(long[] position, boolean rePaintImage){	
		
		this.clearOverlay();
		addZOverlayes((int)position[3]);
		if(viewModel.mouseIsInWindow){
			this.addXLineOverlay(position[0]);
			this.addYLineOverlay(position[1]);
		}
		int frameNumber= (int)position[3];
		RandomAccessibleInterval<IT> toDraw=model.getFrame(frameNumber, viewModel.getCurrentChannelNumber()).getZProjections();
		reDraw( position ,rePaintImage, toDraw);
	}

	
	@Override
	public long[] positionFromEvent(MouseEvent e){
		int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
		System.out.println("x:"+ x +"  y:"+y);
		
		
			long[] pos= viewModel.getPosition();
			pos[0]=x;
			pos[1]=y; 
			pos[2]=-1;
		return pos;
	}

	@Override
	public void setWindowPosition(MainWindow<IT> mainWindow, MaxProjectionZ<IT> maxZWindow) {
		Point windowLoc = mainWindow.getWindow().getLocation();
		imp.getWindow().setLocation(windowLoc.x+mainWindow.getWindow().getWidth()+20, windowLoc.y+mainWindow.getWindow().getHeight());
		
	}
	
	
	
	

}
