package tools;

import java.util.Map.Entry;
import java.util.TreeMap;

public class ErlangDist {
	private final int input;
	private final double gain;
	private final TreeMap<Double, Integer> accProbFunc;
	private TreeMap<Integer, Double> probFunc;
	public ErlangDist(int inp, double g, double thresh, boolean doProbFunc){
		
		input = inp;
		gain=g;
		
		
		
		accProbFunc= new TreeMap<Double,Integer>();
		if(doProbFunc)	probFunc= new TreeMap<Integer,Double>();
		
		if(inp==0){
			if(doProbFunc)	probFunc.put(0, 1.0);
			return;
		}
		
		 
		int i=0;
		double acProb=0;
		double temp=0;
		
		while(i<100){
			accProbFunc.put(acProb, i);
	//		System.out.println(i);
			i++;
			 temp=OtherTools.getErlangProbAlternative(input, i, gain);
			
			
			
			acProb=acProb+temp;
	//		if(i>10000)System.out.println("i:"+i+" p:"+temp);
	if(doProbFunc)	probFunc.put(i, temp);
			
		}
		//accProbFunc.put(1.0, i);
		
	}
	
	
	public int drawOutput(double rv){
		if(input==0) return 0;
		if(rv>1)return 0;
		Entry<Double,Integer> e= accProbFunc.ceilingEntry(rv);
		
		if(e!=null)return e.getValue();
		
		int i=accProbFunc.floorEntry(rv).getValue();	
		double accProb=accProbFunc.floorEntry(rv).getKey();
		while(i<1000000){
			i++;
			double temp=OtherTools.getErlangProbAlternative(input, i, gain);
			accProb=accProb+temp;
			if(i<10000 && accProb<(0.9))accProbFunc.put(accProb, i);
			if(rv<accProb)break ;
			
		}
		return i;
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
