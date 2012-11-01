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
	
	public static double bessi0( double x )
	/*------------------------------------------------------------*/
	/* PURPOSE: Evaluate modified Bessel function In(x) and n=0.  */
	/*------------------------------------------------------------*/
	{
	   double ax,ans;
	   double y;


	   if ((ax=Math.abs(x))<  3.75) {
	      y=x/3.75;y=y*y;
	      ans=1.0+y*(3.5156229+y*(3.0899424+y*(1.2067492
	         +y*(0.2659732+y*(0.360768e-1+y*0.45813e-2)))));
	   } else {
	      y=3.75/ax;
	      ans=(Math.exp(ax)/Math.sqrt(ax))*(0.39894228+y*(0.1328592e-1
	         +y*(0.225319e-2+y*(-0.157565e-2+y*(0.916281e-2
	         +y*(-0.2057706e-1+y*(0.2635537e-1+y*(-0.1647633e-1
	         +y*0.392377e-2))))))));
	   }
	 
	   return ans;
	}




	public static double bessi1( double x)
	/*------------------------------------------------------------*/
	/* PURPOSE: Evaluate modified Bessel function In(x) and n=1.  */
	/*------------------------------------------------------------*/
	{
	   double ax,ans;
	   double y;


	   if ((ax=Math.abs(x))<  3.75) {
	      y=x/3.75;y=y*y;
	      ans=ax*(0.5+y*(0.87890594+y*(0.51498869+y*(0.15084934
	         +y*(0.2658733e-1+y*(0.301532e-2+y*0.32411e-3))))));
	   } else {
	      y=3.75/ax;
	      ans=0.2282967e-1+y*(-0.2895312e-1+y*(0.1787654e-1
	         -y*0.420059e-2));
	      ans=0.39894228+y*(-0.3988024e-1+y*(-0.362018e-2
	         +y*(0.163801e-2+y*(-0.1031555e-1+y*ans))));
	      ans *= (Math.exp(ax)/Math.sqrt(ax));
	   }
	   return x<  0.0 ? -ans : ans;
	} 
}


