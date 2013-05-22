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

import frameWork.Model;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;
import ij.process.ImageProcessor;

import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.text.DecimalFormat;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;


public class MainWindow  < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> 
implements ImageListener, MouseListener, MouseMotionListener{
	
	private class MyStackWindow extends StackWindow{	
		
		
		public MyStackWindow(ImagePlus imp) {
			super(imp);
		}
		
		
		@Override
		public void adjustmentValueChanged(java.awt.event.AdjustmentEvent e){
//		public void updateImage(ImagePlus imp){	
			
			if(!model.hasSwitchedDimension()){
				
					System.out.println("no SwitchedDimension");
					
					//viewModel.setPosition(3, this.getImagePlus().getFrame()-1);
					if(model.isVolume()) viewModel.setPosition(2, sliceSelector.getValue()-1);
					if(model.isTimeSequence()) viewModel.setPosition(3, tSelector.getValue()-1);
					if(model.isMultiChannel()) viewModel.setPosition(4, cSelector.getValue()-1);
				
			
			}else{
				    System.out.println("SwitchedDimension");
		//			viewModel.setPosition(3, currentFrameNumber);
				    if(model.isTimeSequence()) viewModel.setPosition(3, sliceSelector.getValue()-1);
					if(model.isMultiChannel()) viewModel.setPosition(4, cSelector.getValue()-1);

			
			
			//	viewModel.setPosition(4, currentChannelNumber);
				
			}
				
		
			//super.adjustmentValueChanged(e);
			
			
		}
		
	}
	


	private int currentFrameNumber;
	private int currentSliceNumber;
	private int currentChannelNumber;
		
	public MainWindow(ImagePlus imagePlus, Model< IT> mod, ViewModel< IT> vm) {
		super(mod, mod.getImage(), imagePlus.getWindow().getTitle(), vm, imagePlus);
		
		
		currentFrameNumber= imp.getFrame()-1;
		currentSliceNumber= imp.getSlice()-1;
		currentChannelNumber=imp.getChannel()-1;
		//imp.getWindow().close();
		imp.setOpenAsHyperStack(true);
		ImageCanvas ic=imp.getCanvas();
		ic.setVisible(true);
		
		
		ImagePlus.addImageListener(this);
		xSize=imp.getWidth();
	 	ySize=imp.getHeight();
		initWindow();
		
		
			imp.getWindow().setLocation(0, 0);
		
		this.startThread();
	}

	@Override
	public void reFresh(long[] position, boolean rePaintImage) {
		ImagePlus.removeImageListener(this);
		
		currentFrameNumber= (int)position[3];
		currentSliceNumber= (int)position[2];
		currentChannelNumber=(int)position[4];
		
		if(!model.hasSwitchedDimension()){
			if(currentFrameNumber != imp.getFrame()-1
				|| currentSliceNumber!= imp.getSlice()-1
				|| currentChannelNumber!= imp.getChannel()-1) 
					imp.setPosition(currentChannelNumber+1, currentSliceNumber+1, currentFrameNumber+1);
		}else
		{
			if(currentFrameNumber != imp.getSlice()-1					
					|| currentChannelNumber!= imp.getChannel()-1) 
						imp.setPosition(currentChannelNumber+1, currentFrameNumber+1, 0);
		}
		
		this.clearOverlay();
		addZOverlayes((int)position[3]);
		if(viewModel.mouseIsInWindow) {
			this.addXLineOverlay(position[0]);
			this.addYLineOverlay(position[1]);
		}
		
		this.upDateOverlay();
		imp.updateAndDraw();

		ImagePlus.addImageListener(this);
	}
	
	

	@Override
	public void imageClosed(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageOpened(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
public synchronized void imageUpdated(ImagePlus arg0) {
		
		
		if(!arg0.equals(imp)) return;
		
		if(!model.hasSwitchedDimension()){
		
			if(currentFrameNumber != imp.getFrame()-1){
				currentFrameNumber= imp.getFrame()-1;
				viewModel.setPosition(3, currentFrameNumber);
		
				return;
			}
		
			if(currentSliceNumber!= imp.getSlice()-1){
				currentSliceNumber= imp.getSlice()-1;
				viewModel.setPosition(2, currentSliceNumber);
		
				return;
			}
		
		}else{
			
			if(currentFrameNumber!= imp.getSlice()-1){
				currentFrameNumber=imp.getSlice()-1;
				viewModel.setPosition(3, currentFrameNumber);
			}
		}
		
		
		
		if(currentChannelNumber!= imp.getChannel()-1){
			currentChannelNumber= imp.getChannel()-1;
			viewModel.setPosition(4, currentChannelNumber);
			
			return;
		}
			
	
		return;
		
	}

	
	

	@Override
	public void initWindow() {
		synchronized (this){
			
			
			
			new StackWindow(imp,new MyCanvas(imp));	
			
			WindowListener [] wListeners=imp.getWindow().getWindowListeners();
			for(int i=0;i<wListeners.length;i++){
				imp.getWindow().removeWindowListener(wListeners[i]);
			}
				
			
			imp.getWindow().addWindowListener(new MyWindowListener());
			imp.getCanvas().addMouseListener(this);
			imp.getCanvas().addMouseMotionListener(this);
		}
		}
	
	//@Override
	//public void upDate(long[] position, boolean rePaintImage){
	//	reFresh(position,rePaintImage);
	//}

	@Override
	public void setWindowPosition(MainWindow<IT> mainWindow, MaxProjectionZ<IT> maxZmaxZWindow) {

	//	imp.getWindow().setLocation(50, 50);

	}

	@Override
	public long[] positionFromEvent(MouseEvent e){
		int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
		int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
	//	System.out.println("x:"+ x +"  y:"+y);
		
		
			long[] pos= viewModel.getPosition();
			pos[0]=x;
			pos[1]=y; 			
		return pos;
	}
	
	public Window getWindow(){
		return imp.getWindow();
	}

	@Override
	public void saveWindow(double magnification) {
	

		ImageProcessor ipro= imp.getProcessor().convertToRGB();
		ImagePlus ip= new ImagePlus();
		
		
		ipro=ipro.resize((int)(ipro.getWidth()*magnification) , (int)(ipro.getHeight()*magnification));
		ip.setProcessor(ipro);
	//	IJ.save(ip, model.getProjectDirectory()+ "save"); 
		this.clearOverlay();
		addZOverlayes(viewModel.getCurrentFrameNumber(),magnification);
		ip.getProcessor().drawOverlay(this.upDateOverlay(1.0/magnification));
		
		DecimalFormat df = new DecimalFormat("0000");
		String path= model.getProjectDirectory()+ "movieMain";
		
		File f= new File(path);
		f.mkdir();
		
		path= model.getProjectDirectory()+ "movieMain/"+df.format(viewModel.getCurrentFrameNumber())+"main"+".bmp";
		IJ.save(ip, path ); 
	
		
	}
}
