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

import ij.IJ;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
public class HotKeyListener implements KeyListener {
 
	private final ViewModel viewModel;
	public	HotKeyListener(ViewModel vm){
		viewModel=vm;
	}
	
	@Override
	public void keyPressed(KeyEvent keyEvent) {

		switch (keyEvent.getKeyCode()) {
		  case KeyEvent.VK_P:			  
			  viewModel.toggleDrawOverlays();		 
		      return;
		      
		  case KeyEvent.VK_C:
		  IJ.doCommand("Brightness/Contrast...");
		  return;
		      
		  case KeyEvent.VK_G:			  
			  viewModel.toggleTracking(false);		 
		      return;
		      
		  case KeyEvent.VK_L:			  
			  viewModel.toggleTracking(true);		 
		      return;
		      
		  case KeyEvent.VK_T:			  
			  viewModel.trimSequence();		 
		      return;
		      
		  case KeyEvent.VK_S:			  
			  viewModel.splitSequence();		 
		      return;
		      
		  case KeyEvent.VK_D:			  
			  viewModel.deleteSequence();		 
		      return;
		      
		  case KeyEvent.VK_A:			  
			  viewModel.saveAll();		 
		      return;
		      
		    
		  case KeyEvent.VK_R:			  
			  viewModel.getController().reFresh(); 
		      return;
		      
		  case KeyEvent.VK_M:			  
			  viewModel.mergeSequenences();		 
		      return; 
		     
		  case KeyEvent.VK_O:
			  viewModel.getController().showOjectOptions(viewModel.getCurrentFrameNumber());
			  return;
			  
		  case KeyEvent.VK_E:
			  viewModel.getController().getCurrentSession().showPropertiesDialog();
			  return;
		           
		  case KeyEvent.VK_N:
			  viewModel.getController().newSession(viewModel);
			  return;
			  
		  case KeyEvent.VK_1:
			  viewModel.toggleDrawNumbers();
			  return;
		
		  case KeyEvent.VK_F:
			  viewModel.getController().optimizeFrame(viewModel.currentFrameNumber);
			  return;
			  
		  case KeyEvent.VK_W:
			  viewModel.resetWindowsPositions();
			  return;
			  
		  case KeyEvent.VK_Q:
			  viewModel.getController().optimizeAllFrames(viewModel);
			  return;
	
		  
		        
		}
		IJ.getInstance().keyPressed(keyEvent);	//send event to imagej to use it
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent keyEvent) {
		
		

	}
}
	
