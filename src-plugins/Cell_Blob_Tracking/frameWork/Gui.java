package frameWork;

import ij.ImageListener;
import ij.ImagePlus;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.Type;

public abstract class Gui<IT extends Type<IT>> implements MouseMotionListener, ImageListener {

	protected RandomAccessibleInterval<IT> image;

	
	

@Override
public void mouseDragged(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseMoved(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void imageClosed(ImagePlus arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void imageOpened(ImagePlus arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void imageUpdated(ImagePlus arg0) {
	// TODO Auto-generated method stub
	
}

}
