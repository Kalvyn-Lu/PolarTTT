PolarTTT
========

CSCI 446 Articial Intelligence  Final Project

Group Members
Todd Beckman
Kalvyn Lu
(Dylan Hills)

==============================================

Notes to group members:
Todd says:
Predicate logic for our winning condition just got much easier since I just recently learned that there are only 4 rings, not 5, so now it shouldn't be nearly as difficult to test the winning conditions using predicate logic. 

If the * is where the most recent move was, then there are two cases to be checked- inner and outer rings (r=0,3 or r=1,2). Ring-based win check can be the same regardless of the ring.

==============================================

Files:
Main.java
This runner instantiates a PolarTTT object and then runs it.

PolarTTT.java
An object instantiated from this class has multiple purposes. It:

1) Manages a GUI with which to interact with the user. It sends the GUI the status of the game and receives mouse input. It is what tracks the keyboard input instead of the GUI.

2) Handles the game logic. The rules of the game are enforced here. The players can request information about the state of the game, and ultimately are asked to decide where to go each turn. Invalid moves result in a disqualification.

3) Restarting or closing the game. Closing can be done at any time by pressing Escape, while pressing Enter when a game is over will restart the game loop to the initial menu.

Note:
There is a known issue in Java regarding how input listeners stop working sometimes when focus is lost and regained. This has been fixed with a compromise: on Windows computers, the window flashes on the taskbar.

Also note:
There is a known issue where it may take multiple mouse clicks to play on a location as the Human Player. Just keep trying and it will accept it within 1-3 tries.

Location.java
This class abstracts grid locations into a tuple that pairs two values rather than keeping them entirely separate. It also provides a method and the potential for more that help simplify the semantics behind what a location represents.

GameCanvas.java
This class denotes objects that are the GUI. It draws both the menues and the game states as well as delivering useful and essential information to the user. It contains a mouse listener that will feed into the game the grid location closest to the location of the mouse click. Players can also preview which moves are currently available as well.