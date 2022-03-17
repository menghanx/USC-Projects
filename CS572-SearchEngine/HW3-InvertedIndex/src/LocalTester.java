import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class LocalTester {

	public static void main(String[] args) {
		
		String input = "5722018101	\"The DeLorme PN-20 represents a new breed of GPS devices. ...a fantastic device,"; 
		
		
		// split docID and rest of the doc
		String[] splitedInput = input.split("\t", 2);
		// replace special character and numerals with space character
		input = splitedInput[1];
		input = input.replaceAll("[^A-Za-z]", " ").toLowerCase();
		
		String docId = splitedInput[0];
		System.out.println("docId: " + docId);
		
		StringTokenizer itr = new StringTokenizer(input);
		String previous = itr.nextToken();
		
		while(itr.hasMoreElements()) {
			String current = itr.nextToken();
			System.out.println(previous + " " + current);
			previous = current;
		}
		
		HashMap<String, Integer> hashMap = new HashMap<>();
		hashMap.put("doc1", 1);
		hashMap.put("doc2", 3);
		hashMap.put("doc3", 2);
		
		StringBuilder sb = new StringBuilder();
		for (String doc : hashMap.keySet()) {
			String entry = doc + ":" + hashMap.get(doc) + " ";
			sb.append(entry );
		}
		String output = sb.substring(0, sb.length()-1);
		
	}

}
