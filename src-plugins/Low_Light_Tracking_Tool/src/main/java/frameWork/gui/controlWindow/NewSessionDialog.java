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

import javax.swing.JDialog;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import frameWork.Model;
import frameWork.gui.ViewModel;
import frameWork.Controller;

public class NewSessionDialog < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {

	private String methodChoice;
	private Integer channelChoice;
	private String userSessionName;

	public NewSessionDialog(Controller<IT> controller, Model <IT> model, int startingChannelNumber){
		JDialog.setDefaultLookAndFeelDecorated(true);


		String[] trackingMethods = controller.getPossibleSessionTypes();
		int channelNumber = model.getNumberOfChannels();
		String[] channelList = new  String[channelNumber];
		Integer i;
		for (i=1; i <= channelNumber; i++ ){
			channelList[i-1]= i +"";
		}

		GenericDialog gd = new GenericDialog("New Session");
		gd.addStringField("Enter new session name: ", "");
		gd.addChoice("Pick tracking method", trackingMethods, null);
		gd.addChoice("Pick channel to track", channelList, startingChannelNumber + "");
		gd.showDialog();
		if(gd.wasCanceled())
			return;
		userSessionName = gd.getNextString();




		methodChoice = gd.getNextChoice();
		channelChoice =Integer.valueOf(gd.getNextChoice());




	}
	public String getUserSessionName() {
		return userSessionName;
	}
	public String getMethodChoice() {
		return methodChoice;
	}

	public Integer getChannelChoice() {
		return channelChoice;
	}

}


