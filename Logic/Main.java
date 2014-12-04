package Logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Main {
	public static void main(String[] args) {
		//	Start the game
		new PolarTTT().begin();
	}
	
	/**
	 * Mainly for Debugging. Prints out both a meaningful label and the value of any variable
	 * @param anchor A hopefully meaningful label
	 * @param var A hopefully non-null variable
	 */
	public static void sout(String anchor, Object var){
		System.out.println(anchor + ": " + (var == null ? "null" : var));
	}
	
	public static float[][] csv_to_float(String filename) {
		BufferedReader file = null;
		String raw = "";
		try {
			file = new BufferedReader(new FileReader(filename));
			while (file.ready()) {
				raw += file.readLine() + "\n";
			}
			file.close();
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
			return null;
		}
		
		String[] lines = raw.split("\n");
		float[][] data = new float[lines.length][];
		for (int i = 0; i < lines.length; i++) {
			String[] line = lines[i].split(",");
			data[i] = new float[line.length];
			for (int j = 0; j < line.length; j++) {
				data[i][j] = Float.parseFloat(line[j]);
			}
		}
		
		return data;
	}
	
	public static void float_to_csv(String filename, float[][] data) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filename), "utf-8"));
			
			//	Clear the file
			writer.write("");
			for (int i = 0; i < data.length; i++){
				
				//	Dirty implode (Java so lame)
				String line = "";
				for (int j = 0; j < data[i].length; j++) {
					line += data[i][j] + ",";
				}
				writer.append(line.substring(0, line.length() - 1));
				if (i < data.length - 1) {
					writer.append("\n");
				}
			}
		}
		catch (IOException ex) {} 
		finally {
			try {
				writer.close();
			} 
			catch (Exception ex) {}
		}
	}
}
