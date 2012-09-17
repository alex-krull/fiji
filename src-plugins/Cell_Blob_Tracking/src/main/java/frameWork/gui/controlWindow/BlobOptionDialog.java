package frameWork.gui.controlWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import blobTracking.Blob;
import blobTracking.BlobSession;

import frameWork.Model;
import ij.gui.GenericDialog;

public class BlobOptionDialog{

boolean isVolume;

	public BlobOptionDialog(Blob blobData, boolean isVolume) {
		GenericDialog gd = new GenericDialog("Session Options");

		// gd.setPreferredSize(new Dimension(300,200));

		gd.addCheckbox("Automatic \u03C3", blobData.autoSigma);
		gd.addNumericField("\u03C3:", blobData.sigma, 2);
		gd.addNumericField("Max \u03C3:", blobData.maxSigma, 2);
		gd.addNumericField("Min \u03C3:", blobData.minSigma, 2);


		if(isVolume==true){
			gd.addMessage("");
			gd.addNumericField("\u03C3Z:", blobData.sigmaZ, 2);
			gd.addNumericField("Max \u03C3Z:", blobData.maxSigmaZ, 2);
			gd.addNumericField("Min \u03C3Z:", blobData.minSigmaZ, 2);

		}


		gd.showDialog();
		if (gd.wasCanceled()) return;

		if(gd.wasOKed()){
			double init = gd.getNextNumber();
			double max=gd.getNextNumber();
			double min = gd.getNextNumber();


			if(isVolume==true){
				double initz=gd.getNextNumber();
				double maxz=gd.getNextNumber();
				double minz=gd.getNextNumber();

				blobData.sigmaZ=initz;
				blobData.maxSigmaZ=maxz;
				blobData.minSigmaZ=minz;
			}


			
			blobData.maxSigma=max;
			blobData.minSigma=min;
			blobData.sigma=init;
			


			blobData.autoSigma=gd.getNextBoolean();
		}



	}




}
