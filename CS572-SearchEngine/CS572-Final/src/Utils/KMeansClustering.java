package Utils;

import java.util.ArrayList;
import java.util.List;

public class KMeansClustering {

	public static void main(String[] args) {
		
		double[][] cluster1 = { 
				{1, 1}, {3.6, 2},{2.6, 1.8}	
		};
		
		double[][] cluster2 = { 
				{7, 7},{6.4, 6.6},{5.5, 4.0},{6.5, 5.0}
		};
		
		Data centroid1 = new Data(findCentroid(cluster1));
		Data centroid2 = new Data(findCentroid(cluster2));
		System.out.println("Centroid 1:" + centroid1);
		System.out.println("Centroid 2:" + centroid2);
		
		Data candidate = new Data(new double[]
				{2.6, 1.8});
		System.out.println("candidate:" + candidate);
		
		System.out.println("ED to " + centroid1.toString() + " " + ComputeEuclideanDistance(centroid1, candidate));
		System.out.println("ED to " + centroid2.toString() + " " + ComputeEuclideanDistance(centroid2, candidate));
		
		
	}
	
	static class Data {
		List<Double> values = new ArrayList<>();
		
		public Data(double[] vals) {
			for (double v : vals) {
				this.values.add(v);
			}
		}
		
		public Data(Data copy) {
			for (Double d : copy.values) {
				this.values.add(d);
			}
		}
		
		public String toString() {
			String out = "( ";
			
			for (Double d : this.values) {
				out += d+", ";
			}
			
			int pos = out.lastIndexOf(", ");
			out = out.substring(0, pos);
			
			return out + " )";
		}
	}
	
	private static double ComputeEuclideanDistance(Data data1, Data data2) {
		if (data1.values.size() != data2.values.size()) {
			return -1;
		}
		
		int size = data1.values.size();
		double sum = 0;
		
		for (int i = 0; i < size; i++) {
			sum += Math.pow((data1.values.get(i) - data2.values.get(i)), 2);
		}
		
		return round(Math.sqrt(sum), 1);
	}
	
	private static double round (double value, int precision) {
	    int scale = (int) Math.pow(10, precision);
	    return (double) Math.round(value * scale) / scale;
	}
	
	
	private static double[] findCentroid(double[][] cluster) {
		double[] centroid = new double[cluster[0].length];
		
		for (double[] point : cluster) {
			for (int i = 0; i<cluster[0].length; i++) {
				centroid[i] += point[i];
			}
		}
		
		for (int i = 0; i<cluster[0].length; i++) {
			centroid[i] = round(centroid[i]/cluster.length, 1);
		}
		
		return centroid;
	}
}

