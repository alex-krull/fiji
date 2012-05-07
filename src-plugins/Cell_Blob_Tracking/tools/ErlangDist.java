package tools;

import java.util.TreeMap;

public class ErlangDist {
	private final int input;
	private final double gain;
	private final TreeMap<Double, Integer> accProbFunc;
	public ErlangDist(int inp, double g, double thresh){
		
		input = inp;
		gain=g;
		int i=0;
		
		
		accProbFunc= new TreeMap<Double,Integer>();
		if(inp==0) return;
		
		double acProb=0;
		double temp=1;
		
		while(acProb<1-thresh &&i<100000){
			accProbFunc.put(acProb, i);
			
			i++;
			 temp=OtherTools.getErlangProbAlternative(input, i, gain);
			
			
			
			acProb=acProb+temp;
			
		}
		accProbFunc.put(1.0, i);
	}
	
	
	public int drawOutput(double rv){
		if(input==0) return 0;
		if(rv>1)return 0;
		return accProbFunc.ceilingEntry(rv).getValue();
	}
	
public int getLastPossibleOutput(){
		return accProbFunc.lastEntry().getValue();
	}
}
