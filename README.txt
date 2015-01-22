Kushagra Udai
UFID: 0937-7483

Gossip.Simulator
================

Gossip simulator using akka actors

The input provided (as command line to networkTopology.scala) will be of the form:

  topologyTest.scala numNodes topology algorithm
  
nNodes - the number of actors involved (for 2D based topologies it is round up until it is a square),
topology - one of "full", "2D", "line", "imp2D"
algorithm - one of "gossip" or "push-sum".

Output: Print the amount of time it took to achieve convergence of the algorithm.


The actual network topology plays a critical role in the dissemination speed of Gossip protocols. This project experiments with various topologies. The topology determines who is considered a neighbour in the above algorithms.
Full Network: Every actor is a neighbour of all other actors. That is,every actor can talk directly to any other actors.
2D Grid: Actors form a 2D square grid of maximum possible size which is lesser than number of actors. The actors can only talk to the grid neigbours.
Line: Actors are arranged in a line. Each actor has only 2 neighbours (one left and one right, unless you are the first or last actor).

Imperfect 2D: Grid arrangement but one random other neighbour is selected from the list of all actors (4+1 neighbours).


Gossip Algorithm for information propagation involves the following:
Initialization - A participant(actor) it told/sent a rumor(fact) by the main process
Step - Each actor selects a random neighbour and tells it the rumor
Termination - Each actor keeps track of rumors and how many times it has heard the rumor. It stops transmitting once it has heard the rumor N times (N is currently set to 10 (arbitrary), This parameter can be modified in the receiverNode class).

Push-Sum algorithm for sum computation
State - Each actor Ai maintains two quantities: s and w. Initially, s = xi = i (that is actor number i has value i) and w = 1
Starting - Ask one of the actors to start from the main process.
Receive - Messages sent and received are pairs of the form (s;w). Upon receive, an actor should add received pair to its own corresponding values. Upon receive, each actor selects a random neighbour and sends it a message.
Send - When sending a message to another actor, half of s and w is kept by the sending actor and half is placed in the message.
Sum estimate - At any given moment of time, the sum estimate is s/w where s and w are the current values of an actor.
Termination - If an actors ratio s/w did not change more than 10^-10 in 3 consecutive rounds the actor terminates.


Largest Networks for each Topology as exectued on my computer is as follows:

TOPOLOGY	           ALGORITHM            TOTAL NUMBER OF NODES
LINE							GOSSIP							5000
2D							GOSSIP							10000
IMPERFECT 2D		GOSSIP							10000
FULL						GOSSIP							5000

LINE							PUSH-SUM						5000
2D							PUSH-SUM						5000
IMPERFECT 2D		PUSH-SUM						10000
FULL						PUSH-SUM						5000

The Bonus section is added as networkTopologyBonus.scala which replaces the original file and has a variable nKill in the master class which determines how many nodes to kill.
