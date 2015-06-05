package info.hb.time.series.core;

import java.util.Arrays;
import java.util.Random;

public class Item {

	int x, y;

	public Item(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void increment() {
		x++;
		y++;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Item && (((Item) o).x == x && ((Item) o).y == y);
	}

	public static void main(String[] args) {
		int m = 10;
		int nums = 5;
		int[] data = new int[m];
		Random r = new Random();
		for (int i = 0; i < data.length; i++) {
			data[i] = r.nextInt(nums);
		}
		System.out.println(Arrays.toString(data));
		int[] counts = new int[nums];
		int[] from = new int[nums];
		int[] to = new int[nums];
		for (int i = 0; i < nums; i++) {
			counts[i] = r.nextInt();
			from[i] = r.nextInt();
			to[i] = r.nextInt();
		}
		int top = 0;
		for (int j = 0; j < data.length; j++) {
			int i = data[j];
			if (!(from[i] < top && from[i] >= 0 && to[from[i]] == i)) {
				from[i] = top;
				to[top] = i;
				top++;
				counts[i] = 0;
			}
			counts[i] += 1;
			System.out.println(Arrays.toString(counts));
		}
		System.out.println(Arrays.toString(counts));
		System.out.println(Arrays.toString(data));
	}

}
