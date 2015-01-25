package ARodCluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ARodCluster{
	
	public static void main(String[] args){}
	
	public ARC_Return ARodClustering(ARC_Input ARC_In)
	{
		// Parameters setup
		int n = ARC_In.X.length;
		int m = ARC_In.X[0].length;
		ARC_Return ARC_Re = new ARC_Return();
		ARC_Re.Label = new int[n];
		ARC_Re.ClusterCenter = new int[n];
		ARC_Re.Outlier = new Boolean[n];
		ARC_Re.Map = new double[2][n];
		
		try{
			// Input parameters check
			if (ARC_In.nargin < 2) {
				System.out.println("Not enough input parameters!");
			} else if (ARC_In.nargin < 3) {
				ARC_In.N = 3;
				ARC_In.Method = "standard";
			} else if (ARC_In.nargin < 4) {
				ARC_In.Method = "standard";
			}
			
			// Distance calculation
			double[][] Distance = distance_calculator(ARC_In.X, n, m);
			
			// Density calculation
			double[] Density = density_calculator(Distance, ARC_In.d_c, ARC_In.Method);
			int[] density_order = density_sort(Density); 
			
			// Minimum distance calculation
			double_int_array delta_pointer = delta_calculator(Distance, Density);
			double[] Delta = delta_pointer.A;
			int[] Pointer = delta_pointer.B;
			
			// Find clustering center
			ARC_Re.ClusterCenter = clustercenter_finder(Density, Delta, ARC_In.N);
			
			// Find label
			ARC_Re.Label = label_finder(density_order, ARC_Re.ClusterCenter, Pointer);
			
			// Find outlier
			ARC_Re.Outlier = outlier_finder(Density, Delta, ARC_In.N, density_order, ARC_Re.ClusterCenter, Pointer);

		} catch (NumberFormatException e) {
			System.out.println("Input invalid");
		}
		
		return ARC_Re;
	}
	
	private int[] density_sort(double[] Density) {
		//sort the array into a new array
		 double[] x = Density;
		 Arrays.sort(x); //sort ascending

		 //final array of indexes
		 int index_array[] = new int[Density.length];

		 //Iterate on x array
		 for(int i=0; i<Density.length; i++)
		    index_array[i] = Arrays.binarySearch(Density, x[i]);
		return index_array;
	}

	public double[][] distance_calculator(double[][] X, int n, int m)
	{
		double[][] Distance = new double[n][n];
		for (int i = 1; i < n; i++)
		{
			for (int j = 0; j < i; j++)
			{
				double temp = 0;
				for (int k = 0; k < m; k++)
				{
					temp += Math.pow(X[i][k]-X[j][k], 2);
				}
				Distance[i][j] = Math.sqrt(temp);
				Distance[j][i] = Math.sqrt(temp);
			}
		}
		return Distance;
	}
	
	public double[] density_calculator(double[][] Distance, double d_c, String Method)
	{
		int n = Distance.length;
		double[] Density = new double[n];
		if (Method.equals("kernel")) {
			for (int i = 0; i < n; i++)
			{
				for (int j = 0; j < n; j++)
				{
					Density[i] += Math.exp(-Math.pow(Distance[i][j], 2)/(2*Math.pow(d_c, 2)));
				}
				Density[i] -= 1;
				Density[i] /= n*d_c*Math.sqrt(2*3.1415926);
			}
		} else {
			for (int i = 0; i < n; i++)
				for (int j = 0; j < n; j++)
					if(Distance[i][j] < d_c)
						Density[i]++;
		}
		return Density;
	}

	private double_int_array delta_calculator(double[][] Distance, double[] Density)
	{
		double_int_array  delta_pointer = new double_int_array();
		int n = Density.length;
		double[] Delta = new double[n];
		int[] Pointer = new int[n];
	 
		for(int i = 0; i < n; i++)
		{
			double dist = 0.0;
			int pter = 0;
			boolean flag = false;
			for(int j = 0; j < n; j++)
			{
				if(i == j) continue;
				if(Density[j] > Density[i])
				{
					double tmp = Distance[i][j];
					if(!flag)
					{
						dist = tmp;
						pter = j;
						flag = true;
					} else {
						if (tmp < dist)
						{
							dist = tmp;
							pter = j;
						}
					}
				}
			}
			if(!flag)
			{
				for(int j = 0; j < n; j++)
				{
					double tmp = Distance[i][j];
					if (tmp < dist)
					{
						dist = tmp;
						pter = j;
					}
				}
			}
			Delta[i] = dist;
			Pointer[i] = pter;
		}
		delta_pointer.A = Delta;
		delta_pointer.B = Pointer;
		return delta_pointer;
	}

	private int[] clustercenter_finder(double[] Density, double[] Delta, double N) {
		List<Integer> CenterList = new ArrayList<Integer>();
		double B = N*(double_max(Delta)+double_mean(Delta))*0.4;
		double C = (double_max(Delta)+double_mean(Delta))*0.2;
		for (int i = 0; i < Density.length; i++)
		{
			if (Density[i]>=N && Density[i]*Delta[i]>=B && Delta[i]>C)
			{
				CenterList.add(i);
			}
		}
		int[] ClusterCenter = new int[CenterList.size()];
		for (int i = 0; i < CenterList.size(); i++)
			ClusterCenter[i] = CenterList.get(i);
		return ClusterCenter;
	}

	private double double_max(double[] Delta) {
		double tmp = 0.0;
		for (int i = 0; i < Delta.length; i++)
			tmp = tmp > Delta[i] ? tmp : Delta[i];
		return tmp;
	}

	private double double_mean(double[] Delta) {
		double tmp = 0.0;
		for (int i = 0; i < Delta.length; i++)
			tmp += Delta[i];
		return tmp/Delta.length;
	}
	
	private int[] label_finder(int[] density_order, int[] ClusterCenter, int[] Pointer) {
		int[] Label = new int[Pointer.length];
		int count = 0;
		for (int i = 0; i < Pointer.length; i++)
		{
			if (Arrays.asList(ClusterCenter).contains(density_order[i]))
			{
				count++;
				Label[density_order[i]] = count;
			} else {
				Label[density_order[i]] = Label[Pointer[density_order[i]]];
			}
		}
		return Label;
	}

	private Boolean[] outlier_finder(double[] Density, double[] Delta, double N, int[] density_order, int[] ClusterCenter, int[] Pointer) {
		Boolean[] Outlier = new Boolean[Delta.length];
		double C = (double_max(Delta)+double_mean(Delta))*0.2;
		for (int i = 0; i < Density.length; i++)
		{
			if (Density[i]<N && Delta[i]>C)
			{
				Outlier[i] = true;
			} else {
				Outlier[i] = false;
			}
		}
		for (int i = 0; i < Pointer.length; i++)
		{
			if (Outlier[Pointer[density_order[i]]])
			{
				Outlier[density_order[i]] = true;
			}
		}
		return Outlier;
	}
}