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
     * sets lastInput to input
     * sets lastOutput to output
     * 
     * @param input input of the node
     * @return the output of the node : dot product of input and edge weights 
     */
    public float output(float[] input){
        float output = 0;
        for(int i = 0; i < input.length; i++){
            for(int j = 0; j < edges.length; j++){
                output = input[i] * edges[j].weight;
            }
        }
        lastInput = input;
        lastOutput = output;
        return output;
    }
}