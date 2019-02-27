package org.processmining.interactivefiltering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IQR {

	
	// the arraylist
	private ArrayList<Double> nums;

	// the sample size
	private int sample_size = 0;

	// the median
	private double median;

	// the 25 and 75 percentiles
	private double lq;
	private int uq;

	// the outliers
	private double uo;
	private double lo;

	// the outlier multiplier (set at 1.5 initially)
	private double omult = 1.5;

	public  IQR() {
		// TODO empty constructor
	}

	public  IQR(double[] indoubles) {
		// TODO constructor that takes in a primitive array, creates an array
		// list, and sets the sample size
		nums = new ArrayList<Double>();

		for (double d : indoubles) {
			nums.add(d);
		}

		sample_size = nums.size();

	}

	public  IQR(ArrayList<Double> innums) {
		// TODO Constructor that takes in a list and sets the sample size
		nums = innums;
		sample_size = nums.size();
	}

	public  void setNums(ArrayList<Double> innums) {
		// TODO set the nums array from an arrayList
		nums = innums;
		sample_size = nums.size();
	}

	public  void setomult(Double inom) {
		// TODO get the omult
		omult = inom;
	}

	public  ArrayList<Double> getNums() {
		// TODO return nums list
		return nums;
	}

	public  int getSample_size() {
		// TODO return sample size
		return sample_size;
	}

	public  double getMedian() {
		// TODO return the median
		return median;
	}

	public  double getLq() {
		// TODO get the lower quartile
		return lq;
	}

	public  double getUq() {
		// tOOD get the upper quartile
		return uq;
	}

	public  double getLo() {
		// TODO return the lower outlier
		return lo;
	}

	public  double getUo() {
		// TODO get the upper outlier
		return uo;
	}

	public  double getOmult() {
		// TODO return the omultiplier
		return omult;
	}

	public  void getFromInt(ArrayList<Integer> innums) {
		// TODO create a double array from an integer array

		if (nums == null) {
			nums = new ArrayList<Double>();
		} else {
			nums.clear();
		}

		for (int tonum : innums) {
			nums.add((tonum * 1.0));
		}
	}
/*
	public  void quickSort() {
		// TODO call the sort classes quicksort method
		QuickSort<Double> qs = new QuickSort<Double>(nums);
		qs.sort();
		nums = qs.getData();
	}

	public  void treeSort() {
		// TODO call the sort classes binary treesort method
		TreeSort<Double> ts = new TreeSort<Double>(nums);
		ts.sort();
		nums = ts.getSorted();
	}
*/
	public  void MergeSort() {
		// TODO call the merge sort class
		MergeSort<Double> ms = new MergeSort<Double>(nums);
		nums = ms.getData();
	}

	public  void getTrimmedMean(double trimp) {
		// TODO if IQR fails, this allows for getting a trimmed mean without
		// calling a new object as well as to perform comparisons

		// the trimmed mean
		double mean = 0;

		// the standard deviation used about the trimmed mean
		double sd = 0;

		for (int i = 0; i < nums.size(); i++) {
			if (i > ((int) Math.floor(nums.size() * trimp))
					& i < ((int) Math.ceil(nums.size() * (1 - trimp)))) {
				mean += nums.get(i);
			}
		}

		mean = mean / (nums.size() - (nums.size() * trimp));

		// calculate the sd using the trimmed mean
		for (int i = 0; i < nums.size(); i++) {
			if (i > ((int) Math.floor(nums.size() * trimp))
					& i < ((int) Math.ceil(nums.size() * .875))) {
				sd += ((nums.get(i) - mean) / i);
			}
		}

		// get the lo and uo based on the z-score( anything more than 2.5
		// standard deviations away is discarded
		lo = 1000000;
		uo = 0;

		for (int i = 0; i < nums.size(); i++) {
			if (i > ((int) Math.floor(nums.size() * trimp))
					& i < ((int) Math.ceil(nums.size() * (1 - trimp)))) {
				if (((nums.get(i) - mean) / sd) < 2.5 & nums.get(i) > uo) {
					uo = nums.get(i);
				}

				if (((nums.get(i) - mean) / sd) > -2.5 & nums.get(i) < lo) {
					lo = nums.get(i);
				}
			}
		}
	}

	public  void pullstats() {
		// TODO pull the statistics

		MergeSort();

		// the median pos
		// the quartile pos are always found from evens so two numbers are taken
		// later
		int mpos = 0;

		// reset basic stats
		lq = 0;
		uq = 0;
		median = 0;
		uo = 0;
		lo = 0;

		// the boolean determining whether there are an even number of elements
		Boolean evenm = false;

		// set the quartile positions
		if ((nums.size() % 2) == 0) {
			mpos = (int) Math.floor(nums.size() / 2);
			evenm = true;
		} else {
			mpos = (nums.size() + 1) / 2;
			mpos = mpos - 1;
		}

		// get the median position
		if (mpos > 0) {
			// lower quartile
			lq += (nums.get((int) Math.floor(mpos / 2)) + nums.get((int) Math
					.ceil(mpos / 2))) / 2;

			if (evenm == false) {
				// median of an odd set
				median = nums.get(mpos);
			} else if (evenm == true) {
				// median of an even set
				median += (nums.get(mpos) + nums.get(mpos + 1)) / 2;
			}

			// upper quartile
			uq += (nums.get(((int) (mpos + Math.ceil(mpos / 2)))) + nums
					.get(((int) (mpos + Math.floor(mpos / 2))))) / 2;

			uo = median + ((uq - lq) * omult);
			lo = median - ((uq - lq) * omult);
		}

	}
	
}
class MergeSort<E extends Comparable> {
	private ArrayList<E> data = new ArrayList<E>();

	public  MergeSort(ArrayList<E> keys) {
		List<E> list = new ArrayList<E>();

		for (E k : keys) {
			list.add(k);
		}

		Collections.sort(list);

		for (Object d : list) {
			data.add((E) d);
		}
	}

	public  void setData(ArrayList<E> inout) {
		data = inout;
	}

	public  ArrayList<E> getData() {
		return data;
	}
}
