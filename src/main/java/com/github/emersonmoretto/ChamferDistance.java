package com.github.emersonmoretto;
/**
 * Chamfer distance
 * 
 * @author Code by Xavier Philippeau <br> Kernels by Verwer, Borgefors and Thiel 
 */
public class ChamferDistance  {
	
	public final static int[][] cheessboard = new int[][] {
		new int[] {1,0,1},
		new int[] {1,1,1}
	};
 
	public final static int[][] chamfer3 = new int[][] {
		new int[] {1,0,3},
		new int[] {1,1,4}
	};
 
	public final static int[][] chamfer5 = new int[][] {
		new int[] {1,0,5},
		new int[] {1,1,7},
		new int[] {2,1,11}
	};
	
	public final static int[][] chamfer7 = new int[][] {
		new int[] {1,0,14},
		new int[] {1,1,20},
		new int[] {2,1,31},
		new int[] {3,1,44}
	};
	
	public final static int[][] chamfer13 = new int[][] {
		new int[] { 1,  0,  68},
		new int[] { 1,  1,  96},
		new int[] { 2,  1, 152},
		new int[] { 3,  1, 215},
		new int[] { 3,  2, 245},
		new int[] { 4,  1, 280},
		new int[] { 4,  3, 340},
		new int[] { 5,  1, 346},
		new int[] { 6,  1, 413}
	};
	
	private int[][] chamfer = null; 
	private int normalizer = 0; 
	
	private int width=0,height=0;
	
	public ChamferDistance() {
		this(ChamferDistance.chamfer3);
	}
 
	public ChamferDistance(int[][] chamfermask) {
		this.chamfer = chamfermask;
		this.normalizer = this.chamfer[0][2];
	}
 
	private void testAndSet(double[][] output, int x, int y, double newvalue) {
		if(x<0 || x>=this.width) return;
		if(y<0 || y>=this.height) return;
		double v = output[x][y];
		if (v>=0 && v<newvalue) return;
		output[x][y] = newvalue;
	}
 
	public double[][] compute(boolean[][] input, int width, int height) {
 
		this.width = width;
		this.height = height;
		double[][] output = new double[width][height]; 
		
		// initialize distance
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
				if (  input[x][y] )
					output[x][y]=0; // inside the object -> distance=0
				else
					output[x][y]=-1; // outside the object -> to be computed
		
		// forward
		for (int y=0; y<=height-1; y++) {
			for (int x=0; x<=width-1; x++) {
				double v = output[x][y];
				if (v<0) continue;
				for(int k=0;k<chamfer.length;k++) {
					int dx = chamfer[k][0];
					int dy = chamfer[k][1];
					int dt = chamfer[k][2];
 
					testAndSet(output, x+dx, y+dy, v+dt);
					if (dy!=0) testAndSet(output, x-dx, y+dy, v+dt);
					if (dx!=dy) {
						testAndSet(output, x+dy, y+dx, v+dt);
						if (dy!=0) testAndSet(output, x-dy, y+dx, v+dt);
					}
				}
			}
		}
		
		// backward
		for (int y=height-1; y>=0; y--) {
			for (int x=width-1; x>=0; x--) {
				double v = output[x][y];
				if (v<0) continue;
				for(int k=0;k<chamfer.length;k++) {
					int dx = chamfer[k][0];
					int dy = chamfer[k][1];
					int dt = chamfer[k][2];
					
					testAndSet(output, x-dx, y-dy, v+dt);
					if (dy!=0) testAndSet(output, x+dx, y-dy, v+dt);
					if (dx!=dy) {
						testAndSet(output, x-dy, y-dx, v+dt);
						if (dy!=0) testAndSet(output, x+dy, y-dx, v+dt);
					}
				}
			}
		}
		
		// normalize
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
				output[x][y] = output[x][y]/normalizer;
		
		return output;
	}
}