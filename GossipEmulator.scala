import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import com.sun.org.apache.xpath.internal.operations.Bool
import scala.collection.immutable.HashMap

object Constants{
          val ConvergenceError = 0.000000001;  //stopping condition for pushSum operation
          val CounterLimit = 10; //stopping condition for gossip protocols
}

class Workers(arrActors: Array[ActorRef], numNodes: Int) extends Actor {

  //array keeping track of no. of messages each node has recieved.
  var counter: Array[Int] = new Array[Int](numNodes);
  for (i <- 1 to numNodes) {
    counter(i - 1) = 0;
  }

  //counters for push_sum algorithm
  var s : BigDecimal = self.path.name.toFloat;
  var w : BigDecimal = 1.0
  var ratio : BigDecimal = s/w;
  var convergenceCounter : Int = 0;
  

  def receive = {
    case "Rumour_gossip" =>
      println(self.path.name + " recieved a message");
      transmitGossip;
    case "Rumour_push-sum" =>
      println("Starting the push_Sum algorithm");
      transmit_pushSumMessage;
    case (s_m : BigDecimal,w_m : BigDecimal) => //println("recieved a tuple (s,w) :" + s_m.toString + "  " + w_m.toString);
      updateCounters(s_m,w_m);
      transmit_pushSumMessage;
    case "Full_Mesh_Rumour_gossip" =>
      println("received message from" + sender.path.name);
      transmitGossipInMesh;
  }

  //message dissemination in full mesh topology
  def transmitGossipInMesh = {
    var index = self.path.name;
    var actorNumber = index.toInt;
    counter(actorNumber) = counter(actorNumber) + 1;
    if (counter(actorNumber) == Constants.CounterLimit ) {
      println("Worker" + actorNumber.toString + "is stopping");
      context.stop(self);
    }

    //var i = index.toInt;
    val r = scala.util.Random;
    //var nextActorNumber = r.nextInt(numNodes);
    var actNum = r.nextInt(numNodes);
    while (actNum == actorNumber) {
      actNum = r.nextInt(numNodes);
    }
    arrActors(actNum) ! "Full_Mesh_Rumour_gossip";
  }

  //message dissemination in line topology
  def transmitGossip = {

    var index = self.path.name;
    var actorNumber = index.toInt;
    counter(actorNumber) = counter(actorNumber) + 1;
    if (counter(actorNumber) == Constants.CounterLimit) {
      println("Worker" + actorNumber.toString + "is stopping");
      context.stop(self);
    }

    if (index == "0") {
      arrActors(1) ! "Rumour_gossip";
    } else if (index == (arrActors.length - 1).toString) {
      arrActors(arrActors.length - 2) ! "Rumour_gossip";
    } else {
      var i = index.toInt;
      val r = scala.util.Random;
      if (r.nextBoolean()) {
        arrActors(i + 1) ! "Rumour_gossip";
      } else {
        arrActors(i - 1) ! "Rumour_gossip";
      }

    }
  }
  
  //message dissemination in line tpology - PUSH-SUM algorithm
  def transmit_pushSumMessage = {
            s = s/2 ; w = w/2;
            var message = (s/2,w/2);
            val r = scala.util.Random;
            var actorNumber = self.path.name.toInt;  //ith actor in the array.
            var actNum = r.nextInt(numNodes);
             if(actorNumber == 0){
                 arrActors(1) ! message;
             }else if(actorNumber == arrActors.length -1){
                   arrActors(arrActors.length -2) ! message;
             }else{
                 var r = scala.util.Random;
                 if(r.nextBoolean()){
                     arrActors(actorNumber+1) ! message;
                 }else {
                     arrActors(actorNumber-1) ! message;
                 }
             }
        
  }
  
  def tranmit_pushSumFullMesh = {  
            s = s/2 ; w = w/2;
            var message = (s/2,w/2);
            val r = scala.util.Random;
            var actorNumber = self.path.name.toInt;  //ith actor in the array.
            var actNum = r.nextInt(numNodes);
            while(actNum == actorNumber){
                actNum = r.nextInt(numNodes)
            }
            arrActors(actNum) ! message;
    
    
  }
  
  def updateCounters(s_m :BigDecimal , w_m : BigDecimal) = {
            var new_ratio =  (s + s_m)/(w + w_m);
            if(new_ratio - ratio > convergenceCounter || new_ratio -ratio < (-1)*convergenceCounter) {
                  convergenceCounter = 0;
            }else {
                convergenceCounter += 1;
            }
            println("convergenceCounter for  : " + self.path.name + " " + convergenceCounter.toString());
           if(convergenceCounter == 3 ) context.stop(self);       
  }

}

class Workers3D(arrActors: Array[Array[Array[ActorRef]]], numNodes: Int, actorNum : Int) extends Actor {

  var counter = Array.ofDim[Int](numNodes, numNodes, numNodes);
  for (i <- 1 to numNodes) {
    for (j <- 1 to numNodes) {
      for (k <- 1 to numNodes) {
        counter(i - 1)(j - 1)(k - 1) = 0;
      }
    }
  }
    //counters for push_sum algorithm
  var s : BigDecimal = actorNum
  var w : BigDecimal = 1.0
  var ratio : BigDecimal = s/w;
  var convergenceCounter : Int = 0;

  def receive = {
    case "3D_rumour_gossip" =>
      println(self.path.name + " recieved a message");
      transmit3DGossip;
    case "3D_rumour_push-sum" => println("starting push sum algorithm for 3D grid")
    transmit3DpushSumMessage;
    case (s_m : BigDecimal , w_m : BigDecimal ) => updateCounters(s_m,w_m)
    transmit3DpushSumMessage;

  }

  def transmit3DGossip = {

    var index = self.path.name.split(",");
    counter(index(0).toInt)(index(1).toInt)(index(2).toInt) += 1;
    if (counter(index(0).toInt)(index(1).toInt)(index(2).toInt) == Constants.CounterLimit) {
      println("Worker" + index.toString + "is stopping");
      context.stop(self);
    }
    val r = scala.util.Random;
    var randomGen = r.nextInt(6);
    val rangeStart = 0; val rangeEnd = numNodes - 1;
    var isSent: Boolean = false;
    while (!isSent) {

      randomGen = r.nextInt(6);

      if (randomGen == 0) {
        if (index(0).toInt - 1 >= 0) {
          arrActors(index(0).toInt - 1)(index(1).toInt)(index(2).toInt) ! "3D_rumour_gossip";
          isSent = true;
        }

      } else if (randomGen == 1) {
        if (index(1).toInt - 1 >= 0) {
          arrActors(index(0).toInt)(index(1).toInt - 1)(index(2).toInt) ! "3D_rumour_gossip";
          isSent = true;

        }

      } else if (randomGen == 2) {
        if (index(2).toInt - 1 >= 0) {
          arrActors(index(0).toInt)(index(1).toInt)(index(2).toInt - 1) ! "3D_rumour_gossip";
          isSent = true;
        }

      } else if (randomGen == 3) {
        if (index(0).toInt + 1 < numNodes) {
          arrActors(index(0).toInt + 1)(index(1).toInt)(index(2).toInt) ! "3D_rumour_gossip";
          isSent = true;
        }

      } else if (randomGen == 4) {

        if (index(1).toInt + 1 < numNodes) {
          arrActors(index(0).toInt)(index(1).toInt + 1)(index(2).toInt) ! "3D_rumour_gossip";
          isSent = true;
        }

      } else if (randomGen == 5) {
        if (index(2).toInt + 1 < numNodes) {
          arrActors(index(0).toInt)(index(1).toInt)(index(2).toInt + 1) ! "3D_rumour_gossip";
          isSent = true;
        }
      }
    }
  }

  def transmit3DpushSumMessage= {
    var index = self.path.name.split(",");
    val r = scala.util.Random;
    var randomGen = r.nextInt(6);
    val rangeStart = 0; val rangeEnd = numNodes - 1;
    var isSent: Boolean = false;
    s = s/2;
    w = w/2;
    var message = (s/2,w/2);
    while (!isSent) {

      randomGen = r.nextInt(6);

      if (randomGen == 0) {
        if (index(0).toInt - 1 >= 0) {
          arrActors(index(0).toInt - 1)(index(1).toInt)(index(2).toInt) ! message;
          isSent = true;
        }

      } else if (randomGen == 1) {
        if (index(1).toInt - 1 >= 0) {
          arrActors(index(0).toInt)(index(1).toInt - 1)(index(2).toInt) ! message;
          isSent = true;

        }

      } else if (randomGen == 2) {
        if (index(2).toInt - 1 >= 0) {
          arrActors(index(0).toInt)(index(1).toInt)(index(2).toInt - 1) ! message;
          isSent = true;
        }

      } else if (randomGen == 3) {
        if (index(0).toInt + 1 < numNodes) {
          arrActors(index(0).toInt + 1)(index(1).toInt)(index(2).toInt) ! message;
          isSent = true;
        }

      } else if (randomGen == 4) {

        if (index(1).toInt + 1 < numNodes) {
          arrActors(index(0).toInt)(index(1).toInt + 1)(index(2).toInt) ! message;
          isSent = true;
        }

      } else if (randomGen == 5) {
        if (index(2).toInt + 1 < numNodes) {
          arrActors(index(0).toInt)(index(1).toInt)(index(2).toInt + 1) ! message;
          isSent = true;
        }
      }
    }
  }
  
def updateCounters(s_m :BigDecimal , w_m : BigDecimal) = {
            var new_ratio =  (s + s_m)/(w + w_m);
            if(new_ratio - ratio > convergenceCounter || new_ratio -ratio < (-1)*convergenceCounter) {
                  convergenceCounter = 0;
            }else {
                convergenceCounter += 1;
            }
            println("convergenceCounter for  : " + self.path.name + " " + convergenceCounter.toString());
           if(convergenceCounter == 3 ) context.stop(self);       
  }
}

//entry point of the code.
object Master {

  def main(args: Array[String]) {

    //default values of the parameters.
    var numNodes = 5;
    var topology = "Line";
    var algorithm = "Gossip";

    if (args.length < 3) {
      println("invalid arguments");
      System.exit(0);
    }

    numNodes = Integer.parseInt(args(0));
    topology = args(1);
    algorithm = args(2);

    val system = ActorSystem("Master");
    topology match {
      case "line" => createLineTopology(numNodes, system,algorithm);
      case "Full" => createFullMeshTopology(numNodes, system,algorithm);
      case "3D" => create3DgridTopology(numNodes, system,algorithm);
      case "imp#D" => createImperfect3DgridTopology(numNodes,system,algorithm);
    }

  }

  
  /*
   * create a 3D grid and start sending a rumour for created topology
   */
  def create3DgridTopology(numNodes: Int, system: ActorSystem, algorithm : String) {
    var arrActors = Array.ofDim[ActorRef](numNodes, numNodes, numNodes);
    var counter = 0;
    for (i <- 1 to numNodes) {
      for (j <- 1 to numNodes) {
        for (k <- 1 to numNodes) {
          counter += 1;
          arrActors(i - 1)(j - 1)(k - 1) = system.actorOf(Props(new Workers3D(arrActors, numNodes,counter)), name = (i - 1).toString + "," + (j - 1).toString + "," + (k - 1).toString);
        }
      }
    }
    println("sending first message in 3D topology");
    arrActors(0)(0)(0) ! "3D_rumour_" + algorithm; //top left corner of the cuboid

  }

   
  /*
    * create a imperfect 3D grid topology and start a rumour
    */
  def createImperfect3DgridTopology(numNodes :Int , system : ActorSystem,algorithm : String){
          var arrActors: Array[ActorRef] = new Array[ActorRef](numNodes);
          //the imperfect 3D grid has 5 fixed neighbors and a one random neighbor.
          //the question is to fix which 5 ? and leave the other? and what if there are two links to one node?
          //for now we are avoiding multiple links to a node. and fixing [i-1][j][k] .... [i][j-1][k] 
          for (i <- 1 to numNodes) {
            for (j <- 1 to numNodes) {
              for (k <- 1 to numNodes) {
                                  //figure out the topology structure.
              }
            }
          }
  }
  
  /*
   * create a line topology and start seding a rumour for the created topology.
   */
  def createLineTopology(numNodes: Int, system: ActorSystem,algorithm : String) = {

    var arrActors: Array[ActorRef] = new Array[ActorRef](numNodes);
    for (i <- 1 to numNodes) {
      arrActors(i - 1) = system.actorOf(Props(new Workers(arrActors, numNodes)), name = (i - 1).toString);
    }
    println("Sending first message in line topology");
    arrActors(0) ! "Rumour_" + algorithm;

  }

  
  /*
   * create a full mesh topology and start sending a rumour for the created topology
   */
  def createFullMeshTopology(numNodes: Int, system: ActorSystem,algorithm : String) = {

    var arrActors: Array[ActorRef] = new Array[ActorRef](numNodes);
    for (i <- 1 to numNodes) {
      arrActors(i - 1) = system.actorOf(Props(new Workers(arrActors, numNodes)), name = (i - 1).toString);
    }
    println("Sending first message in Full mesh topology");
    arrActors(0) ! "Full_Mesh_Rumour_" + algorithm;

  }

}