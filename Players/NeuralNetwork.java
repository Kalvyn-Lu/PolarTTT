
package Players;

import java.util.ArrayList;
import java.util.Random;


public class NeuralNetwork {
    ArrayList neurons;          //List of neurons in the neural network
    
    public NeuralNetwork(){
        neurons = new ArrayList<>();
    }
    //Adds neuron to NeuralNetwork
    void addNeuron(Neuron n){
        neurons.add(n);
    }
    
    void connect(Neuron from, Neuron to){
        Random randFloat = new Random();                       //Generates random number
        Edge newEdge = new Edge(from,to,randFloat.nextFloat());//Connect two neurons with a random weight
    }
}