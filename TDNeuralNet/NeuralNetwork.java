package TDNeuralNet;

public class NeuralNetwork {

    Neuron[][] net;
    int[] layers = {48,400,200,1};
    float[] lastOutput;
    float[] previousOutput;
    float learningRate = (float) .8;
    float gamma =(float) 0.8;
    /**
     *
     * @param inLayers to initialize the Network. #indices = #of layers,
     * elements = #nodes
     */
    public NeuralNetwork() {
        //layers = layer;
        lastOutput = new float[layers[layers.length-1]];
        previousOutput = new float[layers[layers.length-1]];
        initializeNetwork();
    }

    /**
     * Create neurons,connection edges to initialize neural net
     */
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

    public void learn (float[] boardArr, float answer) {
            output(boardArr);
            try{
                backPropagation(answer);
            }
            catch(Exception E){
                for(int i = 0; i < boardArr.length; i++){
                    System.out.print((int)boardArr[i]+",");
                }
                //System.out.println(line.length);
            }
            //System.out.println(line[48]);

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
        previousOutput = copyArray(lastOutput);
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
            lastOutput = copyArray(tempInput);
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

    public void backPropagation(float win) {
        learningRate *=gamma;
        //for all layers in the network, adjust the weights
        for(Neuron[] layers: net){
            //For each neuron in the layer, adjust the weight
            for(int i = 0; i < layers.length; i++){
                //Iteration through weights from edges
                for(int j = 0; j < layers[i].edges.size();j++){
                    //Adjust the weight
                    layers[i].edges.get(j).weight +=learningRate * (lastOutput[0] - previousOutput[0]);
                    //switch case 0 win, 1 loss, 2 tie
                    if(win==0)  layers[i].edges.get(j).weight += learningRate* 1;
                    else if(win == 1)  layers[i].edges.get(j).weight +=learningRate* -1;
                    else if(win == 2) layers[i].edges.get(j).weight +=learningRate* 0.5;
                    else layers[i].edges.get(j).weight += 0;
                }
            }
        }
    }
    
   public void printWeights(){
        for(Neuron[] layers: net){
            for(int i = 0; i < layers.length;i++){
                for(int j = 0; j < layers[i].edges.size();j++){
                   System.out.print( layers[i].edges.get(j).weight+",");
                }
            }
        }
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
//    public static void main(String[] args) {
//        int layer[] = {10,10,1};
//        float[]input = {1,1};
//        NeuralNetwork net = new NeuralNetwork(layer);
//        net.printArray(net.net);
//        System.out.println(net.output(input)[0]);
//    }
}
