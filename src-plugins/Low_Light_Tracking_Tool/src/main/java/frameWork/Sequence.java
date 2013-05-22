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
package frameWork;

import ij.gui.Overlay;

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import tools.OtherTools;

public class Sequence<T extends Trackable> {
	protected int id;
	protected String label;
	protected SortedMap <Integer,T> trackables;
	protected Color color;
	protected Properties properties;
	protected Policy<T,?> policy;
	private final Session<T,?> session;
	private String path;


	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}


	public Session<T, ?> getSession() {
		return session;
	}

	public int getFirstFrame(){
		return trackables.firstKey();
	}

	public int getLastFrame(){
		return trackables.lastKey();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}



	public Sequence(int ident, String lab, Policy<T,?> pol,Session<T,?> sess, String fileName){
		session=sess;
		policy=pol;
		color=new Color(255,0,0,255);
		id = ident;
		label=lab;
		trackables=new TreeMap<Integer,T>();
		properties=new Properties();
		properties.setProperty("this", "property");
		properties.setProperty("is", "property");
		properties.setProperty("a", "property");
		properties.setProperty("test", "property");
		color=OtherTools.colorFromIndex(id);
		if(fileName==null){
			createFileName();
		}else{
			path=fileName;
		}
	}

	public void createFileName(){
		DecimalFormat df = new DecimalFormat("00");
		path= Model.getInstance().getImageFileNameNoEnding() + "_" +session.getLabel()+ "_" +
				this.getLabel()	+ "_"+df.format(this.getId())+".trcT";
	}

	public String getPath() {
		return path;
	}

	public void addTrackable(T trackable){
		trackables.put(trackable.frameId, trackable);

	}

	public T getTrackableForFrame(int frameNumber){
		return trackables.get(frameNumber);
	}


	public  SortedMap <Integer,T> getTrackables(){
		return trackables;
	}

	public String getTypeName(){
		return policy.getTypeName();
	}

	public int getId(){
		return id;
	}

	public boolean writeToFile(FileWriter fileWriter) throws IOException{
		fileWriter.write("%-sequence properties-\n");
		OtherTools.writeProperties(fileWriter, getProperties());		
		fileWriter.write("%-data-\n");
		for(T trackable: trackables.values()){
			fileWriter.write(trackable.toSaveString()+"\n");
		}

		fileWriter.flush();




		return true;
	}

	public void setProperties(Properties props){
		String s;
		int red=255; int green=0; int blue=0;
		s= props.getProperty("seqId"); if(s!=null) this.id=Integer.valueOf(s);
		s= props.getProperty("label"); if(s!=null) this.label=s;
		s= props.getProperty("color-red"); if(s!=null) red=Integer.valueOf(s);
		s= props.getProperty("color-green"); if(s!=null) green=Integer.valueOf(s);
		s= props.getProperty("color-blue"); if(s!=null) blue=Integer.valueOf(s);
		this.color=new Color(red,green,blue);
	}

	public Properties getProperties(){
		Properties props= new Properties();
		props.setProperty("seqId",String.valueOf(getId()));
		props.setProperty("label",String.valueOf(this.label));
		props.setProperty("color-red",String.valueOf(color.getRed()));
		props.setProperty("color-green",String.valueOf(color.getGreen()));
		props.setProperty("color-blue",String.valueOf(color.getBlue()));

		return props;
	}



	public void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected){	
		policy.getKymoOverlayX(ov, scaleX, scaleY, transX, transY, selected, trackables, color);

	}

	public void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected){
		policy.getKymoOverlayY(ov, scaleX, scaleY, transX, transY, selected, trackables, color);
	}




}
