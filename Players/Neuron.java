package Players;

import Logic.*;
import java.util.ArrayList;

public class Neuron {
    
    char[][] state;         // Input   
    int expectedOutcome;    // Output
    
    ArrayList<Edge> edges;  //Connections to nodes
    
    public Neuron(char[][] inState){
        edges = new ArrayList<>();
    }
    
    public void addEdge(Edge e){
        edges.add(e);
    }
}
