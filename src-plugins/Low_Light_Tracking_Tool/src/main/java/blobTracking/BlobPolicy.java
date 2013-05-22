/*******************************************************************************
 * This software implements the tracking method described in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexander Krull
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Contributors:
 * 	Alexander Krull (Alexander.Krull@tu-dresden.de)
 *     Damien Ramunno-Johnson (GUI)
 *******************************************************************************/
package blobTracking;

import ij.gui.Line;
import ij.gui.Overlay;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.ChannelController;
import frameWork.Model;
import frameWork.MovieChannel;
import frameWork.Policy;
import frameWork.Sequence;
import frameWork.Session;
import frameWork.TrackingFrame;
import frameWork.gui.ViewModel;

public abstract class BlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends Policy<Blob, IT>{

	@Override
	public ChannelController<Blob, IT> produceControllerAndChannel(
			Properties sessionProps, Model<IT> model) {
		Integer cid= Integer.valueOf(sessionProps.getProperty("channelId"));
		if(cid==null)cid=0;
		Integer sid= Integer.valueOf(sessionProps.getProperty("sessionId"));
		if(sid==null)sid=model.getNextTCId();
		Session<Blob, IT> btc=  new BlobSession<IT>(sid, this, model.getMovieChannel(cid));
		
		btc.setProperties(sessionProps);
		model.addTrackingChannel(btc, btc.getId());
		return new ChannelController<Blob,IT>(model,btc, this);
	}

	
	@Override
	public Sequence<Blob> produceSequence(int ident, String lab, Session<Blob, IT> session, String filePath) {
		return new Sequence<Blob>( ident,  lab, this, session, filePath);
	}
	
	
	@Override
	public void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected,
			SortedMap <Integer,Blob> trackables, Color color){
		
	Blob b=null;
	Blob lastB=null;
	double stepSize=1;
	for(double i=(transX/scaleX);i<=((301+transX)/scaleX);i+=stepSize){
	//	if((i+0.5)*scaleX-transX<0 || (i+0.5)*scaleX-transX>501) continue;
		
		lastB=b;
		b=trackables.get((int)i);
		if(b==null) continue;
		
		if(lastB!=null&&b!=null){
			Line l=new Line((i-1+0.5)*scaleX-transX,(lastB.yPos+0.5)*scaleY-transY,(i+0.5)*scaleX-transX,(b.yPos+0.5)*scaleY-transY);
			l.setStrokeColor(color);
			l.setStrokeWidth(1);
			if(selected) l.setStrokeWidth(4);
			ov.add(l);
			
			
			
		
		}
		if(!(trackables.keySet().contains((int)i+1)) && !(trackables.keySet().contains((int)i-1) ) ){
			
			
			Line lStart1 =new Line((i+0.5)*scaleX-transX-1,(b.yPos+0.5)*scaleY-transY-1,
					(i+0.5)*scaleX-transX+1,(b.yPos+0.5)*scaleY-transY+1);
			lStart1.setStrokeColor(color);
			lStart1.setStrokeWidth(1);
			if(selected) lStart1.setStrokeWidth(4);
			ov.add(lStart1);
			
			Line lStart2 =new Line((i+0.5)*scaleX-transX+1,(b.yPos+0.5)*scaleY-transY-1,
					(i+0.5)*scaleX-transX-1,(b.yPos+0.5)*scaleY-transY+1);
			lStart2.setStrokeColor(color);
			lStart2.setStrokeWidth(1);
			if(selected) lStart2.setStrokeWidth(4);
			ov.add(lStart2);
		
		
	}
	}
	}
	
	@Override
	public void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected,
			SortedMap <Integer,Blob> trackables, Color color){
		

		
		Blob b=null;
		Blob lastB=null;
		double stepSize=1;
		for(double i=(transY/scaleY);i<=((301+transY)/scaleY);i+=stepSize){
	//		if((i+0.5)*scaleY-transX<0 || (i+0.5)*scaleY-transX>501) continue;
			lastB=b;
			b=trackables.get((int)i);
			if(b==null) continue;
			if(lastB!=null&&b!=null){
				Line l =new Line((lastB.xPos+0.5)*scaleX-transX,(i-1+0.5)*scaleY-transY,(b.xPos+0.5)*scaleX-transX,(i+0.5)*scaleY-transY);
				l.setStrokeColor(color);
				l.setStrokeWidth(1);
				if(selected) l.setStrokeWidth(4);
				ov.add(l);
				
			}
			
			if(!(trackables.keySet().contains((int)i+1)) && !(trackables.keySet().contains((int)i-1) ) ){
				
				
					
					Line lStart1 =new Line((b.xPos+0.5)*scaleX-transX-1,(i+0.5)*scaleY-transY-1,
							(b.xPos+0.5)*scaleX-transX+1,(i+0.5)*scaleY-transY+1);
					lStart1.setStrokeColor(color);
					lStart1.setStrokeWidth(1);
					if(selected) lStart1.setStrokeWidth(4);
					ov.add(lStart1);
					
					Line lStart2 =new Line((b.xPos+0.5)*scaleX-transX+1,(i+0.5)*scaleY-transY-1,
							(b.xPos+0.5)*scaleX-transX-1,(i+0.5)*scaleY-transY+1);
					lStart2.setStrokeColor(color);
					lStart2.setStrokeWidth(1);
					if(selected) lStart2.setStrokeWidth(4);
					ov.add(lStart2);
				
				
			}
		}
		
	
		

	}
	
	@Override
	public TrackingFrame<Blob,IT> produceFrame(int frameNum, MovieChannel<IT> mChannel) {
		return new BlobFrame<IT>(frameNum, mChannel.getMovieFrame(frameNum), this);
	}
	
	@Override
	public Blob loadTrackableFromString(String s, int sessionId) {
	//	System.out.println(s);
	
		String[] values=s.split("\t");
	
		
		
		int fNum= Integer.valueOf(values[0]); 
		int sId= Integer.valueOf(values[1]);
		double x= Double.valueOf(values[2]);
		double y= Double.valueOf(values[3]);
		double z= Double.valueOf(values[4]);
		double sigma= Double.valueOf(values[5]); 
		double sigmaZ= Double.valueOf(values[6]);
		double maxSigma= Double.valueOf(values[7]);
		
		boolean sigmaConst = values[8].equals("1");
		
		boolean coupled=false;
		double inten=0;
		
		if(values.length>10){					// the colomn for coupled is optional
		coupled= values[9].equals("1");		
		inten= Double.valueOf(values[10]);
		}
		else{
			inten= Double.valueOf(values[9]);
		}
		
	
		
		Blob nB=new Blob(sId, fNum, x, y, z, sigma, sessionId, !sigmaConst, sigmaZ, maxSigma);
		nB.inten=inten;
		nB.coupled=coupled;
		
		if(values.length>11){
			nB.backInten=Double.valueOf(values[12]);
			double temp=(Double.valueOf(values[13]));
			nB.numberOfPixels=(int)temp;
			nB.pK=Double.valueOf(values[14]);
			nB.totalInt=Double.valueOf(values[15]);
		}
		
		return nB;
	}
	
	@Override
	public synchronized int click(long[] pos, MouseEvent e, Model<IT> model,
			List<Integer>  selectedIdList, Session<Blob,IT> trackingChannel,
			int selectedSequenceId, ViewModel <IT> vm){
		
		
		Blob selectedTrackable;
		
		if(e.getID()==MouseEvent.MOUSE_PRESSED && e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()==1){
			if(pos[0]>=0&&pos[1]>=0)pos[2]=-1;
			selectedSequenceId=trackingChannel.selectAt((int)pos[0],(int) pos[1],(int) pos[2],(int) pos[3],(int) pos[4]);	 
		
			
			if(!e.isShiftDown()){
				selectedIdList.clear();			
			}
			if(selectedIdList.contains(selectedSequenceId)){
				selectedIdList.remove(new Integer( selectedSequenceId));	
			}else {
				
				selectedIdList.add(new Integer( selectedSequenceId));
			}
			
			
			
			
			model.makeStructuralChange();
			
		}
		
		if(e.getID()==MouseEvent.MOUSE_CLICKED){
			
			if( e.getClickCount()==2){
				BlobSession<IT> bs= (BlobSession<IT>)trackingChannel;
				Blob nB=new Blob(model.getNextSequqnceId(), (int)pos[3], pos[0], pos[1], vm.getCurrentSliceNumber()*Model.getInstance().getXyToZ(), 1,
						trackingChannel.getId(), false, bs.getDefaultSigmaZ(), bs.getDefaultMaxSigma());				
				
				nB.sigma=bs.getDefaultSigma();
				nB.sigmaZ=bs.getDefaultSigmaZ();
				nB.minSigma=bs.getDefaultMinSigma();
				nB.maxSigma=bs.getDefaultMaxSigma();
				nB.minSigmaZ=bs.getDefaultMinSigmaZ();
				nB.maxSigmaZ=bs.getDefaultMaxSigmaZ();
				nB.autoSigma=bs.isAutoSigma();
				nB.autoSigmaZ=bs.isAutoSigmaZ();
				
			
				
				selectedIdList.clear();
				selectedIdList.add(nB.sequenceId);
				trackingChannel.addTrackable(nB);
			}
			
			
			model.makeStructuralChange();
			
		}

		
		if(e.getID()==MouseEvent.MOUSE_DRAGGED){
			if(pos[0]>=0&&pos[1]>=0)pos[2]=-1;
			selectedTrackable=trackingChannel.getTrackable(selectedSequenceId, (int)pos[3]);
			if(selectedTrackable==null){
	//			System.out.println("selectedTrackable==null");
				return selectedSequenceId; 
			}
			if(pos[0]>=0)selectedTrackable.xPos=pos[0];
			if(pos[1]>=0)selectedTrackable.yPos=pos[1];
			if(pos[2]>=0)selectedTrackable.zPos=pos[2]*model.getXyToZ();
			System.out.println("z:"+selectedTrackable.zPos);
			
		}
		
				model.makeChangesPublic();
		return selectedSequenceId;
	}
	
	@Override
	public Blob copy(Blob toCopy){
		if(toCopy==null) return null;
		Blob result=new Blob(toCopy.sequenceId, toCopy.frameId, toCopy.xPos, toCopy.yPos, toCopy.zPos,
				toCopy.sigma, toCopy.channel, toCopy.autoSigma,toCopy.sigmaZ, toCopy.maxSigma);
		result.pK=toCopy.pK;
		result.inten=toCopy.inten;
		result.backInten=toCopy.backInten;
		result.sigma=toCopy.sigma;
		result.sigmaZ=toCopy.sigmaZ;
		result.minSigma=toCopy.minSigma;
		result.maxSigma=toCopy.maxSigma;
		result.minSigmaZ=toCopy.minSigmaZ;
		result.maxSigmaZ=toCopy.maxSigmaZ;
		result.autoSigma=toCopy.autoSigma;
		result.autoSigmaZ=toCopy.autoSigmaZ;
		return result;
	}

	@Override
	public void copyOptions(Blob src, Blob dst){
		dst.autoSigma=src.autoSigma;
		dst.autoSigmaZ=src.autoSigmaZ;
		dst.sigma=src.sigma;
		dst.sigmaZ=src.sigmaZ;
		dst.minSigma=src.minSigma;
		dst.maxSigma=src.maxSigma;
		dst.minSigmaZ=src.minSigmaZ;
		dst.maxSigmaZ=src.maxSigmaZ;
	}

}
