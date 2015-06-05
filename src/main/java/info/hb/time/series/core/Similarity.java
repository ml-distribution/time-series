package info.hb.time.series.core;

import info.hb.time.series.process.TextFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Similarity {

	private static double[][] disMatrix;

	public Similarity(int max_m, int max_n) {
		disMatrix = new double[max_m][max_n];
	}

	public double constraintDtw(double[] segmentQ, double[] segmentC, float limitQ, float limitC) {
		int m = segmentQ.length;
		int n = segmentC.length;
		Bounder bounder = new Bounder(m, n, limitQ, limitC);
		int bSize = (int) bounder.gradient + 1;
		System.out.println(bSize);
		for (int j = 0; j < n; j++) {
			if (j == n - 1)
				System.out.println(j);
			int lower = bounder.lower(j);
			int upper = bounder.upper(j);
			if (lower > 0) {
				for (int lowerWall = bSize; lowerWall > 0 && lower > lowerWall && lower - lowerWall < m - 1; lowerWall--)
					disMatrix[lower - lowerWall][j] = Double.MAX_VALUE;
			}
			if (upper < m)
				for (int upperWall = 0; upperWall < bSize && upper + upperWall < m; upperWall++)
					disMatrix[upper + upperWall][j] = Double.MAX_VALUE;
			for (int i = lower; i < upper; i++) {
				double minus = segmentQ[i] - segmentC[j];
				double distance = minus * minus;
				if (i == 0 && j == 0)
					disMatrix[i][j] = distance;
				else if (i == 0)
					disMatrix[i][j] = disMatrix[i][j - 1] + distance;
				else if (j == 0)
					disMatrix[i][j] = disMatrix[i - 1][j] + distance;
				else {
					int minIndex = getMinIndex(disMatrix[i][j - 1], disMatrix[i - 1][j - 1], disMatrix[i - 1][j]);
					switch (minIndex) {
					case 0:
						disMatrix[i][j] = disMatrix[i][j - 1] + distance;
						break;
					case 1:
						disMatrix[i][j] = disMatrix[i - 1][j - 1] + distance;
						break;
					default:
						disMatrix[i][j] = disMatrix[i - 1][j] + distance;
						break;
					}
				}
			}
		}
		if (m > n)
			return disMatrix[m - 1][n - 1];
		return trimedDtw(m, n);
	}

	public double constraintDtw(double[] segmentQ, double[] segmentC, float limit) {
		int m = segmentQ.length;
		int n = segmentC.length;
		Bounder bounder = new Bounder(m, n, limit);
		for (int j = 0; j < n; j++) {
			int lower = bounder.lower(j);
			int upper = bounder.upper(j);
			if (lower > 0)
				disMatrix[lower - 1][j] = Double.MAX_VALUE;
			if (upper < m)
				disMatrix[upper][j] = Double.MAX_VALUE;
			for (int i = lower; i < upper; i++) {
				double minus = segmentQ[i] - segmentC[j];
				double distance = minus * minus;
				if (i == 0 && j == 0)
					disMatrix[i][j] = distance;
				else if (i == 0)
					disMatrix[i][j] = disMatrix[i][j - 1] + distance;
				else if (j == 0)
					disMatrix[i][j] = disMatrix[i - 1][j] + distance;
				else {
					int minIndex = getMinIndex(disMatrix[i][j - 1], disMatrix[i - 1][j - 1], disMatrix[i - 1][j]);
					switch (minIndex) {
					case 0:
						disMatrix[i][j] = disMatrix[i][j - 1] + distance;
						break;
					case 1:
						disMatrix[i][j] = disMatrix[i - 1][j - 1] + distance;
						break;
					default:
						disMatrix[i][j] = disMatrix[i - 1][j] + distance;
						break;
					}
				}
			}
		}
		if (m > n)
			return disMatrix[m - 1][n - 1];
		return trimedDtw(m, n);
	}

	private double trimedDtw(int lenQ, int lenC) {
		List<Integer> path = new ArrayList<Integer>();
		int i = lenQ - 1;
		int j = lenC - 1;
		int upI = 0, upJ = 0;
		while ((i > 0 && j == lenC - 1) || (j > 0 && i == lenQ - 1)) {
			upI = i;
			upJ = j;
			int minIndex = getMinIndex(disMatrix[i][j - 1], disMatrix[i - 1][j - 1], disMatrix[i - 1][j]);
			path.add(minIndex);
			switch (minIndex) {
			case 0:
				j--;
				break;
			case 1:
				i--;
				j--;
				break;
			default:
				i--;
				break;
			}

		}
		while (i > 0 && j > 0) {
			int minIndex = getMinIndex(disMatrix[i][j - 1], disMatrix[i - 1][j - 1], disMatrix[i - 1][j]);
			path.add(minIndex);
			switch (minIndex) {
			case 0:
				j--;
				break;
			case 1:
				i--;
				j--;
				break;
			default:
				i--;
				break;
			}
		}
		//		int a = upI, b = upJ;
		for (int p = path.size() - 1; p >= 0; p--) {
			// System.out.print(path.get(p));
			// switch (path.get(p)) {
			// case 2:
			// System.out.println(a + "\t" + (b - 1));
			// b--;
			// break;
			// case 1:
			// System.out.println((a - 1) + "\t" + (b - 1));
			// b--;
			// a--;
			//
			// break;
			// default:
			// System.out.println((a - 1) + "\t" + (b));
			// a--;
			// break;
			// }
		}
		// System.out.println(upI + " " + upJ + " " + i + " " + j);
		return disMatrix[upI][upJ] - disMatrix[i][j];
	}

	private int getMinIndex(double... data) {
		int index = 0;
		for (int i = 1; i < data.length; i++)
			if (data[i] < data[index])
				index = i;
		return index;
	}

	private class Bounder {

		public double gradient;
		double interceptC;
		double interceptQ;
		private int lenQ;

		Bounder(int lenQ, int lenC, float limit) {
			this.lenQ = lenQ;
			this.gradient = 1;
			if (lenQ > lenC) {
				interceptC = lenC * limit;
				interceptC = interceptC > 1 ? interceptC : 2;
				interceptQ = lenQ - lenC + interceptC;
			} else {
				interceptQ = lenQ * limit;
				interceptQ = interceptQ > 1 ? interceptQ : 2;
				interceptC = lenC - lenQ + interceptQ;
			}

		}

		Bounder(int lenQ, int lenC, float limitQ, float limitC) {
			this.lenQ = lenQ;
			interceptC = lenC * limitQ;
			interceptC = interceptC > 1 ? interceptC : 2;
			interceptQ = lenQ * limitC;
			interceptQ = interceptQ > 1 ? interceptQ : 2;
			this.gradient = (lenQ - interceptQ) / (lenC - interceptC);
			System.out.println(gradient + " " + interceptC + " " + interceptQ);
		}

		public int lower(int indexC) {
			int low = (int) (gradient * indexC - interceptC);
			low = low > 0 ? low : 0;
			low = low < lenQ - 1 ? low : lenQ - 1;
			return low;
		}

		public int upper(int indexC) {
			int up = (int) (gradient * indexC + interceptQ);
			return up < lenQ ? up : lenQ;
		}

	}

	public static void main(String[] args) throws IOException {
		TextFile queryString = new TextFile(".\\data\\q.txt");
		double[] query = new double[queryString.size()];
		int i = 0;
		for (String item : queryString)
			query[i++] = Double.valueOf(item);
		TextFile candidertString = new TextFile(".\\data\\c.txt");
		double[] candidert = new double[candidertString.size()];
		i = 0;
		for (String item : candidertString)
			candidert[i++] = Double.valueOf(item);
		Similarity s = new Similarity(500, 500);
		double dis = s.constraintDtw(query, candidert, 0.1f);
		System.out.println(dis);
		TextFile.write("disMatrix.txt", disMatrix);
	}

}