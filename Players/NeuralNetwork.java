
package Players;

import java.util.ArrayList;
import java.util.Random;
import Logic.PolarTTT;
import java.util.Arrays;


public class NeuralNetwork {
    Neuron[][] shrek;
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
    public void initializeNetwork(){
        shrek = new Neuron[layers.length-1][];
        for(int i = 0; i < shrek.length; i++){
            shrek[i] = new Neuron[layers[i + 1]];
            for(int j = 0; j < layers[i+1]; j++){
                shrek[i][j] = new Neuron();
                if(i > 0){
                    for(int k = 0; k < layers[i-1]; k++){
                        System.out.println("Added");
                        connect(shrek[i][j],shrek[i-1][k]);
                    }
                }
            }
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
        to.addEdge(newEdge);
    }
    
    /**
     * Output the heuristic given the float array
     * @param input
     * @return 
     */
    public float[] output(float[] input){
        float[] tempInput = input;
        for(Neuron[] layer:shrek){
            float[] output = new float[layer.length];
            for(int i = 0; i < layer.length; i++){
                output[i] = layer[i].output(tempInput);
            }
            tempInput = output;
        }
        System.out.println(tempInput);
        return tempInput;
    }
    
    public void backProp(){
        
    }
    
    public static void main (String[] args){
        int[] layerd = new int[] {2,10,1};
        float[] input = new float[]{1,1,1,1};
        NeuralNetwork net = new NeuralNetwork(layerd);
        net.output(input);            
    }
        
    }
