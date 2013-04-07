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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import blobTracking.BlobSession;

import frameWork.Model;
import ij.gui.GenericDialog;

public class SessionOptionsDialog{



	public SessionOptionsDialog(BlobSession<?> mod) {
		GenericDialog gd = new GenericDialog("Session Options");

		// gd.setPreferredSize(new Dimension(300,200));

		gd.addCheckbox("Automatic \u03C3", mod.isAutoSigma());
		gd.addNumericField("\u03C3:", mod.getDefaultSigma(), 2);
		gd.addNumericField("Max \u03C3:", mod.getDefaultMaxSigma(), 2);
		gd.addNumericField("Min \u03C3:", mod.getDefaultMinSigma(), 2);


		if(mod.isVolune()){
			gd.addMessage("");
			gd.addNumericField("\u03C3Z:", mod.getDefaultSigmaZ(), 2);
			gd.addNumericField("Max \u03C3Z:", mod.getDefaultMaxSigmaZ(), 2);
			gd.addNumericField("Min \u03C3Z:", mod.getDefaultMinSigmaZ(), 2);

		}

		gd.addMessage("");
		gd.addNumericField("Quality Threshold:", mod.getQualityThreshold(), 4);


		gd.showDialog();
		if (gd.wasCanceled()) return;

		if(gd.wasOKed()){
			double init = gd.getNextNumber();
			double max=gd.getNextNumber();
			double min = gd.getNextNumber();


			if(mod.isVolune()){
				double initz=gd.getNextNumber();
				double maxz=gd.getNextNumber();
				double minz=gd.getNextNumber();

				mod.setDefaultSigmaZ(initz);
				mod.setDefaultMaxSigmaZ(maxz);
				mod.setDefaultMinSigmaZ(minz);
			}

			double qthreshold = gd.getNextNumber();
			mod.setQualityThreshold(qthreshold);
			
			mod.setDefaultMaxSigma(max);
			mod.setDefaultMinSigma(min);
			mod.setDefaultSigma(init);
			


			mod.setAutoSigma(gd.getNextBoolean());
		}



	}



	public static void main(String[] args) {
		SessionOptionsDialog test = new SessionOptionsDialog(null);

	}
}
