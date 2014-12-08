PolarTTT
========

Todd Beckman, Dylan Hills, Kalvyn Lu

CSCI 446 Articial Intelligence Semester Project

In progress:
========
->Setting up game to store game data for learning agents (Todd)

->Finalizing RBF Classifier for this game's use (Todd)

->Setting up TD ANN (Kalvyn)

Files:
========
Logic package:
========

Main.java: This runner instantiates a PolarTTT object and then runs it.
There is also an included feature to allow reading .CSV files into float matrices and vice versa,
a feature for the learning and testing data on the learning algorithms.

PolarTTT.java: An object instantiated from this class is the centralized game manager. It tracks
the status of the game while sending information to the graphical user interface for users to
view game progress. Players are queried for their moves one at a time in this game. A human player
may attempt an invalid move to no effect, while all computer players attempting invalid moves will
be immediately disqualified. When the game ends (in a win, loss, or tie), the user can then choose
to restart the game for another match.

GameCanvas.java: An object instantiaged from this class renders the game state. Initially, this
canvas is a menu for the user to pick settings such as which heuristic functions the computer players
are to user and whether to run a single game or in bulk. Humans cannot play in bulk. A single game is
rendered turn by turn, but bulk games will display only a few important statistics.

Note: There is a known issue in Java in which losing window focus may not fire the focusLost event.
In order to prevent the graphical user interface from becoming completely locked, focus is requested
immediately. For Window users, this causes the window to flash. This behavior is normal.

Location.java: This class abstracts grid locations into a tuple that pairs two values. In addition to 
this semantic abstraction, it bypasses Java's multiple return limitation. Also helpful of this class
is the method that fetches the list of five or eight adjacent locations on the grid.

Players package:
========

Player.java: This abstract class provides base functionality of players for PolarTTT.java to interact
with. Important methods are getName(), which allows the player to decide at runtime which identifier
is to be used in the graphical user interface, and getChoice(), which provides for the player which
locations on the board are legal moves and returns to the game the choice of move.

RandomPlayer.java: A player instantiated from this class will pick random moves from the list of
available moves every turn.

GreedyPlayer.java: A player instantiated from this class will use the game's choice of heuristic
function to maximize the fitness of the next move only.

MinimaxPlayer.java: A player instantiated from this class will use the game's choice of heuristic
function to maximize the fitness of the next N/2 moves by searching N plies into a tree of options
and applying the Minimax algorithm to select the best plan. Alpha-beta pruning is applied only if
the user has chosen to use it.

RBFClassifier package:
========

RBFNetwork.java: An object instantiated from this class is a neural network with one input layer, one
hidden layer, and one output layer. The weights from the input layer into the hidden layer are
generated externally and given to the network. The neural network learns by updating the weights from
the hidden (Gaussian) layer into the output layer based on the the error. This makes it a supervised
learning agent.

RBFClassifier.java: An object instantiated from this class is a classifier that manages the RBFNetwork
and determines the best-fit class for given input according to the neural network simply by maximizing
the output of the neural network. It also generates the Gaussian functions' centers as averages among the
learning data. These averages are calculated with the k-means algorithm which is guaranteed to find
a local optimum for the centers in a finite (and typically small) number of steps, unlike many methods of
approximation which require convergence.
