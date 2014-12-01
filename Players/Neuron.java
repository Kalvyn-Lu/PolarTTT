package Players;

import Logic.*;
import java.util.ArrayList;

public class Neuron {
      
    Edge[] edges;  //Connections to nodes
    
    float[] lastInput;     //Last input for Error check
    float lastOutput;       //Last Output for Error check
    
    public Neuron(){
        
    }
    
    /**
     * Adds Edge from this neuron to toAdd
     * 
     * @param toAdd Edge to be added 
     */
    
    public void addEdge(Edge toAdd){
        for(int i = 0; i < edges.length;i++){
            if(edges[i]==null){
                edges[i] = toAdd;
                break;
            }
        }
    }
    /**
     * Output of the node
     * 
     * sets lastInput to input
     * sets lastOutput to output
     * 
     * @param input input of the node
     * @return the output of the node : dot product of input and edge weights 
     */
    
    public float output(float[] input){
        float output = 0;
        for(int i = 0; i < input.length; i++){          //Loop to find the dot
            for(int j = 0; j < edges.length; j++){      //product of input*weight
                output = input[i] * edges[j].weight;
            }
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