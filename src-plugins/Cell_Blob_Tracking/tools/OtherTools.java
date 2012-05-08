package tools;

import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

public class OtherTools {
	public static void writeProperties(Writer fileWriter, Properties properties) throws IOException{
			
		String props = properties.toString();
		String[] propsSeperated=props.replace("{", "").replace("}", "").split(",");
		
		
		for(int i=0; i<propsSeperated.length;i++){
			String s= propsSeperated[i].trim();
			
			fileWriter.write("%"+s+"\n");
		}
	}
	
	public static long fak(int n){
		long l=1;
		for(int i=1;i<=n;i++)
			l*=i;
		return l;
	}
	
	public static double getErlangProp(int n, int x, double g){
		if(n==0){
			
			if(x==0) return 1;
			else return 0;
		}
		double result=(Math.pow(x,(double) n-1)*Math.exp(-(double)x/g))
				/(Math.pow(g, n) * fak(n-1));
		
		if(result<0) System.out.println("n:"+n+ " x:"+x + " g:"+g);
				
		return Math.max(0, result);
	}
	
	public static double getErlangProbAlternative(int n, int x, double g){
		if(n==0){			
			if(x==0) return 1;
			else return 0;
		}

		double currentLogProb=Math.log(getErlangProp(1,x,g));
		
		for(int i=2;i<=n;i++){
		currentLogProb=currentLogProb
				+(Math.log(x) )
				-(Math.log(g)+Math.log(i-1));
		}
		
		return Math.exp(currentLogProb);
	}
	
	public static Color colorFromIndex(int i){
		double h=0;
		float l=1;
		int remainder=i-1;
		double c=1;
		
		while(remainder>=8){
			remainder-=8;
			l+=0.5;
			while(l>1) l=l-1;
		}
		
		while(remainder>0){
			int newSummand= remainder % 3;
			remainder=(remainder-newSummand)/3;
			h=h+newSummand/Math.pow(3.0, c);
			while(h>1) h=h-1;
	//		l+=0.33333;
	//		while(l>1) l=l-1;
			c++;
		}
	//	IJ.error(String.valueOf(h));
		return Color.getHSBColor((float)h, l,l);
		
	}
}
