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
