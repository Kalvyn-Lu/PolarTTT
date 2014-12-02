
package Players;

import java.util.ArrayList;
import java.util.Random;
import Logic.PolarTTT;
import java.util.Arrays;


public class NeuralNetwork {
    ArrayList<Neuron>neurons;          //List of neurons in the neural network
    
    float[] initialInput;        //Input Layer
    
    int[] layers;
    
    public NeuralNetwork(int[] inLayers){
       layers = inLayers;
    }
    
    /**
     * Create neurons,connection edges to initialize neural net
     */
    public void initializeNetwork(){
        
    }
    
    
    /**
     * Adds neuron to the network
     * 
     * @param n The neuron to be added to the network 
     */
    void addNeuron(Neuron n){
        neurons.add(n);
    }
    
    /**
     * 
     * @param from The neuron for the connection to be made from
     * @param to   The neuron for the connection to be made to
     */
    void connect(Neuron from, Neuron to){
        Random randFloat = new Random();                       //Generates random number
        Edge newEdge = new Edge(from,to,randFloat.nextFloat());//Create a new edge from Neuron 'from' to Neuron 'to'
        from.addEdge(newEdge);                                 //Connect edge from Neuron 'from' to neuron 'to'                                  
    }
    
    public float[] output(){
        return null;
    }
    
    public void feedForward(){
        //Feed through the network
    }
    
    public void backProp(){
        
    }
}