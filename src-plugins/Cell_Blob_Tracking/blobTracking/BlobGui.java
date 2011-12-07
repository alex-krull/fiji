package blobTracking;

import ij.ImagePlus;

import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Controler;
import frameWork.Gui;

public class BlobGui < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>  > extends Gui<Blob, IT> {
	

public BlobGui(ImagePlus imp, RandomAccessibleInterval<IT> img, Controler<Blob,IT> contr){
	super(imp, img, contr);
}

	
}
