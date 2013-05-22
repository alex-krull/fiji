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

public class MScaleOption{

	public MScaleOption(BlobSession<?> mod) {
		GenericDialog gd = new GenericDialog("MultiScale Options");

		gd.addNumericField("Downscaling:", mod.getDownscaleFactor(), 2);
		gd.addNumericField("Smoothing \u03C3:", mod.getMscaleSigma(), 2);
		gd.addNumericField("Iterations ", mod.getMscaleIterations(), 2);

		gd.showDialog();
		if (gd.wasCanceled()) return;

		if(gd.wasOKed()){
			double scaling = gd.getNextNumber();
			double smoothing=gd.getNextNumber();
			int interations = (int) gd.getNextNumber();

			mod.setMscaleIterations(interations);
			mod.setMscaleSigma(smoothing);
			mod.setDownscaleFactor(scaling);

		}

	}

	public static void main(String[] args) {
		MScaleOption test = new MScaleOption(null);
	}
}
