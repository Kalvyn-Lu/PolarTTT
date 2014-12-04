package TDNeuralNet;

public class NeuralNetwork {

    Neuron[][] net;
    int[] layers;

    /**
     *
     * @param inLayers to initialize the Network. #indices = #of layers,
     * elements = #nodes
     */
    public NeuralNetwork(int[] inLayers) {
        layers = inLayers;
        initializeNetwork();
    }

    /**
     * Create neurons,connection edges to initialize neural net
     */
//    public void initializeNetwork(){
//        shrek = new Neuron[layers.length-1][];
//        for(int i = 0; i < shrek.length; i++){
//            shrek[i] = new Neuron[layers[i + 1]];
//            for(int j = 0; j < layers[i+1]; j++){
//                shrek[i][j] = new Neuron();
//                if(i > 0){
//                    for(int k = 0; k < layers[i-1]; k++){
//                        System.out.println("Added");
//                        connect(shrek[i][j],shrek[i-1][k]);
//                    }
//                }
//            }
//        }
//        
//    }
    public void initializeNetwork() {
        net = new Neuron[layers.length][];
        //loop to Create Layers (1st dim is layer)
        for (int i = 0; i < layers.length; i++) {
            net[i] = new Neuron[layers[i]];
            //loop to Create Nodes within layers(2nd dim is neurons)
            for (int j = 0; j < layers[i]; j++) {
                net[i][j] = new Neuron();
                //After creating a node after the first layer, connect all nodes
                //from previous layer to newly created node.
                if (i > 0) {
                    for (int k = 0; k < layers[i - 1]; k++) {
                        connect(net[i - 1][k], net[i][j]);
                    }
                }
            }
        }
    }

    /**
     *
     * @param from The neuron for the connection to be made from
     * @param to The neuron for the connection to be made to
     */
    void connect(Neuron from, Neuron to) {                      //Generates random number
        float randomFloat = (float) (Math.random() - .5);
        Edge newEdge = new Edge(from, to, randomFloat);//Create a new edge from Neuron 'from' to Neuron 'to'
        to.addEdge(newEdge);                                 //Connect edge from Neuron 'from' to neuron 'to'      
    }

    /**
     * Output the heuristic given the float array
     *
     * @param input
     * @return
     */
    public float[] output(float[] input) {
        float[] tempInput = input;
        //for each layer in the neural net
        for (int i = 1; i < net.length; i++) {
            float[] output = new float[net[i].length];
            //fill the output array with the outputs of the layer
            for (int j = 0; j < output.length; j++) {
                //output[i] = output of respective node
                output[j] = net[i][j].output(tempInput);
            }
            //input for next layer is output of current layer
            tempInput = copyArray(output);
        }
        return tempInput;
    }

    /**
     *
     * @param toCopy
     * @return copy of array
     */
    public float[] copyArray(float[] toCopy) {
        float[] copy = new float[toCopy.length];
        for (int i = 0; i < toCopy.length; i++) {
            float iCopy = toCopy[i];
            copy[i] = iCopy;
        }
        return copy;
    }

    public void backPropogation() {
        float learningRate = 1;
        
        //for all layers in the network, adjust the weights
        for(Neuron[] layers: net){
            //For each neuron in the layer, adjust the weight
            for(int i = 0; i < layers.length; i++){
                //Iteration through weights from edges
                for(int j = 0; j < layers[i].edges.size();j++){
                    //Adjust the weight
                    //layers[i].edges.get(i).weight += weightAdjustFunction
                }
            }
        }
    }
    
    public void train(){
        
    }
    
    /**
     * Print array for testing.Neuron -> [#of edges]
     * @param n 
     */
    void printArray(Neuron[][] n) {
        for (int i = 0; i < n.length; i++) {
            for (int j = 0; j < n[i].length; j++) {
                System.out.print("[" + n[i][j].edges.size() + "]");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        int[] layerd = new int[]{100,1000,10};
        float[] input = new float[]{1, 1};
        NeuralNetwork net = new NeuralNetwork(layerd);
        net.printArray(net.net);
        float[] boop = net.output(input);
        for (int i = 0; i < boop.length; i++) {
            System.out.print(boop[i] + " ");
        }
    }

}
