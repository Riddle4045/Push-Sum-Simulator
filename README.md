# Push-Sum-Simulator
Push-Sum and Gossip protocol simulation with Actor model on different topologies.
Contributors : [Ishan Patwa](http://riddle4045.github.io/blog) and [Juthika Das](http://djuthika.github.io/)


###How to run the program:
**Using Jar file**: Located in target/scala-2.11/xyz.jar
```
Java –jar file_name [ #nodes ] [topology] [algorithm] 
Example: java –jar Project2.jar 1000 Full push-sum
```
Various topologies implemented are:

- Line Topology
- Full topology
- 3D Topology
- Imperfect 3D Topology


Algorithms Implemented are:

- Push-Sum Algorithm.
- Gossip Simulator.

###Adopted working Model:
- We have used a simple build and run approach for each Algo-Topology combination.
- 1D, 3D array are used to implement the topologies.
- The termination condition is implement as per the project description.
- Convergence time is take as the time where a message is sent to first dead node. i.e first dead letters encountered.
- Convergence time can be construed differently by each person, in our understanding it the moment when we think everyone has received x times to approximately ensure its travelled across the network.


###Performance:
Here are some Graphs describing the performance achieved with our code.

[]()
[]()
[]()
[]()
[]()



