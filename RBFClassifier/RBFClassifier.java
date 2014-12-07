package RBFClassifier;

import java.util.HashMap;

import Logic.Main;

public class RBFClassifier {
	
	protected RBFNetwork network;

	/**
	 * Builds a manager for a Radial-Basis Function network which will classify
	 * input into num_output possible classes.
	 * @param num_inputs The number of inputs into the network
	 * @param num_gaussian The number of hidden nodes in the network
	 * @param num_output The number of categories (the output layer size).
	 */
	public RBFClassifier(int num_inputs, int num_gaussian, int num_output, float learning_rate, float gaussian_width, String learndata) {
		network = new RBFNetwork(num_inputs, num_gaussian, num_output, learning_rate, gaussian_width);
		float[][] data = Main.csv_to_float(learndata);
		set_centers_w("data/classifier_centers.csv", data, num_gaussian);
		int x = 0;
		System.out.println("Learning from input data");
		for (float[] line : data) {
			if (x++ % 1000 == 0) {
				Main.sout("Number of lines processed", x);
			}

			learn(line, (int)line[line.length - 1]);
		}
		System.out.println("Done!\n");
		
		save_weights("data/classifier_weights.csv");
	}

	/**
	 * Builds a manager for a Radial-Basis Function network which will classify
	 * input into num_output possible classes.
	 * @param num_inputs The number of inputs into the network
	 * @param num_gaussian The number of hidden nodes in the network
	 * @param num_output The number of categories (the output layer size).
	 */
	public RBFClassifier(int num_inputs, int num_gaussian, int num_output, float learning_rate, float gaussian_width) {
		network = new RBFNetwork(num_inputs, num_gaussian, num_output, learning_rate, gaussian_width);
		set_centers_r("data/classifier_centers.csv");
		get_weights("data/classifier_weights.csv");
	}
	
	/**
	 * Classifies (tests) the input
	 * @param input The state
	 * @return The class fitting that input
	 */
	public int classify(float[] input) {
		
		//	First read from the the RBF network
		float[] class_vals = network.get_output(input);
		
		//	Find the argmax value (the most likely category)
		int best = 0;
		for (int i = 0; i < class_vals.length; i++) {
			if (class_vals[best] < class_vals[i]) {
				best = i;
				
			}
		}
		
		return best;
	}
	
	public float[] learn(float[] input, int real) {
		if (real < 0) {
			System.out.println("Failure to classify!");
			return null;
		}
		float[] out = network.get_output(input);
		
		//	Classify on {0, 1, 0} for example with 1 being set to the correct class
		float[] outcome = new float[out.length];
		for (int i = 0; i < outcome.length; i++) {
			outcome[i] = 0;
		}
		outcome[real] = 1;
		
		
		//	Learning requires "backpropagation"
		//	swapping these parameters fixed the world somehow.
		network.back_propogate(
				
				//	The actual is given based on the data
				outcome,
				
				//	The expected is what the network thinks will happen
				out);
		return out;
	}

	/**
	 * Sets the weights of the hidden layer based on the data. Also writes
	 * the data to a file to read for later.
	 * @param filename The output file
	 * @param data The experimental data
	 * @param num_gaussian
	 */
	public void set_centers_w(String filename, float[][] data, int num_gaussian) {
		System.out.println("Learning center placement from data.");
		//	make k-means clusters
		float[][] kmeans = kmeans(data, num_gaussian);
		
		System.out.println("Done! Saving to file " + filename);
		Main.float_to_csv(filename, kmeans, false);
		System.out.println("Done! Assigning clusters...");

		//	Simply these clusters to the hidden layer
		for (int i = 0; i < kmeans.length; i++) {
			network.gnodes[i].set_centers(kmeans[i]);
		}
		System.out.println("Done!\n");
	}
	
	/**
	 * Sets the weights of the hidden layer based on the data. Also writes
	 * the data to a file to read for later.
	 * @param filename The input file
	 */
	public void set_centers_r(String filename) {
		float[][] centers = Main.csv_to_float(filename);
		for (int i = 0; i < centers.length; i++) {
			network.gnodes[i].set_centers(centers[i]);
		}
	}
	
	/**
	 * Set the weights of the network's output nodes into those specified by a file
	 * @param filename The file
	 */
	public void get_weights(String filename) {
		float[][] weights = Main.csv_to_float(filename);
		for (int i = 0; i < network.onodes.length; i++) {
			network.onodes[i].weights = weights[i];
		}
	}
	
	/**
	 * Save the weights of the current network's output nodes into the file
	 * @param filename The file
	 */
	public void save_weights(String filename) {
		float[][] weights = new float[network.gnodes.length][network.onodes.length];
		for (int i = 0; i < network.onodes.length; i++) {
			weights[i] = network.onodes[i].weights;
		}
		Main.float_to_csv(filename, weights, false);
	}

	/**
	 * Applies k-means clustering on a data file into a given number of
	 * clusters. This will generate locally optimal cluster centers in
	 * a finite number of iterations.
	 * @param data The input data
	 * @param num_gaussian The number of clusters
	 * @return A list of centers for each hidden Gaussian node
	 */
	private static float[][] kmeans(float[][] data, int num_gaussian) {
		
		//	Make the list of centers- num_gaussian lists, each at the
		//	length of the network input
		float[][] centers = new float[num_gaussian][data[0].length];
		
		//	Initalize the first centers- force the initial clusters
		System.arraycopy(data, 0, centers, 0, centers.length);
		
		//	Prepare the loop
		HashMap<float[], Integer> old_assignments = null;
		boolean changed = true;
		
		int x = 0;
		
		//	Run the loop. Terminate when there is no change because
		//	this function is proven to terminate without requiring
		//	an infinite convergence.
		while (changed) {
			Main.sout("Running K-Means", x++);
			changed = false;
			HashMap<float[], Integer> assignments = new HashMap<>();
			float[][] new_centers = new float[num_gaussian][data[0].length];
			
			//	Assignments
			int[] center_count = new int[num_gaussian];
			for (float[] f : data) {
				
				//	Find the closest center
				int min_index = 0;
				double min_dis = euclidean_distance(centers[0], f);
				for (int i = 1; i < centers.length; i++) {
					double distance = euclidean_distance(centers[i], f);
					if (distance < min_dis) {
						min_dis = distance;
						min_index = i;
					}
				}
				
				//	Save the this data point's closest center
				assignments.put(f, min_index);
				
				//	Did the point change closest cluster since last time?
				if (old_assignments == null || old_assignments.get(f) != min_index) {
					changed = true;
				}
				
				//	Make up new centers
				for (int i = 0; i < f.length; i++) {
					new_centers[min_index][i] += f[i];
				}
				
				//	Count the number in this center (good for average later)
				center_count[min_index]++;
				
			}
			
			//	Updates
			for (int i = 0; i < num_gaussian; i++) {
				for (int j = 0; j < new_centers[i].length; j++) {
					
					//	Average the centers
					if (center_count[i] != 0) {
						new_centers[i][j] /= center_count[i];
					}
				}
			}
			
			//	store this iteration for next time
			old_assignments = assignments;
			centers = new_centers;
		}
		
		//	Return the best
		return centers;
	}
	
	/**
	 * Gets the Euclidean distance between two vectors
	 * @param a One vector
	 * @param b Another vector
	 * @return The Euclidean distance
	 */
	public static double euclidean_distance(float[] a, float[] b) {
		double sum = 0.0;
		for (int i = 0; i < a.length && i < b.length; i++) {
			sum += Math.pow(a[i]-b[i],2);
		}
		return Math.sqrt(sum);
	}
}

