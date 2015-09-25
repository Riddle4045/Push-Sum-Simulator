import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import com.sun.org.apache.xpath.internal.operations.Bool

class Workers(arrActors : Array[ActorRef],numNodes : Int) extends Actor{
  
  var ctr : Array[Int] = new Array[Int](numNodes);
  for(i <- 1 to numNodes){
    
   ctr(i-1)=0;
    
  }
  
  def receive = {
    
    case "You know what?" => println("received message from"+sender.path.name);
     transmitGossip;
    case "x" => println("Hi!");
    case "Hey There!" => println("received message from"+sender.path.name);
     transmitGossipInMesh;
    
  }
  
  def transmitGossipInMesh = {
    var index = self.path.name;
    var actorNumber = index.toInt;
    ctr(actorNumber) = ctr(actorNumber)+1;
    if(ctr(actorNumber) == 10)
    {
      println("Worker"+actorNumber.toString+"is stopping");
      context.stop(self);
    }
    var i = index.toInt;
    val r = scala.util.Random;
    val actNum = r.nextInt(numNodes);
    arrActors(actNum) ! "Hey There!";
    
    
  }
  
  def transmitGossip = {
    
    
    var index = self.path.name;
    var actorNumber = index.toInt;
    ctr(actorNumber) = ctr(actorNumber)+1;
    if(ctr(actorNumber) == 10)
    {
      println("Worker"+actorNumber.toString+"is stopping");
      context.stop(self);
    }
    
    
    if(index == "0")
    {
    
      arrActors(1) ! "You know what?";
    }
    else if(index == (arrActors.length-1).toString){
      
     
      
      arrActors(arrActors.length-2) ! "You know what?";
    }
    else
    {
      var i = index.toInt;
      val r = scala.util.Random;
     // r.nextBoolean();
      if(r.nextBoolean())
      {
       
        arrActors(i+1) ! "You know what?";
      }
      else{
   
        arrActors(i-1) ! "You know what?";
      }
        
    }
  }
  
  //Define a function to randomly transfers a message to its neighbors.
  //Define a case for the message transmitted
  //Set a counter for the number of msgs received. when 10, stop the actor.
}
 
object Master {
  
 def main(args: Array[String]){
   
   var numNodes = 5;
   var topology = "Line";
   var algorithm = "Gossip";
   if(args.length<3) {
     println("invalid arguments");
     System.exit(0);
   }
  
      numNodes = Integer.parseInt(args(0));
      topology = args(1);
      algorithm = args(2);
      
      println(numNodes);
      println(topology);
      println(algorithm);
      
      
    
     
     val system = ActorSystem("Master");
     topology match {
       case "Line" => createLineTopology(numNodes,system);
       case "Full" => createFullMeshTopology(numNodes,system);
     }

     
     
   
 }
   def createLineTopology(numNodes : Int , system : ActorSystem) = {
       
     var arrActors : Array[ActorRef] = new Array[ActorRef](numNodes);
     for(i <- 1 to numNodes){
       
       arrActors(i-1) =  system.actorOf(Props(new Workers(arrActors , numNodes)), name = (i-1).toString) ;
       
       
       
     }
     println("Sending first message");
     arrActors(0) ! "You know what?";
     
 
     
       
       
     }
   def createFullMeshTopology(numNodes: Int, system : ActorSystem) = {
     
     var arrActors : Array[ActorRef] = new Array[ActorRef](numNodes);
     for(i <- 1 to numNodes){
       
       arrActors(i-1) =  system.actorOf(Props(new Workers(arrActors , numNodes)), name = (i-1).toString) ;
       
       
       
     }
     println("Sending first message");
     arrActors(0) ! "Hey There!";
     
   }
 
}
