package blobTracking;

import ij.ImagePlus;

import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.Type;
import frameWork.Gui;

public class BlobGui <IT extends Type<IT> > extends Gui<IT> {
	
public BlobGui(RandomAccessibleInterval<IT> img){
	image=img;
}

	
}
