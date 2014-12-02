package Players;

import Logic.*;
import java.util.ArrayList;

public class Neuron {
      
    ArrayList<Edge> edges;  //Connections to nodes
    
    float[] lastInput;     //Last input for Error check
    float lastOutput;       //Last Output for Error check
    
    public Neuron(){
        edges = new ArrayList<>();
    }
    
    /**
     * Adds Edge from this neuron to toAdd
     * 
     * @param toAdd Edge to be added 
     */
    
    public void addEdge(Edge toAdd){
        edges.add(toAdd);
    }
    /**
     * Output of the node
     * 
     * sets lastInput to input
     * sets lastOutput to output
     * 
     * @param input input of the node
     * @return the output of the node : sum of input put through activation function 
     */
    
    public float output(float[] input){
        float output = 0;
        //sum up all input
        for(int i = 0; i < input.length; i++){
            output += input[i];
        }
        output = activationFuntion(output);
        lastInput = input;
        lastOutput = output;
        return output;
    }
    
    /**
     * Passes n through a logistic activation function
     * @param n variable to be passed through the activation function
     * @return  
     */
    
    public float activationFuntion(float n){
        float function = (float) (1/(1 + Math.exp(n)));
        return function;
    }
}