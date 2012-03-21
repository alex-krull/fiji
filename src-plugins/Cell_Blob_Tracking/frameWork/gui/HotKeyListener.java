package frameWork.gui;

import ij.IJ;

import java.awt.Color;
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
		      
		  case KeyEvent.VK_G:			  
			  viewModel.toggleTracking();		 
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
		      
		    
		  case KeyEvent.VK_C:			  
			  viewModel.setColor(new Color(255,255,0));		 
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
	