/*******************************************************************************
 * This software implements the tracking method descibed in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2013 Alexandar Krull
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



public abstract class Trackable {
	public int sequenceId;
	public int channel;
	public int frameId;
	
	
	protected Trackable(int seqId, int fId, int  chan){
		sequenceId=seqId;
		frameId=fId;
		channel=chan;
	}

public abstract void addShapeZ(Overlay ov, boolean selected, Color c, boolean drawNumbers, double mag);
public abstract void addShapeX(Overlay ov, boolean selected, Color c, double mag);
public abstract void addShapeY(Overlay ov, boolean selected, Color c, double mag);
public abstract double getDistanceTo(double x, double y, double z);
public abstract String toSaveString();



}
