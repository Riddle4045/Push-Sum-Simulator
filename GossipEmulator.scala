import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import com.sun.org.apache.xpath.internal.operations.Bool

class Workers(arrActors: Array[ActorRef], numNodes: Int) extends Actor {

  //array keeping track of no. of messages each node has recieved.
  var counter: Array[Int] = new Array[Int](numNodes);
  for (i <- 1 to numNodes) {
    counter(i - 1) = 0;
  }

  def receive = {
    case "Rumour" =>
      println(self.path.name + " recieved a message");
      transmitGossip; 
    case "Full_Mesh_Rumour" =>
      println("received message from" + sender.path.name);
      transmitGossipInMesh;
  }

  //message dissemination in full mesh topology
  def transmitGossipInMesh = {
    var index = self.path.name;
    var actorNumber = index.toInt;
    counter(actorNumber) = counter(actorNumber) + 1;
    if (counter(actorNumber) == 10) {
      println("Worker" + actorNumber.toString + "is stopping");
      context.stop(self);
    }
    var i = index.toInt;
    val r = scala.util.Random;
    val actNum = r.nextInt(numNodes);
    arrActors(actNum) ! "Full_Mesh_Rumour";

  }

  //message dissemination in line topology
  def transmitGossip = {

    var index = self.path.name;
    var actorNumber = index.toInt;
    counter(actorNumber) = counter(actorNumber) + 1;
    if (counter(actorNumber) == 10) {
      println("Worker" + actorNumber.toString + "is stopping");
      context.stop(self);
    }

    if (index == "0") {
      arrActors(1) ! "Rumour";
    } else if (index == (arrActors.length - 1).toString) {
      arrActors(arrActors.length - 2) ! "Rumour";
    } else {
      var i = index.toInt;
      val r = scala.util.Random;
      if (r.nextBoolean()) {
        arrActors(i + 1) ! "Rumour";
      } else {
        arrActors(i - 1) ! "Rumour";
      }

    }
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
      case "line" => createLineTopology(numNodes, system);
      case "Full" => createFullMeshTopology(numNodes, system);
    }

  }

  /*
   * create a line topology and start seding a rumour for the created topology.
   */
  def createLineTopology(numNodes: Int, system: ActorSystem) = {

    var arrActors: Array[ActorRef] = new Array[ActorRef](numNodes);
    for (i <- 1 to numNodes) {
      arrActors(i - 1) = system.actorOf(Props(new Workers(arrActors, numNodes)), name = (i - 1).toString);
    }
    println("Sending first message in line topology");
    arrActors(0) ! "Rumour";

  }
  /*
   * create a full mesh topology and start sending a rumour for the created topology
   */
  def createFullMeshTopology(numNodes: Int, system: ActorSystem) = {

    var arrActors: Array[ActorRef] = new Array[ActorRef](numNodes);
    for (i <- 1 to numNodes) {
      arrActors(i - 1) = system.actorOf(Props(new Workers(arrActors, numNodes)), name = (i - 1).toString);
    }
    println("Sending first message in Full mesh topology");
    arrActors(0) ! "Full_Mesh_Rumour";

  }

}