package tools;

import java.util.TreeMap;

public class ErlangDist {
	private final int input;
	private final double gain;
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
		
		while(acProb<1-thresh &&i<100000){
			accProbFunc.put(acProb, i);
	//		System.out.println(i);
			i++;
			 temp=OtherTools.getErlangProbAlternative(input, i, gain);
			
			
			
			acProb=acProb+temp;
			probFunc.put(i, temp);
			
		}
		accProbFunc.put(1.0, i);
		
	}
	
	
	public int drawOutput(double rv){
		if(input==0) return 0;
		if(rv>1)return 0;
		return accProbFunc.ceilingEntry(rv).getValue();
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
