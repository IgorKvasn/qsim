

## What can be added (programmed) into qSim? ##
Basically qSim consists of 2 separate modules: **simulation core** and **GUI**. Both these modules can be extended.

## Extending simulation core ##
The whole simulation core was designed in flexibility and scalability in mind.
There are 2 elements in network topology:
  * [NetworkNode](http://code.google.com/p/qsim/source/browse/trunk/Simulation/src/main/java/sk/stuba/fiit/kvasnicka/qsimdatamodel/data/NetworkNode.java)
    * represents routers, switches and PCs
    * contains main logic of all topology vertices

  * [Edge](http://code.google.com/p/qsim/source/browse/trunk/Simulation/src/main/java/sk/stuba/fiit/kvasnicka/qsimdatamodel/data/Edge.java)
    * represents edges between NetworkNodes

All QoS mechanism implements the same interface _QosMechanism_. There are 3 main subclasses (implemented as _abstract classes_ ) that implements this interface:
  1. [PacketClassification](http://code.google.com/p/qsim/source/browse/trunk/Simulation/src/main/java/sk/stuba/fiit/kvasnicka/qsimsimulation/qos/classification/PacketClassification.java) - superclass for all packet classification and **marking** mechanisms
  1. [ActiveQueueManagement](http://code.google.com/p/qsim/source/browse/trunk/Simulation/src/main/java/sk/stuba/fiit/kvasnicka/qsimsimulation/qos/queuemanagement/ActiveQueueManagement.java) - superclass for all active queue mechanisms
  1. [PacketScheduling](http://code.google.com/p/qsim/source/browse/trunk/Simulation/src/main/java/sk/stuba/fiit/kvasnicka/qsimsimulation/qos/scheduling/PacketScheduling.java) - superclass for all packet scheduling mechanisms

To create new QoS mechanism you simply extend one of these abstract classes.

There is one more key class that defines QoS mechanisms that are used by NetworkNode (note that each NetworkNode can use different QoS mechanisms) - [QosMechanismDefinition](http://code.google.com/p/qsim/source/browse/trunk/Simulation/src/main/java/sk/stuba/fiit/kvasnicka/qsimsimulation/qos/QosMechanismDefinition.java). This class stores information about all QoS mechanisms, including defined QoS classes. This class is quite handy when you want to actually use your new QoS mechanism.

## Extending GUI module ##
_(note: it is highly recommened to read previous chapter as well, even if you don't want to create new QoS mechanism)_

GUI module is developed in Netbeans Rich Client Platform (RCP). One of the main features that Netbeans RCP provides is modularization. It is possible to write new plugin and add it to GUI module in standard way provided by Netbeans RCP.