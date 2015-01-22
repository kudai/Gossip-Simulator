package gossip
import akka.actor.Actor
import akka.actor.Actor._
import akka.actor.Props
import akka.actor.ActorSystem
import scala.util.Random
import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer
import akka.actor.ActorRef
import akka.actor.ActorSelection.toScala
import akka.actor.actorRef2Scala

object networkTopology {
  def main(args: Array[String]){
	  if(args.length != 3){
	    println("Incorrect number of input parameters.")
	  }
	  else {
	    val nTContext = ActorSystem("SkyNet")
		val bigBoss = nTContext.actorOf(Props(new masterClass(args(0).toInt,args(1),args(2))),"master")
	  }
	}
}

class masterClass(nNodes:Int, netTop:String, algo:String) extends Actor {

	var n = nNodes
	var actualN = n
	val nodeContext = ActorSystem("SkyNet")
	var childDoneState = Array.fill[Boolean](n)(false)
	var startTime:Long = 0
	var nKill = 50
    for (i<- 1 to n){
      var msg:String="init"
      val node = nodeContext.actorOf(Props(new nodeClass),"Node"+ i.toString)
      node ! msg
    }
	netTop.toLowerCase() match {
	  case "line" => 
	    for(i <- 1 to n){
		  var msg:String="l"+i.toString +","
		  val recieverNode = nodeContext.actorSelection("/user/Node"+ i)
		  if(i-1 > 0) 
			  msg = msg + (i-1).toString +","
		  if(i < n)
			  msg = msg + (i+1).toString 
		  println(recieverNode+" : "+msg)
		  recieverNode ! msg
	    }
	    
	  case "full" =>
	    for(i <- 1 to n){
	      var msg:String="f"+n.toString
		  val recieverNode = nodeContext.actorSelection("/user/Node"+ i) 
		  msg = msg + ","+i.toString
		  println(recieverNode+" : "+msg)
		  recieverNode ! msg
	    }
	    
	  case "2d" =>
	    val length = math.sqrt(n).floor.toInt
	    actualN = math.pow(length , 2).toInt
	    for(i <- 1 to actualN){
	      var msg:String="2"+i.toString
		  val recieverNode = nodeContext.actorSelection("/user/Node"+ i) 
		  if(i-length > 0) msg = msg + "," + (i-length).toString  //Upper row check
		  if(i+length <= actualN) msg = msg + "," + (i+length).toString  //lower row check
		  if(i % length == 0) msg = msg + "," + (i-1).toString // right column check
		  else if (i % length == 1) msg = msg + "," + (i+1).toString // left column check
		  else msg = msg + "," + (i-1).toString + "," + (i+1).toString // middle of the row
		  println(recieverNode+" : "+msg)
		  recieverNode ! msg
	    }
	    
	    case "imperfect2d" =>
		    val length = math.sqrt(n) .floor.toInt
		    actualN = math.pow(length, 2).toInt
		    for(i <- 1 to actualN){
			  var msg:String="m"+i.toString
			  val recieverNode = nodeContext.actorSelection("/user/Node"+ i) 
			  if(i-length > 0) msg = msg + "," + (i-length).toString  //Upper row check
			  if(i+length <= actualN) msg = msg + "," + (i+length).toString  //lower row check
			  if(i % length == 0) msg = msg + "," + (i-1).toString // right column check
			  else if (i % length == 1) msg = msg + "," + (i+1).toString // left column check
			  else msg = msg + "," + (i-1).toString + "," + (i+1).toString // middle of the row
			  var randomNum = Int.MaxValue
			  while (randomNum > actualN || randomNum == 0) {
				  randomNum = (Random.nextInt % (actualN+1)).abs }
			  msg = msg + "," + randomNum.toString    //Random node addition
			  println(recieverNode+" : "+msg)
			  recieverNode ! msg
		    }
	}
	println("Network Topology Built.")
	println("________________________________________________________________________________")
	algo.toLowerCase() match {
	  case "gossip"=>
	    val msg:String="g"+" There's no best way to do anything. There's only compromises."
	    var randomNum = Int.MaxValue
	    while (randomNum > n || randomNum == 0) {
		  randomNum = (Random.nextInt % n).abs
	    }
	    val recieverNode = nodeContext.actorSelection("/user/Node"+ randomNum)
	    recieverNode ! msg
	  case "push-sum"=>
	    val msg:String="p"+0.0.toString + "," + 0.0.toString
	    var randomNum = Int.MaxValue
	    while (randomNum > n || randomNum == 0) {
		  randomNum = (Random.nextInt % n).abs
	    }
	    val recieverNode = nodeContext.actorSelection("/user/Node"+ randomNum)
	    recieverNode ! msg
	  case whatever =>
	    println("Uncomprehensable algorithm: "+whatever+". Please ask my creators to teach me this ^_^")
	}
	
	startTime = System.currentTimeMillis()
	
	def receive = {
		case l:String =>
		  l.toLowerCase().head match {
		    case 'd' => //done
		        var revMsg = l.tail.split(",")
		        childDoneState.update((revMsg(0).toInt-1), true)
		        var childCount =0;
		        for (x <- childDoneState)
		          if(x == true) childCount += 1
		        println("Time taken: " + (System.currentTimeMillis() - startTime).toString+"ms")
		        if (revMsg.size <= 1){
		        var percentCovered = (childCount*100.0/actualN)
			        println("Percentage complete: "+percentCovered.toString)
			        if(percentCovered == 100.0) {
			          context.children.foreach(context.stop(_))
			          System.exit(0)
			        }
		        }
		        else {
		        	println("Final Ratio: "+revMsg(1))
		        	context.children.foreach(context.stop(_))
			        System.exit(0)
		        }
		         case 'z'=>//wake and kill some nodes //---->New Code <------
		         while(nKill > 0) {
		        	 var rndInt = Int.MaxValue
		        			 while (rndInt > n || rndInt == 0) {
		        				 rndInt = (Random.nextInt % n).abs
		        			 }
		        	 println("Killing Node "+rndInt)
		        	 nKill = nKill -1
		        	 val targetChild = nodeContext.actorSelection("/user/Node"+ rndInt)
		        			 targetChild ! "j" //because joker is awesome at random killing
		         }
		         case 'x'=> //some child node dies
		         for(elem <- context.children) elem ! "a"+l.tail 
		    case 'e' => //Error
		        println("Error in Reciever. Terminating!")
		        context.children.foreach(context.stop(_))
		        println("Stopped all nodes, now exiting!")
		        System.exit(1)
		    case whatever =>
		        println("Master got this "+whatever)
		  }
    }
}

class nodeClass extends Actor {
  import context._
  var callerNode:ActorRef = null
  var myCount =0
  var gossipMode = true
  var rumor =""
  var s=0.0; var w=1.0; var ratio = 0.0; var ratio_old =999.0; var dratio = 999.0;
  var conseqRatioCount = 0
  var connections = ArrayBuffer[String]()
  var exconnections = ArrayBuffer[String]()
  def transmitMsg = {
	  val msg = if(gossipMode == true)"rt"+rumor else "rf,"+s.toString+","+w.toString
	  val len = connections.length
	  var randomNum = Int.MaxValue
	  while (randomNum > len || randomNum == 0) {
		  randomNum = (Random.nextInt % len).abs
		   if(exconnections.length > 0){ 
			   for(elem <- exconnections) {
				   if(randomNum == elem.toInt) randomNum = 0
			   }
		   }
	  }
	  val targetBro = context.actorSelection("/user/Node"+connections(randomNum))
	  targetBro ! msg
  }
  def receive = {
    case l:String =>
      l.head match{
        case 'i' =>
          callerNode = sender
        case 'l' =>
          connections ++= l.tail.split(",")
        case 'r' =>
          l.tail.head match {
            case 't'=> gossipMode = true
            case 'f'=> gossipMode = false
          }
          if (gossipMode == true && myCount< 10) {
	          myCount +=1
	          rumor = l.tail.tail
	          var sendmsg = "d"
	          if(connections.length > 0)
	          sendmsg += connections(0);
	          else
        	  	callerNode ! sendmsg;
	          callerNode ! "z"
	          transmitMsg
          }
          //need re-transmission
		  if(gossipMode == true && myCount <9) {
		    val dur = Duration.create(50, scala.concurrent.duration.MILLISECONDS);
		    val me = context.self
		    context.system.scheduler.scheduleOnce(dur, me, "z")
		  }
          else if (myCount == 10) {
        	  myCount+=1;
        	  val sendmsg = "d"+connections(0);
        	  callerNode ! sendmsg;
        	  //println(context.self +" is done.")
          }
          else if (gossipMode == false) {
        	  if (s == 0.0) {
        		  s = connections(0).toDouble
        	  }
        	  var inParams = l.tail.tail.split(",")
        	  s = (s+inParams(1).toDouble)/2
        	  w = (w+inParams(2).toDouble)/2
        	  ratio_old = ratio
        	  ratio = s/w
        	  println(self+": "+ratio.toString)
        	  dratio = (ratio-ratio_old).abs
        	  if(dratio < 0.0000000001) conseqRatioCount += 1
        	  else conseqRatioCount = 0
        	  if(conseqRatioCount == 2) {
        		  val sendmsg = "d"+connections(0)+","+ratio.toString;
        		  callerNode ! sendmsg;
        	  }
        	  else if (conseqRatioCount < 3) {
        		  transmitMsg
        	  }
        	  else if (conseqRatioCount > 3) {
        		  println("Terminated actor is getting messages")
        	  }
          }
          
        case 'f' =>
          val input = l.tail.split(",")
          val numNodes = input(0).toInt
          val myNumber = input(1)
          for (i <- 1 to numNodes)
            connections += i.toString
          connections.update(0, myNumber)
          connections.update((myNumber.toInt - 1), 1.toString)
        case '2' =>
          connections ++= l.tail.split(",")
        case 'm' =>
          connections ++= l.tail.split(",")
        case 'o' =>
          connections ++= l.tail.split(",")
        case 'z'=>
          transmitMsg
        case 'g'=>
          gossipMode = true
          rumor = l.tail
          val sendmsg = "d"+connections(0);
        	  callerNode ! sendmsg;
          transmitMsg
        case 'p'=>
          gossipMode = false
          s = connections(0).toDouble
          ratio = s/w
          dratio = (ratio_old-ratio).abs
          transmitMsg
        case 'c'=> // node is deleted update coming in from bossGuy ---->New Code <------
          exconnections ++= l.tail.split(",")
          println("Killed: "+l.tail.split(","))
        case 'j'=> //parent is killing you for testing node failure analysis
          println("Killed: "+connections(0))
          callerNode ! "x"+connections(0)
          context.stop(self) 
        case whatever => {println("error in Joe, got this: "+whatever); sender ! "error"}
      }
  }
}