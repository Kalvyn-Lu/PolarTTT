package RBFClassifier;

import java.util.Arrays;
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
		float[][] kmeans = KMeans(data, num_gaussian, 48);
		
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
		float[][] weights = new float[network.onodes.length][network.onodes[0].weights.length];
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
	public static float[][] KMeans(float[][] data, int num_clusters, int input_length) {
        float[][] centers = new float[num_clusters][input_length];
        for (int i = 0; i < num_clusters; i++) {
            System.arraycopy(data[i], 0, centers[i], 0, input_length);
        }
        HashMap<float[], Integer> assignments = new HashMap<>(); //hashmap representing the cluster each point is assigned to
        boolean changed; //boolean to check if any points changed clusters
        int x = 0;
        do {
        	Main.sout("KMeans Iteration:",x++);
        	if (x % 100 == 0) {
        		for (float[] arr : centers) {
        			System.out.println(Arrays.toString(arr));
        		}
        	}
            changed = false;
            float[][] new_centers = new float[num_clusters][input_length];
            int[] center_count = new int[num_clusters];
            for (float[] f : data) { //O(n) 
                int mindex = 0; //index of minimum cluster center
                double mindis = euclideanSqrd(centers[0], f, input_length); //minimum distance to a center yet found
                //find the closest center
                for (int i = 1; i < centers.length; i++) { //O(k)
                    double dis = euclideanSqrd(centers[i], f, input_length);
                    if (dis < mindis) {
                        mindis = dis;
                        mindex = i;
                    }
                }
                Integer old_assign = assignments.get(f); //the old assignment
                if (old_assign == null || !old_assign.equals(mindex)) { //check if the assignment changed
                    changed = true;
                }
                assignments.put(f, mindex); //assign point f to cluster mindex
                for (int i = 0; i < input_length; i++) {
                    new_centers[mindex][i] += f[i];
                }
                center_count[mindex]++; //increment the number of points in the cluster this point was asssigned to
            }
            for (int i = 0; i < num_clusters; i++) { //average the new centers
                for (int j = 0; j < input_length; j++) {
                	if (center_count[i] == 0) {
                		System.out.println("a");
                	}
                    new_centers[i][j] /= center_count[i]; //center count should never be 0, might as well throw an error
                }
            }
            centers = new_centers; //change centers
        } while (changed); //case check
        return centers;
    }
 
    public static double euclideanSqrd(float[] f1, float[] f2, int input_length) {
        double sum = 0.0;
        float d;
        for (int i = 0; i < input_length; i++) {
            d = f1[i] * f2[i];
            sum += d * d;
        }
        return sum;
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

