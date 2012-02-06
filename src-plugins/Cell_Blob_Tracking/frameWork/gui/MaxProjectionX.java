package frameWork.gui;

import ij.gui.Overlay;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import tools.ImglibTools;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import frameWork.Model;
import frameWork.Trackable;



	public class MaxProjectionX <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> implements MouseListener, MouseMotionListener{
		
		public MaxProjectionX(Model<T,IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<T,IT> vm){
			super(mod, 
					//ImglibTools.scaleByFactor(
							Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(img,0),0,1) ),0  ) ) , 
					//0,mod.xyToZ)
					"max-X-projection",3, vm);
			this.scaleX=model.xyToZ;
			imp.getCanvas().addMouseListener(this);
			imp.getCanvas().addMouseMotionListener(this);
		}
		
		public void rePaint(long[] position, boolean rePaintImage){
			this.clearOverlay();
			
			this.addXOverlayes((int)position[3]);
			this.addXLineOverlay(((double)position[2]));
			super.rePaint(position, rePaintImage);
			}

		@Override
		public void mouseClicked(MouseEvent e) {
			int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			System.out.println("x:"+ x +"  y:"+y);
			if(e.getButton()==MouseEvent.BUTTON2) viewModel.setPosition(2,(int)((double)x));
			
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			if(e.getButton()==MouseEvent.BUTTON1){
				long[] pos= viewModel.getPosition();
				pos[0]=-1;
				pos[1]=y; 
				pos[2]=x;
				viewModel.mouseAtPosition(pos, e);
			}
						
		}

		@Override
		public void mouseReleased(MouseEvent e) {
					
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			System.out.println("x:"+ x +"  y:"+y);
			
			
				long[] pos= viewModel.getPosition();
				pos[0]=-1;
				pos[1]=y; 
				pos[2]=x;
				viewModel.mouseAtPosition(pos, e);
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

	}
	

