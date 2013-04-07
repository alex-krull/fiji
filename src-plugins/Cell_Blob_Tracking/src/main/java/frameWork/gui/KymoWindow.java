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
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;


public abstract class KymoWindow <  IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> {

	protected RandomAccessibleInterval<IT> originalImage;
	protected double timeScale=1;
	protected double baseTimeScale=1;
	protected double tics=0;
	protected volatile Scrollbar sb;
	
	public KymoWindow(Model<IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<IT> vm, String label){
		super(mod, img, label,  vm, null);
		
		timeScale=1;
		originalImage=img;
		
	
		
	//	sb=new Scrollbar(Scrollbar.HORIZONTAL, 0, 1, 1, model.getNumberOfFrames()+1);
	//	imp.getWindow().add(sb);
	//	sb.addAdjustmentListener(this);
	//	rePaint(viewModel.getPosition(),true);	
	}
	

	@Override
	public void initWindow(){
		super.initWindow();
		
		
		
		MouseWheelListener [] wheelListeners=imp.getWindow().getMouseWheelListeners();
		for(int i=0;i<wheelListeners.length;i++){
			imp.getWindow().removeMouseWheelListener(wheelListeners[i]);
		}
		
		imp.getWindow().addMouseWheelListener(new MyListener());
		
	}
	

	public void adjustmentValueChanged(AdjustmentEvent e) {
		System.err.println(" SLIDER HAS CHANGED !  ! ! !  ");
			if(sb.hasFocus());
			viewModel.setPosition(3,e.getValue()-1);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		//no manipulation in the kymograph;
	}

	
	public class MyListener implements  MouseWheelListener{


		@Override
		public synchronized void  mouseWheelMoved(MouseWheelEvent e) {
			
				if(!e.isShiftDown()){
					int newPos= (int)(viewModel.getPosition()[3]+ e.getWheelRotation());
					newPos=Math.min(Math.max(newPos, 0), model.getNumberOfFrames()-1);
					viewModel.setPosition(3,newPos);
					return;
				}
				
				
				tics+= e.getWheelRotation();
				
				if(tics<0){
					tics=0;
			//		return;
				}
		
				
		//		System.out.println(xSize/baseTimeScale*Math.pow(1.1, tics));
		//		while(xSize/baseTimeScale*Math.pow(1.1, tics)<5.0)
		//			tics--;
				
				timeScale=baseTimeScale*Math.pow(1.1, tics);
				viewModel.setPosition(-1, -1);				
				
		//		System.out.println("baseTimeScale:"+baseTimeScale);
		//		System.out.println("timeScale:"+timeScale);
				
				
				// TODO Auto-generated method stub
				
				
				
			}
		
	}
	
	
}
