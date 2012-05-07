package frameWork;

import ij.IJ;
import fiji.tool.AbstractTool;


public class MyTool extends AbstractTool{
	
	@Override
	public void run(String s){
	//clearToolsIfNecessary=true;

		
		
		super.run(s);
	}
	
	public void shutDown(){
		this.unregisterTool();
		this.unregisterTool(IJ.getImage());
		
	}
	
	
}
