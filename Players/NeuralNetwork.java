
package Players;

import java.util.ArrayList;
import java.util.Random;
import Logic.PolarTTT;
import java.util.Arrays;


public class NeuralNetwork {
    Neuron[][] net;
    int[] layers;
    
    /**
     * 
     * @param inLayers to initialize the Network. #indices = #of layers, elements = #nodes
     */
    public NeuralNetwork(int[] inLayers){
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
    public void initializeNetwork(){
        net = new Neuron[layers.length][];
        //loop to Create Layers (1st dim is layer)
        for(int i = 0; i < layers.length; i++){
            net[i] = new Neuron[layers[i]];
            //loop to Create Nodes within layers(2nd dim is neurons)
            for(int j = 0; j < layers[i]; j++){
                net[i][j] = new Neuron();
                //After creating a node after the first layer, connect all nodes
                //from previous layer to newly created node.
                if(i > 0){
                    for(int k = 0; k < layers[i-1];k++){ 
                        connect(net[i-1][k],net[i][j]);
                    }
                }
            }
        }
    }
    
    void printArray(Neuron[][] n){
        for(int i = 0; i< n.length;i++){
            for(int j = 0; j < n[i].length;j++){
                System.out.print("[" + n[i][j].edges.size() + "]");
            }
            System.out.println();
        }
    }
    
    /**
     * 
     * @param from The neuron for the connection to be made from
     * @param to   The neuron for the connection to be made to
     */
    void connect(Neuron from, Neuron to){                      //Generates random number
        float randomFloat = (float) (Math.random() - .5);
        Edge newEdge = new Edge(from,to,randomFloat);//Create a new edge from Neuron 'from' to Neuron 'to'
        from.addEdge(newEdge);                                 //Connect edge from Neuron 'from' to neuron 'to'      
    }
    
    /**
     * Output the heuristic given the float array
     * @param input
     * @return 
     */
    public float[] output(float[] input){
        float[] tempInput = input;
        //for each layer in the neural net
        for(int i = 0; i < net.length; i++){
            //Output array for each layer
            float[] output = new float[net[i].length];
            //put input through output function of neurons, set to output,
            //Output still needs to be weighted
            for(int j = 0; j < output.length; j++){                
                output[i] = net[i][j].output(tempInput);
            }
            //make output the new input for the next layer
            System.arraycopy(output, 0, tempInput, 0, output.length);
        }
        return tempInput;
    }
    
    public void backProp(){
        
    }
    
    public static void main (String[] args){
        int[] layerd = new int[] {2,10,1};
        float[] input = new float[]{1,1,1,1};
        NeuralNetwork net = new NeuralNetwork(layerd);
        net.printArray(net.net);
        net.output(input);            
    }
        
    }
