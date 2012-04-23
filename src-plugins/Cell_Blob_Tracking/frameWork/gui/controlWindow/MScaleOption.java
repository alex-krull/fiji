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

		// gd.setPreferredSize(new Dimension(300,200));


		gd.addNumericField("Downscaling:", mod.getDownscaleFactor(), 2);
		gd.addNumericField("Smoothing \u03C3:", mod.getMscaleSigma(), 2);
		gd.addNumericField("Iterations \u03C3:", mod.getMscaleIterations(), 2);







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
