package Players;

import Logic.*;
import java.util.ArrayList;

public class Neuron {
    
    float[] input;         // Input   
    float expectedOutcome;  // Output
    Edge[] edges;  //Connections to nodes
    
    char[][] lastInput;     //Last input for Error check
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
}
