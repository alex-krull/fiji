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
	
	public static Color colorFromIndex(int i){
		double h=0;
		float l=1;
		int remainder=i-1;
		double c=1;
		while(remainder>0){
			int newSummand= remainder % 3;
			remainder=(remainder-newSummand)/3;
			h=h+newSummand/Math.pow(3.0, c);
			while(h>1) h=h-1;
			l+=0.33333;
			while(l>1) l=l-1;
			c++;
		}
		
		return Color.getHSBColor((float)h, 1.0f, l);
		
	}
}
