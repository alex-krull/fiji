package blobTracking;

import ij.ImagePlus;
import ij.gui.Roi;

import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Controler;
import frameWork.Gui;
import frameWork.Trackable;


public class BlobGui < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>  > extends Gui<Blob, IT> {
	

public BlobGui(ImagePlus imp, RandomAccessibleInterval<IT> img, Controler<Blob,IT> contr){
	super(imp, img, contr);
	
	
	for(int i=0;i<1000;i++){
 	   Blob tra= new Blob(20,20+ Math.sin(i/10.0f)*15,5,6);
 	   if(i==0) controler.selected=tra;
    controler.addTrackable(tra, i);
    controler.addTrackable(new Blob(20,20+ Math.sin(i/15.0f)*5,5,10), i);
    controler.addTrackable(new Blob(70,20+ Math.sin(i/45.0f)*5,5,10), i);
    controler.addTrackable(new Blob(20,70+ Math.sin(i/35.0f)*5,5,10), i);
    
 	   
    }
	
	
	
	
	
	
}


public void mouseDragged(MouseEvent arg0) {
	if(impZ!=null && impZ.getCanvas().equals(arg0.getSource())){
		int x=impZ.getCanvas().offScreenX(arg0.getX());
		int y=impZ.getCanvas().offScreenY(arg0.getY());
		System.out.println("setting x:"+ x +" setting y:"+y);
		controler.selected.xPos=x;
		controler.selected.yPos=y;
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		//this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);

		Roi r=null;
		impZ.setRoi(r);

	}
	
	
	if(impX!=null && impX.getCanvas().equals(arg0.getSource())){
		int z=impX.getCanvas().offScreenX(arg0.getX());
		int y=impX.getCanvas().offScreenY(arg0.getY());
		
		controler.selected.zPos=z;
		controler.selected.yPos=y;
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		//this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);

		Roi r=null;
		impX.setRoi(r);

	}
	
	
	if(impY!=null && impY.getCanvas().equals(arg0.getSource())){
		int x=impY.getCanvas().offScreenX(arg0.getX());
		int z=impY.getCanvas().offScreenY(arg0.getY());
		
		controler.selected.xPos=x;
		controler.selected.zPos=z;
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		//this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);

		Roi r=null;
		impY.setRoi(r);

	}
}

}


