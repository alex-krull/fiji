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
package frameWork.gui.controlWindow;

import ij.gui.GenericDialog;
import frameWork.Model;

public class GlobalOptionsDialog {
	
	private boolean oked;

	public GlobalOptionsDialog(Model<?> mod) {
		GenericDialog gd = new GenericDialog("Global Options");
		gd.addNumericField("Intensity Offset: ", mod.getIntensityOffset(), 0);
		gd.addNumericField("EMCCD gain: ", mod.getEMCCDGain(), 3);
		gd.addNumericField("Counts per electron: ", mod.getADUperE(), 3);		
		if(mod.isVolume()) gd.addNumericField("\u0394 Z (in pixels): ", mod.getXyToZ(), 3);
		
		gd.showDialog();
		if (gd.wasCanceled()) return;

		
		oked=gd.wasOKed();
		if (gd.wasOKed()){
			mod.setIntensityOffset( (int)gd.getNextNumber());
			mod.setEMCCDGain(gd.getNextNumber());
			mod.setADUperE(gd.getNextNumber());
			if(mod.isVolume()) mod.setXyToZ( gd.getNextNumber() );
		}
	}

	public boolean wasOked(){
		return oked;
	}
	


	

	public static void main(String[] args) {
		GlobalOptionsDialog test = new GlobalOptionsDialog(null);

	}
}
