package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;




	public class MaxProjectionX < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> implements MouseListener, MouseMotionListener{
		
		public MaxProjectionX(Model<IT> mod, ViewModel<IT> vm){
			super(mod, 
				     null,		
					"max-X-projection",vm, null);
			this.scaleX=model.xyToZ;
		//	xSize=(int)model.getFrame(0, viewModel.getCurrentChannelNumber()).getXProjections().max(0);
		//	ySize=(int)model.getFrame(0, viewModel.getCurrentChannelNumber()).getXProjections().max(1);
			
			imp.getCanvas().addMouseListener(this);
			imp.getCanvas().addMouseMotionListener(this);
		}
		
		@Override
		public void reFresh(long[] position, boolean rePaintImage){
			this.clearOverlay();
			
			this.addXOverlayes((int)position[3]);
			this.addXLineOverlay(position[2]);
			
			int frameNumber= (int)position[3];
			toDraw=model.getFrame(frameNumber, viewModel.getCurrentChannelNumber()).getXProjections();
			reDraw( position ,rePaintImage);
			}

		@Override
		public void mouseClicked(MouseEvent e) {
			int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			System.out.println("x:"+ x +"  y:"+y);
			if(e.getButton()==MouseEvent.BUTTON2) viewModel.setPosition(2,(int)x);
			
			if(e.getButton()==MouseEvent.BUTTON1) viewModel.mouseAtPosition(positionFromEvent(e), e);
			
			
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
			
			if(e.getButton()==MouseEvent.BUTTON1) viewModel.mouseAtPosition(positionFromEvent(e), e);
			
			
						
		}

		@Override
		public void mouseReleased(MouseEvent e) {
					
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			viewModel.mouseAtPosition(positionFromEvent(e), e);
		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		
		private long[] positionFromEvent(MouseEvent e){
			int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			System.out.println("x:"+ x +"  y:"+y);
			
			
				long[] pos= viewModel.getPosition();
				pos[0]=-1;
				pos[1]=y; 
				pos[2]=x;
			return pos;
		}

	}
	

