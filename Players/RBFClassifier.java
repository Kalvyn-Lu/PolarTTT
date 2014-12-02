package Players;

import java.io.*;
import java.util.HashMap;

public class RBFClassifier {
	
	private RBFNetwork network;
	
	/**
	 * Builds a manager for a Radio-Basis Function network which will classify
	 * input into num_output possible classes.
	 * @param num_inputs The number of inputs into the network
	 * @param num_gaussian The number of hidden nodes in the network
	 * @param num_output The number of categories (the output layer size).
	 */
	public RBFClassifier(int num_inputs, int num_gaussian, int num_output) {
		network = new RBFNetwork(num_inputs, num_gaussian, num_output);
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
			if (best < class_vals[i]) {
				best = i;
			}
		}
		
		return best;
	}
	
	
	public void learn(float[] input, float[] outcome) {
		//	Learning requires "backpropagation"
		network.back_propogate(
				
				//	The expected is what the network thinks will happen
				network.get_output(input),
				
				//	The actual is given based on the data
				outcome);
	}

	/**
	 * Sets the weights of the hidden layer based on the data. Also writes
	 * the data to a file to read for later.
	 * @param filename The output file
	 * @param data The experimental data
	 * @param num_gaussian
	 */
	public void set_weights_w(String filename, float[][] data, int num_gaussian) {
		
		//	make k-means clusters
		float[][] kmeans = kmeans(data, num_gaussian);
		
		//	Now write the data to the file
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("rbf.kmeans.csv"), "utf-8"));
			
			//	Clear the file
			writer.write("");
			for (int i = 0; i < kmeans.length; i++){
				
				//	Dirty implode (Java so lame)
				String line = "";
				for (int j = 0; j < kmeans[i].length; j++) {
					line += kmeans[i][j] + ",";
				}
				writer.append(line.substring(0, line.length() - 1) + "\n");
				writer.newLine();
			}
		}
		catch (IOException ex) {} 
		finally {
			try {
				writer.close();
			} 
			catch (Exception ex) {}
		}

		//	Simply these clusters to the hidden layer
		for (int i = 0; i < kmeans.length; i++) {
			network.gnodes[i].set_centers(kmeans[i]);
		}
	}
	
	/**
	 * Sets the weights of the hidden layer based on the data. Also writes
	 * the data to a file to read for later.
	 * @param filename The input file
	 */
	public void set_weights_r(String filename) {
		BufferedReader file = null;
		try {
			file = new BufferedReader(new FileReader(filename));
			for (int i = 0; i < network.gnodes.length; i++) {
				
				//	Each line is a list of centers for a single gnode
				String s = file.readLine();
				String[] split = s.split(",");
				
				//	Get the list of centers to assign to the gnode
				float[] data = new float[split.length];
				for (int j = 0; j < data.length; j++) {
					data[j] = Float.parseFloat(split[j]);
				}
				
				//	Assign
				network.gnodes[i].set_centers(data);
			}
			file.close();
		}
		catch (IOException e) {}
	}
	

	/**
	 * Applies k-means clustering on a data file into a given number of
	 * clusters.
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
        
        //	Run the loop. Terminate when there is no change because
        //	this function is proven to terminate without requiring
        //	an infinite convergence.
        while (changed) {
        	
            changed = false;
            HashMap<float[], Integer> assignments = new HashMap<>();
            float[][] new_centers = new float[num_gaussian][data[0].length];
            
            //	Assignments
            int[] center_count = new int[num_gaussian];
            for (float[] f : data) {
            	
            	//	Find the closest center
                int min_index = 0;
                double min_dis = euclidean(centers[0], f);
                for (int i = 1; i < centers.length; i++) {
                    double distance = euclidean(centers[i], f);
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
    protected static double euclidean(float[] a, float[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i]-b[i],2);
        }
        return Math.sqrt(sum);
    }
}

class RBFNetwork {
	protected GaussianNode[] gnodes;
	protected OutputNode[] onodes;
	protected float learning_rate;
	
	/**
	 * Constructs an actual RBF network
	 * @param num_inputs The number of input layer nodes
	 * @param num_gaussian The number of hidden layer nodes
	 * @param num_outputs The number of output layer nodes (classifications)
	 */
	public RBFNetwork(int num_inputs, int num_gaussian, int num_outputs) {
		learning_rate = 0.1f;
		
		//	Save the kmeans data to initialize the gnodes
		gnodes = new GaussianNode[num_gaussian];
		for (int i = 0; i < num_gaussian; i++) {
			//	Inputs = input space
			gnodes[i] = new GaussianNode();
		}
		
		//	Initialize the output nodes (random weights will be updated)
		onodes = new OutputNode[num_outputs];
		for (int i = 0; i < num_outputs; i++) {
			
			//	Inputs = number of hidden nodes
			onodes[i] = new OutputNode(num_gaussian);
		}
	}
	
	/**
	 * Gets the trained outputs of the network given input
	 * @param input
	 * @return The outputs
	 */
	public float[] get_output(float[] input) {

		//	Feed input layer into gaussian layer
		float gaussian[] = new float[gnodes.length];
		for (int i = 0; i < gaussian.length; i++) {
			gaussian[i] = gnodes[i].output(input);
		}
		
		//	Feed gaussian layer into output layer
		float output[] = new float[onodes.length];
		for (int i = 0; i < output.length; i++) {
			output[i] = onodes[i].output(gaussian);
		}
		
		return output;
	}
	
	/**
	 * Updates the weights of the hidden layer (the heights of the
	 * Gaussian functions) in order to allow the network to learn.
	 * @param expected What was thought to be the outcome
	 * @param outcome The actual outcome
	 */
	public void back_propogate(float[] expected, float[] outcome) {
		
		//	Calculate the error for each node of the output layer
		for (int i = 0; i < onodes.length; i++) {
			
			//	Get the error of this classification
			float error = expected[i] - outcome[i];
			
			//	Update the weights
			for (int j = 0; j < onodes[i].weights.length; j++){
				
				//	The weight update function
				onodes[i].weights[j] += 
						learning_rate *
						error *
						onodes[i].last_input[j];
			}
		}
	}
}

class GaussianNode {
	public static final float C = 1;
	float[] centers;
	
	
	public GaussianNode() { }
	
	public float output(float[] raw) {
		
		//	Gaussify the distance between input layer and the gauss centers
		return gaussian_function(
			RBFClassifier.euclidean(raw, centers)
		);
	}
	
	public void set_centers(float[] centers) {
		this.centers = centers;
	}
	
	private static float gaussian_function(double in) {
		return (float)Math.exp(- (in * in) / (2 * C * C));
	}
}

class OutputNode {
	protected float[] weights;
	protected float[] last_input;
	
	public OutputNode(int num_inputs) {
		weights = new float[num_inputs];
		for (int i = 0; i < num_inputs; i++) {
			
			//	Keep the weights between -5 and 5.
			weights[i] = (float)(Math.random() - .5);
		}
	}
	
	public float output(float[] goutput) {
		last_input = goutput;
		
		float sum = 0f;
		
		//	Dot product weights*goutput
		for (int i = 0; i < goutput.length; i++) {
			sum += weights[i] * goutput[i];
		}
		
		return sum;
	}
}

