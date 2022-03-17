package example;

import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

public class WriteData {

	public static void main(String[] args) {
		String path = "D:\\xmh91\\git\\WebCrawler\\data\\test.csv";
		writeData(path);
	}
	
	public static void writeData(String path) {
		try {
			FileWriter outputfile = new FileWriter(path);
			
			CSVWriter writer = new CSVWriter(outputfile);
			
			String[] header = { "Name", "Class", "Marks" };
	        writer.writeNext(header);
	        
	        
	        String[] data1 = { "Aman", "10", "620" };
	        writer.writeNext(data1);
	        String[] data2 = { "Suraj", "10", "630" };
	        writer.writeNext(data2);
	        
	     // closing writer connection
	        writer.close();
	        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
