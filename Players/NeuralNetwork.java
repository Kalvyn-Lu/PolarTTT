
package Players;

import java.util.ArrayList;
import java.util.Random;


public class NeuralNetwork {
    ArrayList neurons;          //List of neurons in the neural network
    
    public NeuralNetwork(){
        neurons = new ArrayList<>();
    }
    /**
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
    
    public void feedForward(){
        
    }
    
    public void backProp(){
        
    }
}