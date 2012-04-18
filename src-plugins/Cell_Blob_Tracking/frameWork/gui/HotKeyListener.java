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
		           
		  case KeyEvent.VK_W:
			  viewModel.getController().newSession(viewModel);
			  
	
		  
		        
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
	
