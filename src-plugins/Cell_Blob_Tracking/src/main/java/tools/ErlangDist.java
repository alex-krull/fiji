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
package tools;

import java.util.Map.Entry;
import java.util.TreeMap;

public class ErlangDist {
	private final int input;
	private final double gain;
	private final double scale=1;
	private final TreeMap<Double, Integer> accProbFunc;
	private TreeMap<Integer, Double> probFunc;
	public ErlangDist(int inp, double g, double thresh){
		
		input = inp;
		gain=g;
		
		
		
		accProbFunc= new TreeMap<Double,Integer>();
		probFunc= new TreeMap<Integer,Double>();
		
		if(inp==0){
			probFunc.put(0, 1.0);
			return;
		}
		
		 
		int i=0;
		double acProb=0;
		double temp=0;
		
		while(acProb<scale &&i<Math.pow(2, 16)){
			
			accProbFunc.put(acProb, i);
//			System.out.println(accProbFunc.size());
			i++;
			 temp=OtherTools.getErlangProbAlternative(input, i, gain)*scale;
			//if(temp< (1e-5) &&i>gain*input)
			//	break;
			double acOld=acProb;			
			acProb=acProb+(temp);
			if(Math.abs((acProb-acOld)-temp)>1e-10)
				break;
		//	System.out.println((acProb-acOld)-temp);
		//	if(temp!= acProb-acOld &&i>gain*input)
		//		break;
			probFunc.put(i, temp);
			
			
			
		}
		accProbFunc.put(scale, i);
		System.out.println("SIZE:          "+ accProbFunc.size());
	}
	
	
	public int drawOutput(double rv){
		if(input==0) return 0;
		if(rv>1)return 0;
		Entry <Double,Integer> en=accProbFunc.ceilingEntry(rv*scale);
		if (en!=null) return en.getValue(); 
		
		en=accProbFunc.floorEntry(rv);
		double acProb=en.getKey();
		int i=0;
		int value= en.getValue();
		double temp=0;
		System.out.println("MISS!!!");
		while(acProb<rv &&value<Math.pow(2, 16)-1){
			 value++;
			 temp=OtherTools.getErlangProbAlternative(input, value, gain);
			 acProb=acProb+temp;
			 System.out.println(acProb+ "#"+ value);
		}
		return value;
	}
	
	public double getProb(int output){	
		Double d=this.probFunc.get(output);
		if(d==null) return 0;
		return d;
	}
	
public int getLastPossibleOutput(){
		return accProbFunc.lastEntry().getValue();
	}
}
