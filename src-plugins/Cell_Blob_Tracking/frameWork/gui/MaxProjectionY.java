package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;



public class MaxProjectionY < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> {
		
		public MaxProjectionY(Model<IT> mod, ViewModel<IT> vm){
			super(mod, 
					//ImglibTools.scaleByFactor(
							null,
					//1,mod.xyToZ)
					"max-Y-projection",vm,null);
			this.scaleY=model.xyToZ;
		//	imp.getCanvas().addMouseListener(this);
		//	imp.getCanvas().addMouseMotionListener(this);
		}
		
		@Override
		public void reFresh(long[] position, boolean rePaintImage){
			this.clearOverlay();
			
			this.addYOverlayes((int)position[3]);
			this.addYLineOverlay(position[2]);
			if(viewModel.mouseIsInWindow) this.addXShortLineOverlay(position[0], position[2],10);
			int frameNumber= (int)position[3];
			toDraw=model.getFrame(frameNumber, viewModel.getCurrentChannelNumber()).getYProjections();
			reDraw( position ,rePaintImage);
			//super.rePaint(position, rePaintImage);
		}

	
		
		public long[] positionFromEvent(MouseEvent e){
			int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			System.out.println("x:"+ x +"  y:"+y);
			
			
				long[] pos= viewModel.getPosition();
				pos[0]=x;
				pos[1]=-1; 
				pos[2]=y;
			return pos;
		}

	
}
