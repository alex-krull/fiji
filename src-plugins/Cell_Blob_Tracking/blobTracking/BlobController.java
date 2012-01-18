package blobTracking;

import ij.ImagePlus;
import ij.gui.Roi;

import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Controller;


public class BlobController < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>  > extends Controller<Blob> {
	

public BlobController(ImagePlus imp, RandomAccessibleInterval<IT> img, Model<Blob,IT> contr){
	super(contr);
	
	
	for(int i=0;i<1000;i++){

    model.addTrackable(new Blob(0,i,20,20+ Math.sin(i/15.0f)*5,5,10));
    model.addTrackable(new Blob(1,i,70,20+ Math.sin(i/45.0f)*5,5,10));
    model.addTrackable(new Blob(2,i,20,70+ Math.sin(i/35.0f)*5,5,10));
    
 	   
    }
	
	
	
	
	
	
}


public void click(long[] position){
	for(int i=0;i<position.length;i++){
		System.out.println(position[i]);
	}
	return;
}


/*
@Override
public void mouseClicked(MouseEvent arg0){
	//if(arg0.getClickCount()!=2) return;
	if(impZ!=null && impZ.getCanvas().equals(arg0.getSource())){
		int x=impZ.getCanvas().offScreenX(arg0.getX());
		int y=impZ.getCanvas().offScreenY(arg0.getY());
			
		if(arg0.getClickCount()==2){	
			controler.addTrackable(new Blob(3,currentFrameNumber,x,y,currentSliceNumber,1.2));
		}
		
		controler.optimizeFrame(currentFrameNumber);
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		
	}
	
	if(mainImage!=null && mainImage.getCanvas().equals(arg0.getSource())){
		int x=mainImage.getCanvas().offScreenX(arg0.getX());
		int y=mainImage.getCanvas().offScreenY(arg0.getY());
			
		if(arg0.getClickCount()==2)controler.addTrackable(new Blob(3,currentFrameNumber,x,y,currentSliceNumber,1.2));
		
		controler.optimizeFrame(currentFrameNumber);
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		
	}
}

@Override
public void mouseDragged(MouseEvent arg0) {
	if(impZ!=null && impZ.getCanvas().equals(arg0.getSource())){
		int x=impZ.getCanvas().offScreenX(arg0.getX());
		int y=impZ.getCanvas().offScreenY(arg0.getY());
		System.out.println("setting x:"+ x +" setting y:"+y);
		
		Blob blob=controler.getTrackable(selectedSequenceId, currentFrameNumber);
		blob.xPos=x;
		blob.yPos=y;
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		//this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);

		Roi r=null;
		impZ.setRoi(r);

	}
	
	if(mainImage!=null && mainImage.getCanvas().equals(arg0.getSource())){
		int x=mainImage.getCanvas().offScreenX(arg0.getX());
		int y=mainImage.getCanvas().offScreenY(arg0.getY());
		System.out.println("setting x:"+ x +" setting y:"+y);
		
		Blob blob=controler.getTrackable(selectedSequenceId, currentFrameNumber);
		blob.xPos=x;
		blob.yPos=y;
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		//this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);

		Roi r=null;
		mainImage.setRoi(r);

	}
	
	
	if(impX!=null && impX.getCanvas().equals(arg0.getSource())){
		int z=impX.getCanvas().offScreenX(arg0.getX());
		int y=impX.getCanvas().offScreenY(arg0.getY());
		
		Blob blob=controler.getTrackable(selectedSequenceId, currentFrameNumber);
		blob.zPos=z;
		blob.yPos=y;
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		//this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);

		Roi r=null;
		impX.setRoi(r);

	}
	
	
	if(impY!=null && impY.getCanvas().equals(arg0.getSource())){
		int x=impY.getCanvas().offScreenX(arg0.getX());
		int z=impY.getCanvas().offScreenY(arg0.getY());
		
		Blob blob=controler.getTrackable(selectedSequenceId, currentFrameNumber);
		blob.xPos=x;
		blob.zPos=z;
		this.addOverlays(currentFrameNumber, currentSliceNumber, currentChannelNumber);
		//this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);

		Roi r=null;
		impY.setRoi(r);

	}
}
*/

}


