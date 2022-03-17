import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
//import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InvertedIndexBigrams {

	public static class TokenizerMapper
	extends Mapper<Object, Text, Text, Text>{

		private Text word = new Text();
		private Text doc = new Text();

		public void map(Object key, Text value, Context context
				) throws IOException, InterruptedException {

			String[] splitedInput = value.toString().split("\t", 2);
			// replace special character and numerals with space character
			String input = splitedInput[1];
			input = input.replaceAll("[^A-Za-z]", " ").toLowerCase();

			String docId = splitedInput[0];
			doc.set(docId);

			StringTokenizer itr = new StringTokenizer(input);
			String previous = itr.nextToken();
			
			while (itr.hasMoreTokens()) {
				String current = itr.nextToken();
				word.set(previous + " " + current);
				previous = current;
				context.write(word, doc);
			}
		}
	}
	
	public static class IntSumReducer
	extends Reducer<Text,Text,Text,Text> {

		private HashMap<String, Integer> hashMap = new HashMap<>();

		public void reduce(Text key, Iterable<Text> values,
				Context context
				) throws IOException, InterruptedException {
			
			for (Text val : values) {
				if (hashMap.containsKey(val.toString())) {
					hashMap.put(val.toString(), hashMap.get(val.toString()) + 1);
				}else {
					hashMap.put(val.toString(), 1);
				}
			}

			StringBuilder sb = new StringBuilder();
			for (String doc : hashMap.keySet()) {
				String entry = doc + ":" + hashMap.get(doc) + " ";
				sb.append(entry );
			}
			String output = sb.substring(0, sb.length()-1);
			context.write(key, new Text(output));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf, "word count");
		job.setJarByClass(InvertedIndexBigrams.class);
		job.setMapperClass(TokenizerMapper.class);
		
		//job.setCombinerClass(IntSumReducer.class);
		
		job.setReducerClass(IntSumReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}