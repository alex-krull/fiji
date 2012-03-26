package tools;

import java.io.FileWriter;
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
}
