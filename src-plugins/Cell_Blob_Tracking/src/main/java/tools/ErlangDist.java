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
