package info.hb.time.series.core;

import info.hb.time.series.process.TextFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Classify {

	public Map<String, List<double[]>> formatTestData(List<String[]> testStringData) {
		String deviceId = testStringData.get(0)[0];
		Map<String, List<double[]>> testData = new LinkedHashMap<String, List<double[]>>();
		List<double[]> oneDivice = new ArrayList<double[]>();
		for (String[] line : testStringData) {
			if (!line[0].equals(deviceId)) {
				deviceId = line[0];
				oneDivice = new ArrayList<double[]>();
			}
			double[] doubleLine = new double[line.length - 1];
			for (int i = 1; i < line.length; i++) {
				doubleLine[i - 1] = Double.valueOf(line[i]);
			}
			if (!testData.containsKey(deviceId)) {
				testData.put(deviceId, oneDivice);
			}
			oneDivice.add(doubleLine);
		}
		return testData;
	}

	public List<double[]> str2double(List<String[]> trianStringData) {
		List<double[]> doubleData = new ArrayList<double[]>();
		for (String[] line : trianStringData) {
			double[] doubleLine = new double[line.length];
			int i = 0;
			for (String item : line)
				doubleLine[i++] = Double.valueOf(item);
			doubleData.add(doubleLine);
		}
		return doubleData;
	}

	public void classifyInMemory(String trianPath, String testFileName, int k, String writeFileName) throws IOException {
		final int max_m = 301;
		final int max_n = 501;
		Similarity similar = new Similarity(max_m, max_n);
		List<String[]> testStringData = TextFile.readAsMatrix(testFileName, ",");
		Map<String, List<double[]>> testData = formatTestData(testStringData);
		testStringData.clear();
		PrintWriter out = new PrintWriter(new File(writeFileName).getAbsoluteFile());

		File dir = new File(trianPath);
		Map<String, List<double[]>> trianData = new HashMap<String, List<double[]>>();
		for (File trianFile : dir.listFiles()) {
			List<String[]> trianStringData = TextFile.readAsMatrix(trianFile.getAbsolutePath(), ",");
			List<double[]> trainDoubleData = str2double(trianStringData);
			trianStringData.clear();
			String name = trianFile.getName().split("\\.")[0];
			trianData.put(name, trainDoubleData);
		}
		System.out.println("calssify...");
		for (Map.Entry<String, List<double[]>> testPerDevice : testData.entrySet()) {
			int segmentId = 0;
			for (double[] segmentQ : testPerDevice.getValue()) {
				double[] minDistances = new double[k];
				String[] minLables = new String[k];
				int[] trianSegIds = new int[k];
				for (int i = 0; i < k; i++) {
					minDistances[i] = Double.MAX_VALUE;
					minLables[i] = "Unkown";
					trianSegIds[i] = 1;
				}
				for (Map.Entry<String, List<double[]>> trainPerDevice : trianData.entrySet()) {
					int trianSegId = 0;
					for (double[] segmentC : trainPerDevice.getValue()) {
						if (segmentC.length > max_n) {
							double[] segmentTemp = new double[max_n];
							for (int i = 0; i < max_n; i++)
								segmentTemp[i] = segmentC[i];
							segmentC = segmentTemp;
						}
						double distance = similar.constraintDtw(segmentQ, segmentC, 0.1f);
						motifyKnear(minDistances, minLables, trianSegIds, distance, trainPerDevice.getKey(),
								trianSegId++);
					}
				}
				out.print(testPerDevice.getKey() + "$" + segmentId++);
				System.out.print(testPerDevice.getKey() + ":");
				for (int i = 0; i < k; i++) {
					System.out.print(minLables[i] + "$" + trianSegIds[i] + "\t");
					out.print(",");
					out.print(minLables[i]);
					out.print(",");
					out.print(minDistances[i]);
				}
				System.out.println();
				out.println();
			}
		}
		out.flush();
		out.close();
	}

	private void motifyKnear(double[] minDistances, String[] minLables, int[] trianIds, double distance,
			String deviceId, int trianSegId) {
		int len = minDistances.length;
		boolean in = false;
		for (int i = 0; i < len; i++) {
			if (distance < minDistances[i]) {
				minDistances[i] = distance;
				minLables[i] = deviceId;
				trianIds[i] = trianSegId;
				in = true;
				break;
			}
		}
		if (in == false)
			return;
		for (int i = 1; i < len; i++) {
			if (minDistances[i] > minDistances[i - 1]) {
				double sentry = minDistances[i];
				String id = minLables[i];
				int segId = trianIds[i];
				int j;
				for (j = i - 1; j >= 0 && sentry > minDistances[j]; j--) {
					minDistances[j + 1] = minDistances[j];
					minLables[j + 1] = minLables[j];
					trianIds[j + 1] = trianIds[j];
				}
				minDistances[j + 1] = sentry;
				minLables[j + 1] = id;
				trianIds[j + 1] = segId;
			}
		}
	}

	public void classify(String writeFileName) throws IOException {
		final int max_m = 301;
		final int max_n = 501;
		Similarity similar = new Similarity(max_m, max_n);
		List<String[]> testStringData = TextFile.readAsMatrix(".\\data\\delta_segment_test.csv", ",");
		Map<String, List<double[]>> testData = formatTestData(testStringData);
		testStringData.clear();
		PrintWriter out = new PrintWriter(new File(writeFileName).getAbsoluteFile());
		File dir = new File(".\\data\\train_cos_avg");
		for (File trianFile : dir.listFiles()) {
			System.out.println(trianFile.getName());
			List<String[]> trianStringData = TextFile.readAsMatrix(trianFile.getAbsolutePath(), ",");
			List<double[]> trainData = str2double(trianStringData);
			for (Map.Entry<String, List<double[]>> dataPerDevice : testData.entrySet()) {
				int segmentId = 0;
				for (double[] segmentQ : dataPerDevice.getValue()) {
					out.print(dataPerDevice.getKey() + "$" + segmentId++);
					double min = Double.MAX_VALUE;
					int minLable = 0;
					int k = 0;
					int lenQ = 0, lenC = 0;
					for (double[] segmentC : trainData) {
						k++;
						if (segmentC.length > max_n) {
							double[] segmentTemp = new double[max_n];
							for (int i = 0; i < max_n; i++)
								segmentTemp[i] = segmentC[i];
							segmentC = segmentTemp;
						}
						double distance = similar.constraintDtw(segmentQ, segmentC, 0.1f);
						if (distance < min) {
							min = distance;
							minLable = k;
							lenQ = segmentQ.length;
							lenC = segmentC.length;
						}
					}

					String trianDeviceId = trianFile.getName().split("\\.")[0];
					out.print("," + trianDeviceId);
					out.print("," + min);
					out.print("," + minLable);
					out.print("," + lenQ);
					out.print("," + lenC);
					out.print(",");
				}
				out.println();
			}
		}
		out.close();
	}

	public static void main(String[] args) throws IOException {
		// String s = "7.csv";
		// String[] t = s.split("\\.");
		Classify classifier = new Classify();
		// classifier.classify("data\\distance6.csv");
		classifier.classifyInMemory("data/train_cos_avg", "data/test.csv", 25, "data/kNear2.csv");
	}

}
