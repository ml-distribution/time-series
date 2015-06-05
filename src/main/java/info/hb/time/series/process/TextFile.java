package info.hb.time.series.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class TextFile extends ArrayList<String> {

	private static final long serialVersionUID = -6155297033096109815L;

	public static String read(String fileName) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader in = new BufferedReader(new FileReader(new File(fileName).getAbsoluteFile()));
		try {
			String s;
			while ((s = in.readLine()) != null) {
				sb.append(s);
				sb.append("\n");
			}
		} finally {
			in.close();
		}
		return sb.toString();
	}

	public static void write3(String fileName, String text) throws IOException {
		PrintWriter out = new PrintWriter(new File(fileName).getAbsoluteFile());
		try {
			out.print(text);
		} finally {
			out.close();
		}
	}

	public static void write(String fileName, double[][] disMatrix) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new File(fileName).getAbsoluteFile());
		try {
			for (double[] line : disMatrix) {
				for (double item : line) {
					out.print(item);
					out.print(",");
				}
				out.println();
			}
		} finally {
			out.close();
		}
	}

	public static List<String[]> readAsMatrix(String fileName, String splitter, String splitterInLine)
			throws IOException {
		String[] lines = read(fileName).split(splitter);
		List<String[]> matrix = new ArrayList<String[]>();
		for (String line : lines) {
			if (line.length() > 0) {
				if (line.lastIndexOf(splitterInLine) == line.length() - 1)
					line = line.substring(0, line.length() - 1);
				matrix.add(line.split(splitterInLine));
			}
		}
		return matrix;
	}

	public static List<String[]> readAsMatrix(String fileName, String splitter) throws IOException {
		List<String[]> matrix = new ArrayList<String[]>();
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		String line;
		while ((line = reader.readLine()) != null) {
			//			if (i++ % 10000 == 0)
			//				System.out.println(i);
			if (line.length() > 0) {
				if (line.lastIndexOf(splitter) == line.length() - 1)
					line = line.substring(0, line.length() - 1);
				matrix.add(line.split(splitter));
			}
		}
		reader.close();
		return matrix;
	}

	public TextFile(String fileName, String splitter) throws IOException {
		super(Arrays.asList(read(fileName).split(splitter)));
		if (get(0).equals(""))
			remove(0);
		if (get(size() - 1).equals(""))
			remove(size() - 1);
	}

	public TextFile(String fileName) throws IOException {
		this(fileName, "\n");
	}

	public TextFile() {

	}

	public void write(String fileName) throws FileNotFoundException {
		PrintWriter out = new PrintWriter(new File(fileName).getAbsoluteFile());
		try {
			for (String item : this) {
				out.println(item);
			}
		} finally {
			out.close();
		}
	}

	public static void main(String[] args) throws IOException {
		String file = read("src/main/java/info/hb/time/series/process/TextFile.java");
		write3("test.txt", file);
		TextFile text = new TextFile("test.txt");
		text.write("test2.txt");
		TreeSet<String> words = new TreeSet<String>(new TextFile(
				"src/main/java/info/hb/time/series/process/TextFile.java", "\\W+"));
		System.out.println(words.headSet("F"));
	}

}
