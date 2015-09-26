import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import com.sun.org.apache.xpath.internal.operations.Bool
import scala.collection.immutable.HashMap

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
    
    //var i = index.toInt;
    val r = scala.util.Random;
    //var nextActorNumber = r.nextInt(numNodes);
    var actNum = r.nextInt(numNodes);
    while(actNum == actorNumber)
    {
      actNum = r.nextInt(numNodes);
    }
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

class Workers3D(arrActors: Array[Array[Array[ActorRef]]], numNodes: Int) extends Actor{
  
  var counter  = Array.ofDim[Int](numNodes, numNodes, numNodes);
        for(i <- 1 to numNodes)
        {
          for(j<- 1 to numNodes)
          {
            for(k <- 1 to numNodes)
            {
             counter(i-1)(j-1)(k-1) = 0;
            }
          }
        }

def receive = {
    case "3D rumour" =>
      println(self.path.name + " recieved a message");
      transmit3DGossip; 
    
  }

def transmit3DGossip = {
  
  var index = self.path.name.split(",");
  counter(index(0).toInt)(index(1).toInt)(index(2).toInt)+=1;
  if(counter(index(0).toInt)(index(1).toInt)(index(2).toInt) == 10)
  {
    println("Worker" + index.toString + "is stopping");
    context.stop(self);
  }
  val r = scala.util.Random;
  var randomGen = r.nextInt(6);
  val rangeStart = 0; val rangeEnd = numNodes -1;
 var isSent:Boolean = false;
  while(!isSent){
    
  randomGen = r.nextInt(6);
    
  if(randomGen == 0)
 {     
        if(index(0).toInt -1 >=0 ){
         arrActors(index(0).toInt-1)(index(1).toInt)(index(2).toInt)  ! "3D rumour";
         isSent = true;
        }
         
 }else if( randomGen == 1){
   if(index(1).toInt -1 >=0 ){
     arrActors(index(0).toInt)(index(1).toInt-1)(index(2).toInt)  ! "3D rumour";
      isSent = true;
      
        }
   
 }else if( randomGen ==2){
   if(index(2).toInt -1 >=0 ){
   arrActors(index(0).toInt)(index(1).toInt)(index(2).toInt-1)  ! "3D rumour";
   isSent = true;
        }
     
 }else if ( randomGen == 3){
   if(index(0).toInt +1 < numNodes ){
    arrActors(index(0).toInt+1)(index(1).toInt)(index(2).toInt)  ! "3D rumour";
    isSent = true;
        }
   
 }else if (randomGen == 4){
   
 if(index(1).toInt +1 < numNodes ){
       arrActors(index(0).toInt)(index(1).toInt+1)(index(2).toInt)  ! "3D rumour";
       isSent = true;
        }

   
 }else if(randomGen == 5){
   if(index(2).toInt +1 < numNodes ){
    arrActors(index(0).toInt)(index(1).toInt)(index(2).toInt+1)  ! "3D rumour";
    isSent = true;
        }
   
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
      case "3D"  =>  create3DgridTopology(numNodes, system);
    }

  }
  
  /*

   * create a 3D grid and start sending a rumour for created topology

   */

  def create3DgridTopology(numNodes: Int, system : ActorSystem){

        //a 3D grid of size numNodes X numNodes X numNodes 

        //neighbour of a particular node is   [i-][j][k] [i][j-1][k] [i][j][k-1] and the + counterparts of these. 

        var arrActors  = Array.ofDim[ActorRef](numNodes, numNodes, numNodes);
        for(i <- 1 to numNodes)
        {
          for(j<- 1 to numNodes)
          {
            for(k <- 1 to numNodes)
            {
              arrActors(i-1)(j-1)(k-1) = system.actorOf(Props(new Workers3D(arrActors , numNodes)) , name = (i-1).toString +","+ (j-1).toString + "," + (k-1).toString);
            }
          }
        }
        
    arrActors(0)(0)(0) ! "3D rumour";
        

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
