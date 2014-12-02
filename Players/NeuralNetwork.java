
package Players;

import java.util.ArrayList;
import java.util.Random;
import Logic.PolarTTT;
import java.util.Arrays;


public class NeuralNetwork {
    ArrayList neurons;          //List of neurons in the neural network
    
    float[] initialInput;        //Input Layer
    
    public NeuralNetwork(char[][] board){
        int initCounter = 0;                            //Counter to fill initalInput
        neurons = new ArrayList<>();                    //Initialize neurons ArrayList
        initialInput = new float[board.length * board.length];  //Initialize initialInput
        
        for(int i = 0; i < board.length;i++){           //Loop to fill initialInput
            for(int j = 0; j < board.length; j++){
                initCounter++;                          //Increment counter
                switch(board[i][j]){                    //Switch statement to fill initalInput corresponding to the board
                    case PolarTTT.EMPTY:                //If board[i][j] == EMPTY, insert 0 into intialInput
                        initialInput[initCounter] = 0;
                        break;
                    case 'X':                           //If board[i][j] == X , insert 1
                        initialInput[initCounter] = 1;
                        break;
                    case 'O':                           //If board[i][j] == O, insert -1 
                        initialInput[initCounter] = -1;
                        break;
                }
            }
        }
    }
    
    /**
     * Create neurons,connection edges to initialize neural net
     */
    public void initializeNetwork(){
        //initialize the first layer
        for(int i = 0; i < initialInput.length; i++){
            addNeuron(new Neuron());
        }
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