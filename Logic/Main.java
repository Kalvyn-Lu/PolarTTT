package Logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
	
	/**
	 * Converts a csv file into a float double array
	 * @param filename The file to read from
	 * @return The resulting float double array
	 */
	public static float[][] csv_to_float(String filename) {
		System.out.println("Opening " + filename);
		BufferedReader file = null;
		String raw = "";
		try {
			file = new BufferedReader(new FileReader(filename));
			while (file.ready()) {
				String line = file.readLine();
				if (line.length() < 1) {
					continue;
				}
				raw += line + "\n";
			}
			file.close();
		}
		catch (IOException e) {
			return null;
		}
		
		System.out.println("Parsing data from " + filename);
		String[] lines = raw.substring(0,raw.length()-2).split("\n");
		float[][] data = new float[lines.length][];
		for (int i = 0; i < lines.length; i++) {
			if (i % 500 == 0) {
				Main.sout("Parsed", i);
			}
			String[] line = lines[i].split(",");
			data[i] = new float[line.length];
			for (int j = 0; j < line.length; j++) {
				data[i][j] = Float.parseFloat(line[j]);
			}
		}
		System.out.println("Done!\n");
		return data;
	}

	/**
	 * Stores a float double array into a csv file
	 * @param filename The name of the destination file
	 * @param data The data to store
	 * @param append Whether to add to the existing file or just overwrite it
	 */
	public static void float_to_csv(String filename, float[][] data, boolean append) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filename, append));
			
			for (int i = 0; i < data.length; i++){
				
				//	Dirty implode (Java so lame)
				String line = "";
				for (int j = 0; j < data[i].length; j++) {
					line += data[i][j] + ",";
				}
				writer.append(line.substring(0, line.length() - 1));
				writer.append("\n");
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
			return;
		} 
		finally {
			try {
				writer.close();
			} 
			catch (Exception ex) {}
		}
	}

	/**
	 * Stores an int double array into a csv file
	 * @param filename The name of the destination file
	 * @param data The data to store
	 * @param append Whether to add to the existing file or just overwrite it
	 */
	public static void int_to_csv(String filename, int[][] data, boolean append) {
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filename, append));
			
			for (int i = 0; i < data.length; i++){
				
				String line = "";
				for (int j = 0; j < data[i].length; j++) {
					line += data[i][j] + ",";
				}
				writer.append(line.substring(0, line.length() - 1));
				writer.append("\n");
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
			System.exit(0);
			return;
		} 
		finally {
			try {
				writer.close();
			} 
			catch (Exception ex) {}
		}
	}
}
