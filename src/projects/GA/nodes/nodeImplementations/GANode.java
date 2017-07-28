package projects.GA.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import projects.GA.CustomGlobal;
import projects.GA.nodes.messages.BorderMessage;
import projects.GA.nodes.messages.DeadNodeMessage;
import projects.GA.nodes.messages.MCIMessage;
import projects.GA.nodes.messages.MSGTREE;
import projects.GA.nodes.messages.RemoveMessage;
import projects.GA.nodes.messages.RequestRouteMessage;
import projects.GA.nodes.messages.SetRepairRouteMessage;
import projects.GA.nodes.messages.GADataMessage;
import projects.GA.nodes.messages.SetRouteMessage;
import projects.GA.nodes.timers.BorderCollectInformationTimer;
import projects.GA.nodes.timers.EndSPTTimer;
import projects.GA.nodes.timers.EventEndGATimer;
import projects.GA.nodes.timers.EventSPTTimer;
import projects.GA.nodes.timers.MessageSPTTimer;
import projects.GA.nodes.timers.MessageTimer;
import projects.GA.nodes.timers.RepairDeadNodeGATimer;
import projects.GA.nodes.timers.RepairDeadNodeTimer;
import projects.GA.nodes.timers.GATimer;
import projects.GA.nodes.timers.SetRouteTimer;
import projects.GA.nodes.timers.VerifyBorderNodeTimer;
import projects.GA.thread.ExecuteAG;
import projects.GA.utils.GATree;
import projects.GA.utils.NEATOWriter;
import projects.GA.utils.PrintResult;
import projects.GA.utils.Vertex;
import projects.GA.utils.STPWriter;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.eps.Exporter;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.models.EnergyModel.simple.SimpleEnergy;
import sinalgo.nodes.Node;
import sinalgo.nodes.TimerCollection;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.UniformDistribution;

public class GANode extends Node {
//	private HashMap<EventKey, RoutingTableEntry> routingtable = new HashMap<EventKey, RoutingTableEntry>();
	private int HopToSink = 100000; // Distance in Hops to Sink. DON'T RELY ON IT TO MAKE THE ROUTING. IT INCREASES THE OVERHEAD
	private int NextHopSink = 100000; // My Relay of Data
	private int ownerID = this.ID; // My OwnerID
	private boolean senddata = false; //
	private int eventnum = 0; //
	public int getEventnum() {
		return eventnum;
	}

	public void setEventnum(int eventnum) {
		this.eventnum = eventnum;
	}

	private int Disttree = 0;
	private boolean rota = false;
	private double nextsenddata = 0;
	private int Disttreerecv = 0;
	private boolean sentmci = false;
	private boolean sentBorderBack = false;

	public static int EventsAmount = 0;
	public static int EventsTimes = 0;
	public static int EventSize = 0;
	public static int CommunicationRadius = 0;
	public static int DropRate = 0;
	public static int Recivers = 0;						//Number of packets received by the sink
	public static int Detects = 0;
	public static int Overheads = 0;
	public static int DataPackets = 0;
	public static int Edges = 0;
	public static int DataRate = 0;
	public static int Density = 0;
	
	public int warningMessages = 0;
	public static int populations = 0;
	public static int generations = 0;
	public static int preprocessing = 1;
	public static int objFunction = 1;
	public static double fFactor = 1;
	public static double kFactor = 1;
	public static double bVariation = 0.05;
	
	public static int Notifications = 0;
	public static double SimulationTime = 0;
	public static Logging Energy = Logging.getLogger( "GAEnergy.txt", true );
	public static Logging debugLog = Logging.getLogger();	//Prints to the console
	//public double battery = 0.0;
	private SimpleEnergy battery;
	public List<Integer> previous = new ArrayList<Integer>();
	// private IEnergy bateria;

	private UniformDistribution uniformRandom = new UniformDistribution( 0, 0.016 );
	// Logging sptLog;
	public static Logging GA = Logging.getLogger( "GALog.txt", true );

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
	}

	private enum Status {
		MONITORING,
		READY
	};

	public enum Roles {
		SINK,
		COLLABORATOR,
		RELAY,
		LEAFNODE
	};

	public enum TNO {
		TREE,
		MONITORING,
		SCHEDULE_FEEDBACK,
		TNO_FEEDBACK
	};

	private GATimer timerTREE;
	private Roles myRole;
	public Roles getMyRole() {
		return myRole;
	}

	public void setMyRole(Roles myRole) {
		this.myRole = myRole;
	}

	private Status mystatus;
	private ArrayList<Integer> son = new ArrayList<Integer>();
	double eventEndTime = 0;
	private ArrayList<Integer> filhos = new ArrayList<Integer>();
	private ArrayList<Integer> filhossend = new ArrayList<Integer>();
	private ArrayList<Integer> filhosrecv = new ArrayList<Integer>();

	private static int enviados = 0;

	/* Captura dos vizinhos e Feedback */
	private LinkedList<Integer> listAll = new LinkedList<Integer>();	//A list containing the IDs of ALL the neighbor nodes, 1-indexed
	//private Map<Node, LinkedList<Integer>> mapNeibohrs = new HashMap<Node, LinkedList<Integer>>();
	public static HashMap<Node, LinkedList<Integer>> mapVizinhosLocal = new HashMap<Node, LinkedList<Integer>>();
	private boolean reached = false;
	private int childrenNodes = 0;
	private int receivedNodes = 0;
	private int data = this.ID;
	public int generateEvent = 0;
	public int cont = 0;
	//public int numberOfNodes;
	public int idMsg = 0;

	private static boolean checkConnectivity = true;
	// public static List<String> listTree = new ArrayList<String>();
	
	private ArrayList<Integer> ReceivedNeighborHopBigger = new ArrayList<Integer>();
	private ArrayList<Integer> NeighborHopBigger = new ArrayList<Integer>();
	//public static int[][] AdjMatrix ;
	public static List<Integer>[] AdjList; 		// Adjacency list 0-indexed
	public static ArrayList<Integer> UpNodes = new ArrayList<>(); 	//1: node awaken, 0: dead node 0-indexed
	public static ArrayList<Integer> DisconnectedNodes = new ArrayList<>(); 	//1: node connected, 0: disconnected 0-indexed
	//public static ArrayList< Vertex > nodes = new ArrayList<Vertex>();		
	public static HashMap<Integer, Vertex> nodes = new HashMap<>();	//Nodes as seen by the sink (1-indexed), note that they may no have the real battery values due to some synchronization, however the real values don't differ significantly
	public static List<String> edges = new ArrayList<String>();		//Edges as seen by the sink
//	static Thread threadGA = new Thread( new ExecuteAG((GANode)Tools.getNodeByID(1)));					//Thread executing the BRKGA algorithm
	static Thread threadGA = null;					//Thread executing the BRKGA algorithm
	static ExecuteAG executeAG = null;				//Class implementing runnable 
	public static ArrayList<Integer> terminals = new ArrayList<Integer>();		//List of terminal nodes, 0-indexed
	private boolean isDead = false ;
	
	private int numTree = 0;
	private int isBorderNode = 0; //0: unknown, 1: is border node, 2: is NOT border node
	
	public ArrayList<Integer> node1 = new ArrayList<Integer>();
	public ArrayList<Integer> node2 = new ArrayList<Integer>();

	public boolean requestedRoute = false;

	public static int numSPTFiles = 0;
	private boolean runUpdateAdjacencyMatrix = false;
	
	@Override
	public void handleMessages( Inbox inbox ) {
		// TODO Auto-generated method stub
		int sender;
		
		if ( GANode.UpNodes.get(this.ID-1)== 0 )	// The nodes just process the packets as long as they are awake (have energy) 
			return;
		
		while ( inbox.hasNext() ) {
			Message msg = inbox.next();
			sender = inbox.getSender().ID;

			// Sink start the flooding of message MCI for configuration initial
			if ( msg instanceof MCIMessage ) {
				InitialConfiguration(msg);
				//InitialConfiguration2(msg);
			}

			if ( msg instanceof MSGTREE ) { //I guess this is not necessary since the sink has the last computed routing tree.
				GetRoutingTree(msg);
			}

			// Sent information about event to Sink
			if ( msg instanceof GADataMessage ) {
				SendDataEvent(msg, sender);
			}

			if (msg instanceof BorderMessage){
				CollectInformation(msg);
			}
			
			if (msg instanceof RequestRouteMessage){
				RequestRoute(msg);
			}
			
			if (msg instanceof SetRouteMessage){
				SetRoute(msg);
			}
			
			if (msg instanceof RemoveMessage ){	//Remove node due to a finished event.
				Remove(msg);
			}
			
			if (msg instanceof DeadNodeMessage){		//Message sent from the dead node to the sink
				ReportDeadNode(msg);
			}
			
			if (msg instanceof SetRepairRouteMessage){	//Message to set repair (fix) the route of the nodes that became orphan
				SetRepairRoute(msg);
			}
		}
	}

	public static void debugMsg(String msg){
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if ( stackTraceElements.length >=2 ){
			StackTraceElement ste = stackTraceElements[2];
			debugLog.log("[ " + String.format("%06.6f", Global.currentTime) + " ] " + ste.getMethodName() + ": " + msg + "\n");
		}
		else
			debugLog.log("[ ERROR LOGGING ] ############################## the stacktrace is too small ############################## ");
	}
	
	public static void debugMsg(String msg, int bold){
		String prefix = null;
		if ( bold == 1){
			prefix = "###";
		} else if ( bold == 2){
			prefix = "######";
		}else{
			prefix = "";
		}
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		if ( stackTraceElements.length >=2 ){
			StackTraceElement ste = stackTraceElements[2];
			debugLog.log(prefix + " [ " + String.format("%06.6f", Global.currentTime) + " ] " + ste.getMethodName() + ": " + msg + "\n");
		}
		else
			debugLog.log("[ ERROR LOGGING ] ############################## the stacktrace is too small ############################## ");
	}

	/*private void InitialConfiguration2(Message msg) {
		MCIMessage mcimsg = (MCIMessage) msg;
		battery.spend(EnergyMode.RECEIVE);
		
		if(mcimsg.HopToSink < this.HopToSink){
			//Se não tiver o ID do nó tabela então adiciona o ID do nó na tabela
			if (!(routingtable.containsKey(new EventKey(mcimsg.SenderID,1) ) )){
				routingtable.put(new EventKey(mcimsg.SenderID,1), new RoutingTableEntry(mcimsg.Energy,this.HopToEvent,mcimsg.HopToSink,mcimsg.SenderID));
			}else if (routingtable.get(new EventKey(mcimsg.SenderID,1)).HopsToSink >  mcimsg.HopToSink ){
				routingtable.put(new EventKey(mcimsg.SenderID,1), new RoutingTableEntry(mcimsg.Energy,this.HopToEvent,mcimsg.HopToSink,routingtable.get(mcimsg.SenderID).NextHop));
			}
			//Setar o nexthop
			SetNextHop();

			if(!this.sentmci){
				//MessageLeoTimer mciTimer = new MessageLeoTimer((new MCIMessage(this.HopToSink,this.ID, this.Energy, "RELAY")));
				MessageLeoTimer mciTimer = new MessageLeoTimer((new MCIMessage(this.HopToSink,this.ID)));
				//double time = new UniformDistribution(1,1.2).nextSample();
				mciTimer.startRelative(this.HopToSink+1,this);
				//this.Energy = this.Energy - ConsumptionInTransmission;
				getBattery().spend(EnergyMode.SEND);
				this.sentmci = true;
				Overheads = Overheads + 1;
				
				this.setColor(Color.GRAY);
		

			}
			
			if(isBorderNode==0){
				CIMTimer cimTimer = new CIMTimer(this);
				cimTimer.startAbsolute(1000,this);
			}
			
		}else if(mcimsg.HopToSink > this.HopToSink){
			NeighborHopBigger.add(mcimsg.SenderID);
			isBorderNode = 2;
		}
	}*/
	
	/*public void SetNextHop(){
    	//Iterator<Integer> it = routingtable.keySet().iterator();
    	Iterator<EventKey> it = routingtable.keySet().iterator();
		EventKey candidato = it.next(); 
		EventKey Key;
		
		while(it.hasNext()){
			Key = it.next();
			
			if ( (routingtable.get(candidato).HopsToSink > routingtable.get(Key).HopsToSink) && (this.ID != routingtable.get(Key).NextHop) ){
				candidato = Key;
				
			}else if( (routingtable.get(candidato).HopsToSink == routingtable.get(Key).HopsToSink)
					&&((routingtable.get(candidato).Energy < (routingtable.get(Key).Energy)-5) ) &&
					(this.ID != routingtable.get(Key).NextHop) ){
						candidato = Key;
			}	
		}
		this.NextHop =  candidato.ID;
		this.HopToSink = routingtable.get(candidato).HopsToSink+1;
    }*/

	/**
	 * Method the processes the removemessage
	 * @param msg
	 */
	private void Remove(Message msg) {
		RemoveMessage removeMessage = (RemoveMessage)msg;
		if (this.ID==1){//sink
			//int nodeToRemove = removeMessage.getNodeID();
			int nodeToRemove = removeMessage.getNodeID()-1;
			Tools.appendToOutput("Node to remove: " + (nodeToRemove+1) + "\n");
			Tools.appendToOutput("Terminal nodes: " + terminals + "\n");
			if (terminals.contains(nodeToRemove)){
				int nodeindex = terminals.indexOf(nodeToRemove);
				terminals.remove(nodeindex);
				runGAAlgorithm(nodeToRemove+1, 2, new ArrayList<Integer>());
			}
		}else{
			send(removeMessage, Tools.getNodeByID(this.NextHopSink));
			battery.spend(EnergyMode.SEND);
			Overheads+=1;
		}
		
	}

	/**
	 * Method to process the DeadNodeMessage. The sink udpates the UpNodes array and runs the algorithm so that now the node isn't
	 * considered anymore. The relay nodes just retransmit it to the sink;
	 * @param msg
	 */
	private void ReportDeadNode(Message msg) {
		DeadNodeMessage dnm = (DeadNodeMessage)msg;
		battery.spend(EnergyMode.LISTEN);
		if (this.ID==1) {//Sink
			debugMsg(">>> SINK recieved ReportDeadMessage from node " + dnm.getNodeID() + " reported itself as dead with battery of " + dnm.getReportedEnergy(), 2);
			//debugMsg(">>> GANode.UpNodes " + GANode.UpNodes.get(dnm.getNodeID() - 1));
			GANode.UpNodes.set(dnm.getNodeID() - 1, 0);
			GANode.DisconnectedNodes.set(dnm.getNodeID()-1, 0);
			
			updateConnectedComponents(dnm.getNeighbors());		//Updates the connected components and fixes, if possible, the neighbors of the dead node so they don't lose connectivity.
			
			runGAAlgorithm(dnm.getNodeID(), 4, null);
			
			//debugMsg(">>> GANode.UpNodes " + GANode.UpNodes.get(dnm.getNodeID() - 1));
			int dn = 0;
			for ( Integer i : DisconnectedNodes )
				dn += i;
			debugMsg(">>> SINK number of connected nodes " + dn);
			
		}else{
			send(dnm, Tools.getNodeByID(this.NextHopSink));
			battery.spend(EnergyMode.SEND);
			Overheads+=1;
			debugMsg("node " + this.ID + " retransmitting DeadNodeMessage for node " + dnm.getNodeID() + " list of reconfiguring nodes " + dnm.getNeighbors());
		}
	}

	/**
	 * Method to process the SetRepairRouteMessage. This message is generated in the sink and 
	 * retransmited trough the path until reaching the last node.
	 * @param msg
	 */
	private void SetRepairRoute(Message msg) {
		SetRepairRouteMessage srrm = (SetRepairRouteMessage)msg;
		debugMsg("))SetRepairRouteMessage rcvd by " + this.ID + " path " + srrm.getPath());
		debugMsg(")))" + this.NextHopSink + " " + srrm.getNextHop() + ", " + srrm.getHopsToSink());
		this.NextHopSink = srrm.getNextHop();
		this.HopToSink = srrm.getHopsToSink();
//		if ( this.ID == ((GANode)Tools.getNodeByID(this.NextHopSink)).NextHopSink )
//			debugMsg("wdf!");
		if (srrm.getPath().size() > 0){
//			if ( this.NextHopSink == this.ID ){
//				debugMsg("wdf!");
//			}
			srrm.setNextHop(this.ID);
			srrm.setHopsToSink(srrm.getHopsToSink()+1);
			//send(srrm, Tools.getNodeByID(srrm.top()));
			int destiny = srrm.top();
			RepairDeadNodeTimer rdnt = new RepairDeadNodeTimer(Tools.getNodeByID(this.ID), srrm, Tools.getNodeByID(destiny));
			rdnt.startRelative(0.0001, Tools.getNodeByID(this.ID));
			
		}
	}

	/**
	 * Collects information (the whole network representation as a graph) starting from the border
	 * nodes. Since we are not considering a mobility model the edges will remain the same in time.
	 * The only varying parameter is the battery energy. The DataMessage should also carry the nodes
	 * that retransmitted the message so that the sink updates their battery use (assuming the use
	 * per transmission is always the same, which in reality DOES NOT happen). A more real approach
	 * would be to send a EnergyReportMessage to the sink by each node to report its real battery.
	 * These two approaches should be contrasted to know which one is better. For the moment, since
	 * the GA does not consider battery use, this is not implemented!
	 * FIXME: not implementing battery methods
	 * @param msg
	 */
	private void CollectInformation(Message msg) {
		BorderMessage borderMessage = (BorderMessage)msg;
		if (this.isBorderNode==1)
    		return;
    	
    	if(NeighborHopBigger.contains(borderMessage.getSenderID())){
    		battery.spend(EnergyMode.RECEIVE);
    		
    		//System.out.println("BorderRecv #"+this.ID);
    		
    		if (!ReceivedNeighborHopBigger.contains(borderMessage.getSenderID()))
    			ReceivedNeighborHopBigger.add((int)borderMessage.getSenderID());
    		
//    		borderMessage.addEdge(borderMessage.getSenderID(), this.ID);
    		
    		for (int i=0; i<this.listAll.size(); i++)
    			borderMessage.addEdge(this.ID, this.listAll.get(i));
    		//node1.add(CIMmsg.getSenderID());
    		//node2.add(this.ID);
    		setColor(Color.MAGENTA);
//    		System.out.println("%%%" + borderMessage.toString());
    		
    		/*Tools.appendToOutput("====\n");
    		Tools.appendToOutput("u: " + borderMessage.node1 + "\n");
			Tools.appendToOutput("v: " + borderMessage.node2 + "\n");
			Tools.appendToOutput("====\n");*/
    		
    		//Verify if the node has received all the CIM messages from its neighbors with higher hop level
    		// If setA==setB, then A includes B and B includes A.
    		List<Integer> setA = new ArrayList<Integer>(NeighborHopBigger);
    		List<Integer> setB = new ArrayList<Integer>(ReceivedNeighborHopBigger);
    		setA.removeAll(ReceivedNeighborHopBigger);
    		setB.removeAll(NeighborHopBigger);
    		if (setA.size() == 0 && setB.size()==0){
    			if (this.myRole == Roles.SINK){
    				//The sink udpates the adjacency matrix
    				if (this.node1!=null && this.node2!=null){
//    					borderMessage.node1.addAll(this.node1);
//    					borderMessage.node2.addAll(this.node2);
    					borderMessage.addEdges(this.node1, this.node2);
    				}
    				createAdjacencyMatrix(borderMessage);
        		}
    			else{
    				borderMessage.setSenderID(this.ID);
    				if (this.node1!=null && this.node2!=null){
//    					borderMessage.node1.addAll(this.node1);
//    					borderMessage.node2.addAll(this.node2);
    					borderMessage.addEdges(this.node1, this.node2);
    				}
    				setColor(Color.PINK);
    				
    				/*CIMmsg.node1.clear();
    				CIMmsg.node2.clear();
    				CIMmsg.node1.addAll(node1);
    				CIMmsg.node2.addAll(node2);*/
    				Overheads+=1;
    				/*BorderMessage bm = new BorderMessage(this.ID);
    				bm.addEdge(borderMessage.getSenderID(), this.ID);
    				bm.node1.addAll(this.node1);
    				bm.node2.addAll(this.node2);*/
    				//broadcast(bm);
    				//MessageSPTTimer borderTimer = new MessageSPTTimer( bm );
    				MessageSPTTimer borderTimer = new MessageSPTTimer( borderMessage );
    				borderTimer.startRelative(this.HopToSink+0.1,this);
    				//System.out.println("BorderSent #"+this.ID);
    				//System.out.println("");
    				Tools.appendToOutput("CollectInformation() BorderMessage Overheads: " + this.Overheads + "\n");
    				//send(borderMessage, Tools.getNodeByID(this.NextHopSink));
    				
    				sentBorderBack = true;
    				battery.spend(EnergyMode.SEND);
    			}
    		}else{
//    			this.node1.addAll(borderMessage.node1);
//    			this.node2.addAll(borderMessage.node2);
    			this.node1.addAll(borderMessage.getNode1());
    			this.node2.addAll(borderMessage.getNode2());
    		}
    	}
    	//borderMessage = null; //To help the GC to discard this object
    	//if (!sentBorderBack && this.myRole!=Roles.SINK){
    		
    		//BorderInitNodeTimer borderTimer = new BorderInitNodeTimer(this);
			//borderTimer.startRelative(0.001,this);
    	//}
    	
    	
    	
	}
	
	/**
	 * A node that detected an event request for an optimal route to the sink. It waits for a preset
	 * time (1 second) before sending its data. The node does not receive any kind of feedback to 
	 * know if the sink processed its request; it just keeps sending its data to the nextHopSink
	 * @param msg
	 */
	private void RequestRoute(Message msg) {
		RequestRouteMessage requestRouteMessage = (RequestRouteMessage)msg;
		battery.spend(EnergyMode.RECEIVE);
		if (this.myRole == Roles.SINK){//this is the SINK, so run the algorithm an send the SetRouteMessage back!
			//print the file
			requestRouteMessage.add(this.ID);
//			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
//			System.out.println("RequestRoute(): path "+requestRouteMessage.getPath()
//			 					+ " requested by " + requestRouteMessage.getRequesterID());
//			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>");
			debugMsg("path " + requestRouteMessage.getPath() + " requested by " + requestRouteMessage.getRequesterID());
			if (!terminals.contains(requestRouteMessage.getRequesterID()-1)){
				terminals.add(requestRouteMessage.getRequesterID()-1);
//				System.out.println("## terminals " + terminals);
				debugMsg("number of terminals: " + terminals.size() +", terminals: " + terminals );
				ArrayList<Integer> shortestPathToTree = new ArrayList<Integer>();
				int gspt = getShortestPathToTree(requestRouteMessage.getRequesterID()-1, shortestPathToTree);
				if ( gspt ==-1 )//when no there is no current solution, that is, just at the first call
					runGAAlgorithm(requestRouteMessage.getRequesterID(), 1, requestRouteMessage.getPath());
				else if ( gspt == 1){//when the node got disconnected from the graph due to dead node
					runGAAlgorithm(requestRouteMessage.getRequesterID(), 1, shortestPathToTree);//FIXME
				}
				else
					runGAAlgorithm(requestRouteMessage.getRequesterID(), 1, shortestPathToTree);
			}
		}else{ //add its ID to the path and retransmit the message
			debugMsg("path NOT IN SINK " + requestRouteMessage.getPath() + " requested by " + requestRouteMessage.getRequesterID(),2);
			requestRouteMessage.setSenderID(this.ID);
			requestRouteMessage.add(this.ID);
			MessageTimer timer = new MessageTimer(requestRouteMessage, Tools.getNodeByID(this.NextHopSink));
			timer.startRelative(0.0001, this);
			battery.spend(EnergyMode.SEND);
			Overheads+=1;
		}
	}

	/**
	 * Shortest path from node u to the existing routing tree.
	 * @param u 
	 * @param shortestPathToTree
	 */
	private int getShortestPathToTree(int u, ArrayList<Integer> shortestPathToTree) {
		ArrayList<Integer> nodes = new ArrayList<Integer>();		//contains all the nodes of the current solution
		ArrayList<String> currentSolution = new ArrayList<String>(); 
		CustomGlobal.treeOptimized.getTreeOptimized(currentSolution);
		if(currentSolution.size()==0)
			return -1;
		for(String s: currentSolution){
			String[] uv = s.split(" ");
			//sp.put("" + Integer.parseInt(uv[0]) + "-" + Integer.parseInt(uv[1]) , 1);
			if (!nodes.contains(Integer.parseInt(uv[0])))
				nodes.add(Integer.parseInt(uv[0]));
			if (!nodes.contains(Integer.parseInt(uv[1])))
				nodes.add(Integer.parseInt(uv[1]));
		}
		
		debugMsg(">>> Shortest paths: nodes from the current tree" + nodes); //nodes is 1-indexed, u is 0-indexed
		ArrayList<Integer> parent = new ArrayList<Integer>(Collections.nCopies(AdjList.length, 0));
		int connecting_node = (int)nodes.get(0)-1;
		int dmin = bfs(u, connecting_node, parent);
		if ( dmin == -1 )//Not found, disconnected graph
			return 1;
		//printParentArray(parent,connecting_node);
		for(int i=1; i<nodes.size(); i++){
			ArrayList<Integer> tmp_parent = new ArrayList<Integer>(Collections.nCopies(AdjList.length, 0));
			int d = bfs(u, (int)nodes.get(i)-1, tmp_parent);
			//printParentArray(tmp_parent,(int)nodes.get(i)-1);
			if (d<dmin){
				connecting_node = (int)nodes.get(i)-1;
				dmin = d;
				parent.clear();
				parent.addAll(tmp_parent);
			}
		}
		
		//System.out.println("))Path from " + u + " to " + connecting_node +": " + parent);
		printParentArray(parent,connecting_node);
		//System.out.println(">>> Shortest paths");
		shortestPathToTree.addAll(parent);
		return 0;
	}

	/**
	 * Printing function for the result of the bfs
	 * @param parent
	 * @param u
	 */
	public void printParentArray(ArrayList<Integer> parent, int u){
		int tmp = u;
		String msg = "route: "+u+" ";
		while (tmp != -1){
//			System.out.print(parent.get(tmp)+" ");
			msg += parent.get(tmp)+" ";
			tmp = parent.get(tmp);
		}
		debugMsg(msg, 2);
	}
	
	/**
	 * Prints in a file the information that will be given as input to the brkga.
	 * This method also prints the file in standard spt format to be given to the scip solver
	 * @param node terminal (1-indexed) node that just appeared as new event, -1 if it is the case that has update the battery levels only
	 * @param path shortest path to the sink or to the tree
	 * @param add true adding the node as a terminal node, false for deleting as terminal node. if node==-1, then this parameter doens't matter
	 */
	private synchronized void PrintGAInput(int node, ArrayList<Integer> path, boolean add) {
		//Assuming the graph is connected
		int n = Tools.getNodeList().size();
		int weight = 1;
		List<String> edges = new ArrayList<String>();
		
		//Add shortest path as solution
		Hashtable<String, Integer> sp = new Hashtable<>();
		for(int i=0; i<path.size()-1; i++){
			sp.put("" + path.get(i) + "-" + path.get(i+1), 1);
			sp.put("" + path.get(i+1) + "-" + path.get(i), 1);
		}
		//Add current tree as solution
		ArrayList<String> currentSolution = new ArrayList<String>(); 
		CustomGlobal.treeOptimized.getTreeOptimized(currentSolution);
		for(String s: currentSolution){
			String[] uv = s.split(" ");
			sp.put("" + Integer.parseInt(uv[0]) + "-" + Integer.parseInt(uv[1]) , 1);
		}
		
		/*Set<String> keys = sp.keySet();
		System.out.println("!!!!!!!!!!!!!!!!!!");
		for(String key: keys){
			System.out.println("("+key+") <- "+sp.get(key));
		}
		System.out.println("!!!!!!!!!!!!!!!!!!");*/
		
		//This block is for printing the edges in the format u-v weight sol
		//Connectivity is STRONGER thatn awaken. 
		//That is, if the node is UP it may be part the network, depending if it's connected, or not. 
		//On the other hand, if a node is DISCONNECTED, it wont be part of the network independtly of being UP or not.
		Hashtable<String, Integer> added = new Hashtable<>();
		for(int i=0; i<n; i++)
			//if (UpNodes.get(i)==1){					//Verify if the node is awaken
			if (DisconnectedNodes.get(i)==1){					//Verify if the node is CONNECTED
				for(int j=0; j<AdjList[i].size(); j++){
					int sol = 0;
					int u = i+1;
//					if ( UpNodes.get(AdjList[i].get(j)) ==1 ){		//Verify whether the node in the other end of the edge is dead. 
					if ( DisconnectedNodes.get(AdjList[i].get(j)) ==1 ){		//Verify whether the node in the other end of the edge is CONNECTED
						int v = AdjList[i].get(j)+1;
						if (u>v){
							u = AdjList[i].get(j)+1;
							v = i+1;
						}
						String key = u + "-" + v;
						if (added.get(key)==null){ //Not added yet
							added.put(key, 1);
							if (sp.get(key)!=null)
								sol = 1;
							edges.add(u + "-" + v + " " + weight + " " + sol);
						}
					}
					
				}
			}
		//System.out.println("!!!!!!!!!!!!!!!!!!");		
		/*for(int i=0; i<n; i++){
			for(int j=i; j<n-1; j++)
				if (AdjMatrix[i][j]==1){
					edges.add((i+1) + "-" + (j+1) + " " + weight);
				}
		}*/
		
		if (node!=-1){//In the case that an event started/ended
			if (add)
				nodes.get(node).terminal = 1;
			else
				nodes.get(node).terminal = 0;
		}else{//In the case battery levels have to be updated
			;//Do nothing, perhaps print smtg for debugging
		}
		
		try {
			GANode.numSPTFiles++; //Counting the number of files for SPT and NEATO
			int aliveNodes = 0;
			for(Integer i: GANode.UpNodes)
				aliveNodes += i;
			//FileWriter file = new FileWriter( "srcAG/log" + GANode.numSPTFiles + ".dat" );	//I guess this was only for testing
			FileWriter file = new FileWriter( "srcAG/log.dat" );
			PrintWriter printFile = new PrintWriter( file );
//			printFile.println(nodes.size() + " " + edges.size());
			printFile.println( aliveNodes + " " + edges.size());
			
			//for (Vertex v : nodes){
			for (Integer key : nodes.keySet()){
				Vertex v = nodes.get(key);
				if ( GANode.DisconnectedNodes.get(v.ID-1)==1 )
					printFile.println(	v.ID + " " + 
										v.terminal + " " +
										v.x + " " + 
										v.y + " " + 
										v.battery);
			}
				
			/*for(int i=0; i<n; i++)
				printFile.println(	nodes.get(i).ID + " " + 
									nodes.get(i).terminal + " " +
									nodes.get(i).x + " " + 
									nodes.get(i).y + " " + 
									nodes.get(i).battery);*/
			printFile.println();
			for (String s : edges)
				printFile.println(s);
			
			printFile.close();
			file.close();
			
			//Print file in SPT format
			String stpname = String.format("ff%04d.stp", GANode.numSPTFiles);
//			String dotname = String.format("t" + Global.currentTime + "_gg%04d.dot", GANode.numSPTFiles);
			
			STPWriter sptfile = new STPWriter(stpname, this.objFunction, ""+Global.currentTime ); 
			sptfile.setInput(nodes, edges);
			sptfile.write();
			//System.out.println("SendDataEvent(): updateBattery: objFunction=1, Printing dot file" + stpname);
			
			//Print graph in NEATO format
//			NEATOWriter neatofile = new NEATOWriter(dotname, this.battery.getInitialEnergy());
//			neatofile.setInput(nodes, edges);
//			neatofile.write();
//			debugMsg("Printing dot file " + dotname + " and stp file " + stpname,1);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
	}

	/**
	 * The sink sends this message to the nodes that are path of the routing tree so they update their
	 * data. It is possible that some nodes are removed from the routing tree so the sink also has to
	 * send the message to these nodes.
	 * @param msg
	 */
	private void SetRoute(Message msg) {
		SetRouteMessage setRouteMessage = (SetRouteMessage)msg;
		getBattery().spend(EnergyMode.RECEIVE);
		setColor(Color.GREEN);
		Tools.appendToOutput("SetRoute(): ID:" + this.ID + " currentNumTree:" +this.numTree + " recvdNumTree:"+ setRouteMessage.numTree);
		if (setRouteMessage.numTree > this.numTree){
			this.NextHopSink = setRouteMessage.SenderID;
			this.HopToSink = setRouteMessage.hopToSink+1;
			this.numTree = setRouteMessage.numTree;
			for(GATree t : setRouteMessage.trees){
				if (t.root == this.ID){
					SetRouteMessage srm = new SetRouteMessage(this.ID, this.HopToSink, setRouteMessage.numTree);
					srm.hopToSink = this.HopToSink;
					for(GATree ts : t.getTrees())
						srm.addTree(ts);
					SetRouteTimer timer = new SetRouteTimer(this, srm);
					timer.startRelative(0.0001, Tools.getNodeByID(this.ID));
					getBattery().spend(EnergyMode.SEND);
					Overheads+=1;
					Tools.appendToOutput("Tree: "+t+ " numTree =>"+ setRouteMessage.numTree +"\n" );
				}
				
			}
		}
	}

	/**
	 * Standard bfs for getting the shortest path between u and v in an unweighted graph.
	 * Method used for getting the shortest path between a node a the existing routing tree 
	 * @param v1 source node 0-indexed
	 * @param v2 destiny node 0-indexed
	 * @param parent array
	 */
	public int bfs(int v1, int v2, ArrayList<Integer> parent){ //This method works everything 0-indexed
		ArrayList<Integer> visited = new ArrayList<Integer>(Collections.nCopies(AdjList.length, 0));
		ArrayList<Integer> distance = new ArrayList<Integer>(Collections.nCopies(AdjList.length, 0));
		visited.set(v1, 1);
		parent.set(v1, -1);
		Queue q = new LinkedList<Integer>();
		q.add(v1);
		boolean found = false;
		//System.out.println("u: " + u);
		//int c=0;
		while (!q.isEmpty()){
			int u = (int) q.poll();
			//System.out.print("r"+c+" "+u+": ");
			for(int i=0; i<AdjList[u].size(); i++){
				
				int v = AdjList[u].get(i);
				if (GANode.UpNodes.get(v)==0)
					continue;
				if (visited.get(v)==0){//not yet visited
					parent.set(v, u);
					if (distance.get(v)==1){
						System.out.println();
					}
					int d = distance.get(u);
					distance.set(v, d+1);
					//System.out.print(distance.get(v)+ " (" + v + ") ");
					visited.set(v, 1);//Mark as visited
					if (v==v2){
						found = true;
						break;
					}
					q.add(v);
				}
			}
			//c++;
			//System.out.println();
			if (found)
				break;
		}
		if (!found && GANode.UpNodes.get(v2)==1){//If it was not found and is up (has energy) then mark it as disconnected
			bfsMarkDisconnected(v1);
			return -1;
		}
		System.out.println("BFS from "+ v1 + " to " + v2 + " distance: " + distance.get(v2));
		return distance.get(v2);
	}
	
	/**
	 * @param v1 node where to start the bfs. 0-indexed
	 * @param visited
	 */
	public void bfs(int v1, ArrayList<Boolean> visited){
		Queue q = new LinkedList<Integer>();
		q.add(v1);
		visited.set(v1, true);
		while ( !q.isEmpty() ){
			int u = (int) q.poll();
			for(int i=0; i<AdjList[u].size(); i++){
				int v = AdjList[u].get(i);
				if (GANode.DisconnectedNodes.get(v)==0)
					continue;
				if ( !visited.get(v) ) {
					visited.set(v, true);
					q.add(v);
				}
			}
		}
	}
	
	/**
	 * Makes a BFS to set nodes as disconnected from the terminal.
	 * @param v1 disconnected node 0-indexed
	 */
	void bfsMarkDisconnected(int v1){
		debugMsg(v1 + " disconnected, marking the nodes that reach it as disconnected");
//		Exporter exp = new Exporter();
//		new Exporter(this).export(new Rectangle(0, 0, graphPanel.getWidth(), graphPanel.getHeight()), getTransformator());
		ArrayList<Integer> visited = new ArrayList<Integer>(Collections.nCopies(AdjList.length, 0));
		visited.set(v1, 1);
		Queue<Integer> q = new LinkedList<Integer>();
		q.add(v1);
		while (!q.isEmpty()){
			int u = (int) q.poll();
			System.out.print(u+": ");
			for(int i=0; i<AdjList[u].size(); i++){
				int v = AdjList[u].get(i);
				System.out.print(v+", ");
				if (GANode.DisconnectedNodes.get(v)==0)
					continue;
				if (visited.get(v)==0){//not yet visited
					visited.set(v, 1);//Mark as visited
					q.add(v);
					GANode.DisconnectedNodes.set(v, 0);
					//if ( GANode.terminals.contains(v) )
					//	GANode.ter .set(v, 0);
					/*if ( GANode.terminals.contains(v) ){
						for ( sinalgo.nodes.timers.Timer t : Tools.getNodeByID(v+1).getTimers() ){
							t.
						}
					}*/
						
				}
			}
			System.out.println();
		}
	}
	
	/**
	 * Connected components. Function to verify if a dead node leaves a component disconnected.
	 * The component connected to the sink is always connected; the others aren't.
	 * It just considers the UpNodes.
	 * @param nodes nodes to be fixed. 1-indexed
	 */
	public void updateConnectedComponents(ArrayList<Integer> nodes){
		ArrayList<Boolean> visited = new ArrayList<Boolean>(Collections.nCopies(AdjList.length, false));
		bfs(0, visited);
		for(int i=0; i< nodes.size(); i++)//Verify if the node can be reached
			if ( visited.get(nodes.get(i)-1) == true ){//can be reached,
				ArrayList<Integer> parent = new ArrayList<>(Collections.nCopies(AdjList.length, 0));
				bfs(0, nodes.get(i)-1, parent);
				ArrayList<Integer> path = new ArrayList<>();
				
				int tmp = parent.get(nodes.get(i)-1);
				//String msg = "route: "+u+" ";
				while (tmp != -1){
//					System.out.print(parent.get(tmp)+" ");
					//msg += parent.get(tmp)+" ";
					path.add(tmp+1);
					tmp = parent.get(tmp);
				}
//				System.out.println(" ******** " + path);
//				System.out.println(" ******** " + nodes.get(i));
				path.add(0,nodes.get(i));
				Collections.reverse(path);
				path.remove(0);
				int destiny = path.get(0);
				path.remove(0);
				//path.add(nodes.get(i));
//				System.out.println(" --------	 " + path);
				SetRepairRouteMessage srrm = new SetRepairRouteMessage(this.ID);
				srrm.setPath(path);
				RepairDeadNodeTimer rdnt = new RepairDeadNodeTimer(Tools.getNodeByID(1), srrm, Tools.getNodeByID(destiny));
				rdnt.startRelative(0.0001, Tools.getNodeByID(1));
//				RepairDeadNodeGATimer rtimer = new RepairDeadNodeGATimer(this);
			}
		for ( int i=0; i<GANode.DisconnectedNodes.size(); i++ ){
			if (GANode.DisconnectedNodes.get(i)==1 && !visited.get(i))
				bfsMarkDisconnected(i);
		}
		
		checkTerminalConnectivity();
	}
	
	private void checkTerminalConnectivity() {
		int n = 0;
		for(int i=0; i<GANode.terminals.size(); i++){
			if ( GANode.DisconnectedNodes.get(GANode.terminals.get(i)) == 0 ){
				eraseTimers(GANode.terminals.get(i)+1);
				n++;
			}
		}
		/*if ( n == GANode.terminals.size() ){
			debugMsg("Finishing simulation");
			sinalgo.tools.Tools.exit();
		}*/
	}
	
	/**
	 * Erases timers of the node
	 * @param nodeID id of the node to erase its timers. 1-indexed
	 */
	public void eraseTimers(int nodeID){
		Tools.getEventQueue().removeAllEventsForThisNode(Tools.getNodeByID(nodeID));
		/*Node n = Tools.getNodeByID(nodeID);
		TimerCollection timerCollection = n.getTimers();
		Iterator<sinalgo.nodes.timers.Timer> it = timerCollection.iterator();
		if ( timerCollection.size() > 0 )
			while ( it.hasNext() ){
				it.remove();
			}*/
	}

	/**
	 * 
	 * @param requesterID (1-indexed) the node that request a new route 
	 * @param reason indicates the motive why it is required that the algorithm is run again: 	1: new event detected, 
	 * 																							2: event end, 
	 * 																							3: variation, 
	 * 																							4: dead node
	 * @param path
	 */
	public synchronized void runGAAlgorithm(int requesterID, int reason, ArrayList<Integer> path){
		String debug_msg = null ;
		if ( reason == 1 ){		//OK!
			debug_msg = "RequestRoute";
			PrintGAInput(requesterID, path, true);
		}			
		else if ( reason == 2 ){
			debug_msg = "Remove";
			PrintGAInput(requesterID, new ArrayList<Integer>(), false);
		}	
		else if ( reason == 3 ){
			debug_msg = "Variation";
			PrintGAInput(-1, new ArrayList<Integer>(), true);
		}			
		else if ( reason == 4 ){
			debug_msg = "DeadNode";
			PrintGAInput(-1, new ArrayList<Integer>(), true);
		}
		debugMsg(debug_msg, 2);
		//execute the algorithm. Once the algorithm calculates a routing tree it schedules a SetRouteMessage to be sent
		if ( GANode.threadGA == null ){
			GANode.executeAG = new ExecuteAG(); 
			GANode.threadGA = new Thread( GANode.executeAG );
			GANode.threadGA.start();
		} else {
			if ( GANode.executeAG.isExecuting() ){
//				this.wait(10000);
//				wait(10000);
				try {
					Tools.appendToOutput("Waiting execution of GA to finish...\n");
					((GANode)Tools.getNodeByID(this.ID)).wait(5000);
					
					//FIXME: then, kill it, and then reinitiate
					
				}catch(Exception e){
					e.printStackTrace();
				}
			} else {
				GANode.executeAG.setCanExecute(true);
			}
			//GANode.executeAG.setCanExecute(true);
		}
		
//		synchronized ((GANode)Tools.getNodeByID(this.ID)) {
		/*synchronized ((GANode)Tools.getNodeByID(1)) {
			debugMsg("this should only be run by the sink, lets see node.ID: " + this.ID , 1);
			if(threadGA==null){
				debugMsg(debug_msg + "("+requesterID+"): THREAD INIT ");
				threadGA = new Thread( new ExecuteAG((GANode)Tools.getNodeByID(this.ID)));
				threadGA.start();
			}else if(threadGA.isAlive()){//This happens when arrives a request from a node but the sink is still computing a tree for the previous request
				try {
					//Thread.sleep(60000); //we wait for a while, then we kill it and calculate again
					//((GANode)Tools.getNodeByID(this.ID)).wait(60000);
					((GANode)Tools.getNodeByID(this.ID)).wait(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				debugMsg(debug_msg + "("+requesterID+"): THREAD KILLED ");
				threadGA = null;
				threadGA = new Thread(new ExecuteAG((GANode)Tools.getNodeByID(this.ID)));
				threadGA.start();
			}else {
				debugMsg(debug_msg + "("+requesterID+"): THREAD NOT RUNNING, INIT ");
				threadGA = null;
				threadGA = new Thread(new ExecuteAG((GANode)Tools.getNodeByID(this.ID)));
				threadGA.start();
			}
		}*/
	}
	
	@Override
	public void handleNAckMessages( NackBox nackBox ) {

	}
	
	/**
	 * If the node has no neighbors with higher hop level, then this is a border node and it broadcasts a BorderMessage
	 */
	public void StartCollectingInformation(){
		if (NeighborHopBigger.size()==0){
			isBorderNode = 1;
			this.setColor(Color.ORANGE);
			BorderCollectInformationTimer borderTimer = new BorderCollectInformationTimer(new BorderMessage(this.ID), this);
			borderTimer.startRelative(0.001, this);
			Overheads+=1;
		}else
			isBorderNode = 2;
	}
	
	private void createAdjacencyMatrix(BorderMessage msg) {
		int n = Tools.getNodeList().size();
		//if (AdjMatrix == null)
		//	AdjMatrix = new int[n][n];
		
		if (AdjList == null){
			AdjList = (List<Integer>[])new List[n];
	        for (int i = 0; i < n; i++) 
	        	AdjList[i] = new ArrayList<Integer>();
		}
		
//		for(int i=0; i<msg.node1.size(); i++){ //Iterate over the information carried by the BorderMessage
//			Integer u = msg.node1.get(i)-1;	//the -1 is because the sink has ID=1
//			Integer v = msg.node2.get(i)-1;
		
		
		/*for(int i=0; i<msg.getNode1().size(); i++){ //Iterate over the information carried by the BorderMessage
			Integer u = msg.getNode1().get(i)-1;	//the -1 is because the sink has ID=1
			Integer v = msg.getNode2().get(i)-1;
			//AdjMatrix[u][v] = 1;
			//AdjMatrix[v][u] = 1;
			if (!AdjList[u].contains(v))
				AdjList[u].add(v);
			if (!AdjList[v].contains(u))
				AdjList[v].add(u);
			//AdjList[v].add(u);
		}*/
		
		float deg = 0;
		if ( !this.runUpdateAdjacencyMatrix ){
			for(int i=0; i<n; i++){
				GANode u = (GANode) Tools.getNodeByID(i+1);
				for(Integer v : u.listAll){
					AdjList[i].add(v-1);
				}
				deg += u.listAll.size();
			}
			this.runUpdateAdjacencyMatrix = true;
		}
		
		debugMsg("Updated adjacency matrix");
		debugMsg("Average degree: " + deg/n);
		Tools.appendToOutput("Updated adjacency matrix!\n");
		for(int i=0; i<n; i++){
			System.out.print(i+": ");
			for(int j=0; j<AdjList[i].size(); j++)
				System.out.print(AdjList[i].get(j)+" ");
			System.out.println();
		}
	}

	/*public void updateNodesSeenBySink(Vertex v){
		//this.nodes.get(v.ID).battery = v.battery;
		//this.nodes.get(key)
	}*/
	
	/*public void removeNodesSeenBySink(ArrayList<Vertex> vertices){
		for ( Vertex v : vertices ){
			GANode.UpNodes.set(v.ID - 1, 0);		//Mark it as dead
			GANode.DisconnectedNodes.set(v.ID - 1, 0);	//Mark it as disconnected
			this.nodes.remove(v.ID);			//Remove it from nodes
			debugMsg(" removing vertex " + v.ID + " (1-indexed) from the nodes seen by the sink");
		}
		updateEdgesSeenBySink();				//Update the edges list
	}
	
	public void updateEdgesSeenBySink(){
		int n = Tools.getNodeList().size();
		int weight = 1;		//The weight of the edge is 1, at least for now
		Hashtable<String, Integer> added = new Hashtable<>();
		for(int i=0; i<n; i++)
			if (UpNodes.get(i)==1){					//Verify if the node is awaken
				for(int j=0; j<AdjList[i].size(); j++){
					int sol = 0;
					int u = i+1;
					if ( UpNodes.get(j) ==1 ){		//Verify whether the node in the other end of the edge is dead. 
						int v = AdjList[i].get(j)+1;
						if (u>v){
							u = AdjList[i].get(j)+1;
							v = i+1;
						}
						String key = u + "-" + v;
						if (added.get(key)==null){ //Not added yet
							added.put(key, 1);
//							if (sp.get(key)!=null)
//								sol = 1;
							edges.add(u + "-" + v + " " + weight + " " + sol);
						}
					}
					
				}
			}
	}*/
	
	public void aproximationtree() {
		MessageSPTTimer msgtree = new MessageSPTTimer( new MSGTREE( 1, this.NextHopSink, this.ID, "Sink" ) );
		double time = uniformRandom.nextSample();
		msgtree.startRelative( time, this );
	}

	@Override
	public void init() {

		// TODO Auto-generated method stub
		myRole = Roles.RELAY;

		// double endTime=0;

		try {
			SimulationTime = sinalgo.configuration.Configuration.getDoubleParameter( "SimTime" );
			EventsAmount = sinalgo.configuration.Configuration.getIntegerParameter( "Event/NumEvents" );
			EventsTimes = sinalgo.configuration.Configuration.getIntegerParameter( "Event/Time" );
			eventEndTime = sinalgo.configuration.Configuration.getDoubleParameter( "Event/EventEnd" );
			Density = sinalgo.configuration.Configuration.getIntegerParameter( "Density" );
			for ( int i = 1; i <= sinalgo.configuration.Configuration.getIntegerParameter( "Event/NumEvents" ); i++ ) {
				EventSPTTimer t = new EventSPTTimer( i );
				t.startEventAbsolute( sinalgo.configuration.Configuration.getDoubleParameter( "Event/EventStart" + i ), this, i );
			}
			populations = sinalgo.configuration.Configuration.getIntegerParameter( "Population" );
			generations = sinalgo.configuration.Configuration.getIntegerParameter( "Generations" );
			preprocessing = sinalgo.configuration.Configuration.getIntegerParameter( "Preprocessing" );
			objFunction = sinalgo.configuration.Configuration.getIntegerParameter( "Objective" );
			fFactor = sinalgo.configuration.Configuration.getDoubleParameter( "FFactor" );
			kFactor = sinalgo.configuration.Configuration.getDoubleParameter( "KFactor" );
			bVariation = sinalgo.configuration.Configuration.getDoubleParameter( "UpdateVariation" );
			
			for (int i = 1; i <= sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents"); i++) {
				EventEndGATimer t = new EventEndGATimer(i,this);
				t.startAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventEnd"+i), this);
			}
			
			DataRate = sinalgo.configuration.Configuration.getIntegerParameter( "Event/DataRate" );
			EventSize = sinalgo.configuration.Configuration.getIntegerParameter( "Event/EventSize" );
			CommunicationRadius = sinalgo.configuration.Configuration.getIntegerParameter( "UDG/rMax" );
			DropRate = sinalgo.configuration.Configuration.getIntegerParameter( "TaxadePerda/dropRate" );

		} catch ( CorruptConfigurationEntryException e ) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit( 0 );
		}
		
		try {
			//Here, we have to get the battery implementation from Config.xml and inject into battery attribute
			String energyModel = Configuration.getStringParameter("Energy/EnergyModel");
			if (energyModel.contains("Simple")){
				battery = new SimpleEnergy(this.ID);
				float bat = sinalgo.configuration.Configuration.getIntegerParameter( "Energy/MaxEnergy" );
				battery.setTotalEnergy(bat);
			}
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Energy Model not found");
			e.printStackTrace();
		}

		//mapNeibohrs.put( this, new LinkedList<Integer>() );
		

		if ( this.ID == 1 ) {
			this.setColor( Color.RED );
			this.HopToSink = 0;
			this.myRole = Roles.SINK;
			this.mystatus = Status.READY;
			this.reached = true;
			//this.numberOfNodes = Random.numberOfNodes;
			this.generateEvent = 1;
			
			//if (!isGraphConnected())1
			//	sinalgo.tools.Tools.exit();
			
			//this.nodes.add(new Vertex(this.ID, 1, this.getPosition().xCoord, this.getPosition().yCoord, this.getBattery().getEnergy()));
			//this.nodes.add(new Vertex(this.ID, 1, this.getPosition().xCoord, this.getPosition().yCoord, 0));
			this.nodes.put(this.ID, new Vertex(this.ID, 1, this.getPosition().xCoord, this.getPosition().yCoord, 0));
			UpNodes.add(1);
			DisconnectedNodes.add(1);
			sendMCI();
		}else{
			UpNodes.add(1);
			DisconnectedNodes.add(1);
			//this.nodes.add(new Vertex(this.ID, 0, this.getPosition().xCoord, this.getPosition().yCoord, this.getBattery().getEnergy()));
			//this.nodes.add(new Vertex(this.ID, 0, this.getPosition().xCoord, this.getPosition().yCoord, 0));
			this.nodes.put(this.ID, new Vertex(this.ID, 0, this.getPosition().xCoord, this.getPosition().yCoord, 0));
		}

		VerifyBorderNodeTimer borderTimer = new VerifyBorderNodeTimer(this);
		borderTimer.startAbsolute(1000,this);
		//if (!isNodeConnected())
		//	sinalgo.tools.Tools.exit();
		
		// Event timers
		EndSPTTimer etimer = new EndSPTTimer();
		etimer.startAbsolute( SimulationTime, this );

	}


	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Initial flooding of the network with MCIMessage to set the hopToSink and HextHopSink
	 * @param msg
	 */
	public void InitialConfiguration(Message msg){
		MCIMessage mcimsg = (MCIMessage) msg;
		battery.spend(EnergyMode.RECEIVE);
		
		if (this.checkConnectivity){
			if (!isGraphConnected())
				sinalgo.tools.Tools.exit();
			this.checkConnectivity=false;
		}
		
		if ( !listAll.contains( mcimsg.getSenderID() ) ) {
			listAll.add( mcimsg.getSenderID() );
		}

		if ( this.HopToSink > (mcimsg.getHopToSink()+1) ) {
			//System.out.println("MCI #"+this.ID);			
			//this.HopToSink = mcimsg.HopToSink;
		//}
		//if ( !this.reached && this.HopToSink > mcimsg.getHopToSink() ) {
			this.setColor( Color.GREEN );			
			
			this.NextHopSink = mcimsg.getSenderID();
			if (this.NextHopSink == 1)
				this.battery.setIsSinkNeighbor(true);
			this.HopToSink = mcimsg.getHopToSink()+1;

			//if(!this.sentmci){
				MCIMessage nmci = new MCIMessage(this.HopToSink, this.ID);
				MessageSPTTimer MCICNS = new MessageSPTTimer( nmci );
				//MCICNS.startRelative( 0.0001, this );
				MCICNS.startRelative(this.HopToSink+0.1,this);
				battery.spend(EnergyMode.SEND);
				Overheads = Overheads + 1;
				this.sentmci = true;
				Tools.appendToOutput("handleMessages() MCIMessage Overheads: " + this.Overheads + "\n");
			//}			
			//feedbackTimer = new PIF_FeedbackTimer( this, TNO.SCHEDULE_FEEDBACK );
			//feedbackTimer.tnoStartRelative( 2, this, TNO.SCHEDULE_FEEDBACK );
			
			/*if(isBorderNode==0){
				BorderInitNodeTimer borderTimer = new BorderInitNodeTimer(this);
				borderTimer.startAbsolute(1000,this);
			}			
			if (NeighborHopBigger.contains(mcimsg.SenderID))
				NeighborHopBigger.remove(NeighborHopBigger.indexOf(mcimsg.SenderID));*/
				//NeighborHopBigger.remove(mcimsg.SenderID);

		}/*else if( (mcimsg.HopToSink+1) > this.HopToSink){
			if (!NeighborHopBigger.contains(mcimsg.SenderID))
				NeighborHopBigger.add(mcimsg.SenderID);
			isBorderNode = 2;
		}else{
			if (NeighborHopBigger.contains(mcimsg.SenderID))
				NeighborHopBigger.remove(NeighborHopBigger.indexOf(mcimsg.SenderID));
				//NeighborHopBigger.remove(mcimsg.SenderID);
		}*/

		// ?????GUARDAR O NEXTTOHOP E O COM MAIOR HOPTOSINK
		// N� mais distante que transmitiu - Armazeno como filho
		/*if ( this.HopToSink < mcimsg.getHopToSink() ) {
			this.setColor( Color.blue );

			this.myRole = Roles.RELAY;
			if ( !son.contains( mcimsg.getSenderID() ) ) {
				son.add( mcimsg.getSenderID() );
				this.childrenNodes = this.childrenNodes + 1;
			}

		} else {
			this.myRole = Roles.LEAFNODE;
			this.setColor( Color.GREEN );
		}

		if ( this.ID == 1 ) {
			this.myRole = Roles.SINK;
		}*/
	}
	
	/**
	 * After a time (1 second) every node verifies whether its a border node; in such a case 
	 * it sends the BorderMessage 
	 */
	public void SetNeighborHopBigger(){
		NeighborHopBigger.clear();
		for(Integer i: this.listAll){
			if ( this.HopToSink < ((GANode)Tools.getNodeByID(i)).getHopToSink())
				NeighborHopBigger.add(i);
		}
		if (NeighborHopBigger.size()==0){
			this.isBorderNode = 1;
			this.setColor(Color.ORANGE);
			BorderCollectInformationTimer borderTimer = new BorderCollectInformationTimer(new BorderMessage(this.ID), this);
			borderTimer.startRelative(0.001, this);
			Overheads+=1;
		}
		else
			this.isBorderNode = 2;
	}
	
	/**
	 * Function called by the EventEndGATimer to send the removemessage to the sink
	 */
	public void RemoveNode(int nodeID){
		RemoveMessage rm = new RemoveMessage(nodeID);
		this.send(rm, Tools.getNodeByID(this.NextHopSink));
		Tools.appendToOutput("Generated RemoveMessage for node " + rm.getNodeID() + "\n");
	}
		
	/**
	 * method called by the DeadNodeRepairGATimer:
	 * Sends a message to the neighbors; they update their neighbors list. (not actually sending package, just a simplification for ease of coding) 
	 * The neighbors that have this node as NextHopToSink are marked for being updated
	 * So when the message reaches the sink it knows which nodes have to be updated.
	 * @param iD
	 */
	public void broadcastDeadNodeRepairMessage(int iD){
		this.battery.spend(EnergyMode.SEND);
		
		Overheads += 1;
		//RepairDeadNodeMessage rdnm = new RepairDeadNodeMessage(this.ID);
		//this.broadcast(rdnm);
		debugMsg("Broadcasted RepairDeadNodeMessage for " + this.ID , 2);
		
		ArrayList<Integer> neighbors = new ArrayList<>();
		for ( int i=0; i<this.listAll.size(); i++ ){
			Overheads += 1;
			GANode neighbor_node = (GANode)Tools.getNodeByID(this.listAll.get(i)); 
			neighbor_node.battery.spend(EnergyMode.RECEIVE);
	
			if ( neighbor_node.listAll.contains(this.ID) ){
				neighbor_node.listAll.removeFirstOccurrence(this.ID);
				//neighbor_node.listAll.remove(this.ID);
			}
				
			if ( neighbor_node.NextHopSink == this.ID ){
				neighbor_node.battery.spend(EnergyMode.SEND);
				//MessageSPTTimer routeTimer = new MessageSPTTimer(rrm, Tools.getNodeByID( this.NextHopSink ));
				//routeTimer.startRelative(0.001, this);
				Overheads += 1;
				neighbors.add(this.listAll.get(i));
			}
		}
		DeadNodeMessage dnm = new DeadNodeMessage(iD, this.battery.getEnergy());
		dnm.setNeighbors(neighbors);
		this.send(dnm, Tools.getNodeByID(this.NextHopSink));
		debugMsg("Generated DeadNodeMessage for " + dnm.getNodeID() + ", started its tranmission to the sink" , 2);
	}
	
	/**
	 * The message MSGTREE is sent to collect the final tree when the events are done.
	 * Note that there is a SimulationTime event that closes the simulation and there
	 * is a eventEndTime that indicates that the events are finished and a MSGTREE is
	 * sent to collect the information regarding the trees being used to send the information
	 * 
	 * This method does not need to log battery sends or receives since its used just to 
	 * collect information about the simulation routes.
	 * @param msg
	 */
	public void GetRoutingTree(Message msg){
		MSGTREE msgtree = (MSGTREE) msg;
		if ( this.ID == 1 ) {
			System.out.println( "MSGTREE nextHop " + msgtree.getNexthop() + " destino " + msgtree.getDest() );
			//////////////////////////////######################################
			//this.Edges = CustomGlobal.treeOptimized.size() ;
			List<String> tree = new ArrayList<String>();
			CustomGlobal.treeOptimized.getTreeOptimized(tree);
			this.Edges = tree.size();
			//PrintResult.print();
		}
		if ( this.ID == msgtree.getNexthop() ) {
			this.filhosrecv.add( msgtree.getSentnode() );

			if ( this.myRole != Roles.SINK ) {
				this.setColor( Color.yellow );
				this.Disttree = this.Disttree + msgtree.getDisttree();
				this.Disttreerecv = this.Disttreerecv + 1;

				if ( this.filhos.size() == 1 ) {
					this.filhossend.add( this.NextHopSink );
					broadcast( new MSGTREE( this.Disttree + 1, this.NextHopSink, this.ID ) );
					this.setColor( Color.black );
				} else if ( this.filhos.size() == this.Disttreerecv ) {
					broadcast( new MSGTREE( this.Disttree + 1, this.NextHopSink, this.ID ) );
					this.filhossend.add( this.NextHopSink );
					this.setColor( Color.white );
				}

			} else {
				this.Disttree = this.Disttree + msgtree.getDisttree();
				this.Disttreerecv = this.Disttreerecv + 1;
				// Tools.appendToOutput( "filhos: " + this.filhos.size()
				// + "  rec: " + this.Disttreerecv );
				this.setColor( Color.black );
				if ( this.filhos.size() == 1 ) {
					Edges = this.Disttree;
				} else {
					if ( this.filhos.size() == this.Disttreerecv ) {
						Edges = this.Disttree;
					}
				}
			}
		}
	}
	
	/**
	 * This message is the data being gathered by the sensors itself.
	 * The flow to get to this message is the following:
	 * init() -> EventSPTTimer.fire() -> startDetecion() -> SPTTimer.fire() -> timeout() -> sendData()  
	 * being the latter the one that generates this kind of message if the current time is smaller
	 * than eventEndTime or generating a MSGTREE otherwise
	 * @param msg
	 * @param sender
	 */
	public void SendDataEvent(Message msg, int sender){
		GADataMessage mdata = (GADataMessage) msg;
		
		if(this.ID == mdata.getDest()){
			if (!this.filhos.contains((Object)sender)){
				this.filhos.add(sender);
			}
		}

		if(this.ID == mdata.getDest()){

			if (!this.son.contains((Object) mdata.getSender()))
				son.add(mdata.getSender());

			if ((this.myRole != Roles.SINK) ){

				double time = uniformRandom.nextSample();

				if(Global.currentTime > this.nextsenddata)
					this.senddata =true;


				//this.aggregatePCKT = this.aggregatePCKT + mdata.getAggPacket();
				this.rota = true;
				if( (this.filhos.size()>1)){
					this.setColor(Color.blue);
				}else{
					this.setColor(Color.yellow);
				}

				//if ( ( (this.filhos.size()<2) && (this.myRole == Roles.RELAY)) ){
				if ( ( (this.filhos.size()<2) ) ){
					this.battery.spend(EnergyMode.SEND);
					if (this.battery.getEnergy()<this.battery.getMinEnergy() && !GANode.terminals.contains(this.ID-1))
						debugMsg("***** this should happen few very few packets for node "+ this.ID, 2);
					mdata.setDest(this.NextHopSink);
					mdata.setSender(this.ID);
					mdata.setHopToSink(this.HopToSink);
					mdata.accumDTime();
					//mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getEnergy()));
					mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getTotalSpentEnergy()));
					MessageSPTTimer msgTimer = new MessageSPTTimer(mdata,Tools.getNodeByID(this.NextHopSink));
					enviados = enviados + 1;
					msgTimer.startRelative(0.1,this);	//The message is sent almost inmediately

					//									sptLog.logln("Sdata t "+ (Global.currentTime+ time)
					//										+ " Ns "+ this.ID 
					//										+ " Ap " + aggregatePCKT 
					//										+ " Nd " + 1);

					DataPackets = DataPackets +1;
					Tools.appendToOutput("SendDataEvent(): Datapackets: "+this.DataPackets + "\n");
					//Spent energy due to the transmission mode
					
					if (this.myRole == Roles.RELAY)
						this.setColor(Color.CYAN);
					//this.aggregatePCKT = 0;
				}//else if ((this.senddata) && (this.myRole != Roles.COLLABORATOR)){
				else if ((this.senddata)){
					this.battery.spend(EnergyMode.SEND);
					if (this.battery.getEnergy()<this.battery.getMinEnergy() && !GANode.terminals.contains(this.ID-1))
						debugMsg("***** this should happen few very few packets for node "+ this.ID, 2);
					mdata.setDest(this.NextHopSink);
					mdata.setSender(this.ID);
					mdata.setHopToSink(this.HopToSink);
					mdata.accumDTime();
					//System.out.println(this.ID + " RX: "+mdata.getPayload());
					//mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getEnergy()));
					mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getTotalSpentEnergy()));
					//System.out.println(this.ID + " TX: "+mdata.getPayload());
					//mdata.setEventNum(this.eventnum);
					//mdata.setAggPacket(this.aggregatePCKT);
					nextsenddata = Global.currentTime + DataRate;

					MessageSPTTimer msgTimer = new MessageSPTTimer(mdata,Tools.getNodeByID(this.NextHopSink));
					msgTimer.startRelative(DataRate,this);	
					
					DataPackets = DataPackets +1;
					Tools.appendToOutput("SendDataEvent(): Datapackets: "+this.DataPackets + "\n");
					this.rota = true;
					//enviadosAgg = enviadosAgg + this.aggregatePCKT;
					this.senddata = false;
					//this.aggregatePCKT = 0;
					//if(this.myrole == Roles.RELAY)
					this.setColor(Color.black);
				}
			}


			if(this.myRole == Roles.SINK)
			{
				//						sptLog.logln("Rdata t " + Global.currentTime
				//								+ " Ns " + inbox.getSender()
				//								+ " Ap " + mdata.getAggPacket()
				//								+ " Nd " + this.ID
				//								);
				Recivers = Recivers +1;
				//packetrecvagg = packetrecvagg + mdata.getAggPacket();
				Tools.appendToOutput("SendDataEvent(): GADataMessage arrived to the SINK, Receivers: " +this.Recivers +"\n");
				Tools.appendToOutput("SendDataEvent(): Updated energies: " + mdata.getPayload()+"\n");
//				debugMsg("delivery time: "+ (Global.currentTime - mdata.getDeliveryTime()));
				//debugMsg("delivery time: "+ mdata.getDeliveryTime() );
				mdata.debugMsg();
				System.out.println("Payload "+mdata.getPayload());
				//For handling energies
				if ( objFunction==2 ||objFunction==3 ){
					boolean run = false;
					String[] energies = mdata.getPayload().toString().split(";");
					int reqNode = 0;
					for (String e : energies){
						String[] nb = e.split(",");
						Vertex v = nodes.get(Integer.parseInt(nb[0]));
						double variation = (Double.parseDouble(nb[1]) - v.pbattery)/this.battery.getInitialEnergy();
						if ( variation >= bVariation && v.terminal==0 ){//Not considering terminal nodes, just steiner nodes
							run = true;
							reqNode = v.ID;
							v.pbattery = Double.parseDouble(nb[1]);
							Tools.appendToOutput("SendDataEvent(): running due to " + variation + " of variation on node (1-indexed) " 
												+ Integer.parseInt(nb[0]) + "\n");
							debugMsg("running GA due to " + variation + " of variation on node (1-indexed) " 
									+ Integer.parseInt(nb[0]), 2);
						}
						v.battery = Double.parseDouble(nb[1]);
					}
					if(run){
						runGAAlgorithm(reqNode, 3, new ArrayList<Integer>());	//FIXME: setting as requestingNode the last one, but it might be posible that many nodes variate its energy enough and this isn't logging it.
					}
				}else if ( objFunction == 1){
					boolean run = false;
					String[] energies = mdata.getPayload().toString().split(";");
					int reqNode = 0;
					for (String e : energies){
						String[] nb = e.split(",");
						Vertex v = nodes.get(Integer.parseInt(nb[0]));
						double variation = (Double.parseDouble(nb[1]) - v.pbattery)/this.battery.getInitialEnergy();
						if ( variation >= bVariation && v.terminal==0 ){
							run = true;
							reqNode = v.ID;
							v.pbattery = Double.parseDouble(nb[1]);
						}
						v.battery = Double.parseDouble(nb[1]);
						//debugMsg("updatingBattery: objFunction=1, new value of battery " + v.battery + "(" + nodes.get(Integer.parseInt(nb[0])).battery +")" + " for node " + Integer.parseInt(nb[0]));
					}
					if ( run ){
						PrintGAInput(-1, new ArrayList<Integer>(), true); 	//Don't run the algorithm on variation, since it wont influence the 
																			//routing tree at all. It will only affect if the variation makes the
																			//node die, but such situation is already handled by the spend method  
																			//of SimpleEnergy.java
//						runGAAlgorithm(reqNode, 3, new ArrayList<Integer>());	
					}

				}
			}
		}
		
		/*List<String> listTree = new ArrayList<String>();

		// Pega seu nó anterior na arvore
		if ( !this.previous.contains( mdata.getSender() ) )
			this.previous.add( mdata.getSender() );

		if ( this.ID == 1 ) {
			battery.spend(EnergyMode.RECEIVE);
			DataPackets = DataPackets + 1;
			Recivers = Recivers + 1;
			idMsg++;

			// An optimal tree has been calculated
			//if(CustomGlobal.computedOptimalTree){
			if ( !CustomGlobal.treeOptimized.isEmpty() && !startCorrect ) {
				System.out.println( "ENTROU\n" );
				//listTree = CustomGlobal.listTree;
				listTree = CustomGlobal.treeOptimized;
				// reinicio a arvore uma vez que o programa me retorna
				// sempre uma arvore nova
				// para teste comentei...
				// CustomGlobal.listTree = new ArrayList<String>();
				List<Integer> sents = new ArrayList<Integer>();
				for ( int item : previous ) {
					if ( !sents.contains( item ) ) {
						sents.add( item );
						MessageSPTTimer mcimsg = new MessageSPTTimer( new CorrectionMessage( this.idMsg, this.ID, listTree ), Tools.getNodeByID( item ) );
						mcimsg.startRelative( 0.0001, this );
						battery.spend(EnergyMode.SEND);
					}
				}
			}
		}
		if ( this.ID == mdata.getDest() ) {
			if ( !this.filhos.contains( (Object) sender ) ) {
				this.filhos.add( sender );
			}
			if ( ( this.myRole != Roles.SINK ) ) {
				battery.spend(EnergyMode.RECEIVE);

				if ( Global.currentTime > this.nextsenddata )
					this.senddata = true;
				this.rota = true;

				if ( ( ( this.filhos.size() < 2 ) ) ) {
					mdata.setDest( this.NextHopSink );
					mdata.setSender( this.ID );
					mdata.setHopToSink( this.HopToSink );
					MessageSPTTimer msgTimer = new MessageSPTTimer( mdata, Tools.getNodeByID( this.NextHopSink ) );
					enviados = enviados + 1;
					DataPackets = DataPackets + 1;
					msgTimer.startRelative( 0.1, this );
					battery.spend(EnergyMode.SEND);
					
					if ( this.myRole == Roles.RELAY )
						this.setColor( Color.CYAN );
				} else if ( ( this.senddata ) ) {
					mdata.setDest( this.NextHopSink );
					mdata.setSender( this.ID );
					mdata.setHopToSink( this.HopToSink );
					nextsenddata = Global.currentTime + DataRate;

					MessageSPTTimer msgTimer = new MessageSPTTimer( mdata, Tools.getNodeByID( this.NextHopSink ) );
					enviados = enviados + 1;
					DataPackets = DataPackets + 1;
					msgTimer.startRelative( DataRate, this );
					battery.spend(EnergyMode.SEND);
					this.rota = true;
					this.senddata = false;
					this.setColor( Color.black );
				}
			}
			if ( this.myRole == Roles.SINK ) {
				battery.spend(EnergyMode.RECEIVE);
				Recivers = Recivers + 1;
				PrintResult.print();
				if ( CustomGlobal.available ) {
					CustomGlobal.available = false;
					Runnable r = new ExecuteAG();
					Thread th = new Thread( r );
					th.start();
				}
			}
		}*/
	}
	
	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub
		System.out.println("postStep " + this.ID + " energy: "+ this.battery.getEnergy());
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub
		System.out.println("preStep");
		for ( sinalgo.nodes.timers.Timer t : this.getTimers() ){
			if ( t.getFireTime() < Global.currentTime ){
				System.out.println("-------------------------------------------");
				System.out.println("NOT FIRED");
			}
		}
	}

	@Override
	public void draw( Graphics g, PositionTransformation pt, boolean highlight ) {
		// TODO Auto-generated method stub
		if ( this.ID == 1 ) {
			// Node node = Tools.getNodeByID( this.ID );
			// node.setPosition( 50, 50, 0 );
			highlight = true;
		}
		super.drawNodeAsDiskWithText( g, pt, highlight, Integer.toString( this.ID ), 8, Color.WHITE );

	}

	// My methods

	@NodePopupMethod( menuText = "Meu Papel" )
	public void myRole() {
		Tools.appendToOutput( myRole + "\n" );
	}

	@NodePopupMethod( menuText = "Meu Status" )
	public void myStatus() {
		Tools.appendToOutput( mystatus + "\n" );
	}

	@NodePopupMethod( menuText = "Exibir Tabela" )
	public void myaggDist() {
		Tools.appendToOutput( "HopToSink: " + this.HopToSink + "\n" + "NextHopSink: " + this.NextHopSink + "\n" + "OwnerID:" + this.ownerID + "\n" );
	}

	public boolean insideEvent( sinalgo.nodes.Position p, int eventID ) {
		double xc = 0, yc = 0, r = 0;
		try {
			xc = sinalgo.configuration.Configuration.getDoubleParameter( "Event/Xposition" + eventID );
			yc = sinalgo.configuration.Configuration.getDoubleParameter( "Event/Yposition" + eventID );
			r = sinalgo.configuration.Configuration.getDoubleParameter( "Event/EventSize" );
		} catch ( CorruptConfigurationEntryException e ) {
			e.printStackTrace();
			System.exit( 0 );

		}
		if ( Math.pow( ( xc - p.xCoord ), 2 ) + Math.pow( ( yc - p.yCoord ), 2 ) < Math.pow( r / 2, 2 ) ) {

			this.eventnum = eventID;
			debugMsg("event " + this.eventnum + " detected by node " + this.ID);
			return true;
		} else
			return false;
	}

	public void startDetection() {
		myRole = Roles.COLLABORATOR;

		this.setColor( Color.cyan );
		// sptLog.logln("Detection t "+(Global.currentTime)
		// +" Ns "+ this.ID
		// +" Ev " +this.eventnum);
		Detects = Detects + 1;
		this.mystatus = Status.MONITORING;
		timerTREE = new GATimer( this, TNO.MONITORING );
		timerTREE.tnoStartRelative( DataRate+10, this, TNO.MONITORING );
	}

	// Node Sink start the Flooding of MCI
	public void sendMCI() {
		this.HopToSink = 0; 	// I am Sink
		this.NextHopSink = this.ID; // I am SInk
		//MessageSPTTimer mcimsg = new MessageSPTTimer( new MCIMessage( this.HopToSink, this.ID, this.numberOfNodes ) );
		//MessageSPTTimer mcimsg = new MessageSPTTimer( new MCIMessage( this.HopToSink, this.ID, 1) );
		MessageSPTTimer mcimsg = new MessageSPTTimer( new MCIMessage( this.HopToSink, this.ID) );
		mcimsg.startRelative( 0.001, this );
		Overheads = Overheads + 1;
	}

	public void sendData() {
		if ( Global.currentTime > eventEndTime ) {
			if ( this.filhos.size() == 0 ) {
				aproximationtree();
				this.setColor( Color.BLACK );
			}
			return;
		}

		this.setColor( Color.orange );

		if ( ( this.myRole == Roles.COLLABORATOR ) && ( this.filhos.size() == 0 ) ) {
			this.setColor( Color.RED );
			
			if(!this.requestedRoute){
//				RequestRouteMessage req = new RequestRouteMessage(this.ID, this.ID);
				RequestRouteMessage req = new RequestRouteMessage(this.ID);
				req.add(this.ID);
				MessageSPTTimer routeTimer = new MessageSPTTimer(req, Tools.getNodeByID( this.NextHopSink ));
				routeTimer.startRelative(0.0001, this);
				Overheads += 1;
				this.battery.spend(EnergyMode.SEND);
				this.requestedRoute = true;
			}
			
			
			this.timerTREE.tnoStartRelative( DataRate, this, TNO.MONITORING );
			//Message mdata = new GADataMessage( this.ID, this.NextHopSink, "Sink", this.HopToSink );
			//Message mdata = new GADataMessage( this.ID, this.NextHopSink, new StringBuffer(this.ID + "," + this.battery.getEnergy()), this.HopToSink );
			Message mdata = new GADataMessage( this.ID, this.NextHopSink, new StringBuffer(this.ID + "," + this.battery.getTotalSpentEnergy()), this.HopToSink , 0.0d);
			((GADataMessage)mdata).accumDTime();
//			Message mdata = new GADataMessage( this.ID, this.NextHopSink, new StringBuffer(this.ID + "," + this.battery.getTotalSpentEnergy()), this.HopToSink);
			MessageSPTTimer msgTimer = new MessageSPTTimer( mdata, Tools.getNodeByID( this.NextHopSink ) );
			msgTimer.startRelative( DataRate, this );
			// Tools.appendToOutput( "SData :" + this.ID + "\n" );
			enviados = enviados + 1;
			Notifications = Notifications + 1;
			DataPackets = DataPackets + 1;
			
			this.battery.spend(EnergyMode.SEND);
		}
	}


	public void timeout( TNO tno ) {
		switch ( tno ) {
			case TREE:
				sendMCI(); // Make one Flooding of MCI (Message of Configuration
							// Initial)
				break;

			case MONITORING:
				if ( this.myRole == Roles.COLLABORATOR ) {
					this.rota = true;
					sendData();
				}
				break;

			case SCHEDULE_FEEDBACK:
				//scheduleFeedback();
				break;

		}
	}
	
	@NodePopupMethod( menuText = "Neighbors" )
	public void neighbors() {
		Tools.appendToOutput( "I have " + this.listAll.size() + " Neighbors :" + this.listAll + "\n" );
		Tools.appendToOutput("My hopToSink "+this.HopToSink +"\n");
		for(Integer i: this.listAll){
			Tools.appendToOutput("ID "+i+ " hopToSink "+ ((GANode)Tools.getNodeByID(i)).getHopToSink() +"\n");
		}
	}

	@NodePopupMethod( menuText = "qtd filhos" )
	public void filhos() {
		Tools.appendToOutput( "QTD Neighbors " + this.childrenNodes + "\n" );
	}

	@NodePopupMethod( menuText = "rec filhos" )
	public void filhosrec() {
		Tools.appendToOutput( "QTD rec " + this.receivedNodes + "\n" );
	}

	@NodePopupMethod( menuText = "Son" )
	public void son() {
		Tools.appendToOutput( "My son " + this.son + "\n" );
	}

	@NodePopupMethod( menuText = "Previous" )
	public void previous() {
		Tools.appendToOutput( "My previous " + this.previous + "\n" );
	}
	
	@NodePopupMethod(menuText="NeighborHopBigger")
	public void NeighborHopBigger() {
		Tools.appendToOutput("NHB "+this.NeighborHopBigger+" size: "+this.NeighborHopBigger.size()+"\n");
	}
	@NodePopupMethod(menuText="ReceivedNeighborHopBigger")
	public void ReceivedNeighborHopBigger() {
		Tools.appendToOutput("RNHB "+this.ReceivedNeighborHopBigger+"size: "+this.ReceivedNeighborHopBigger.size()+"\n");
	}
	@NodePopupMethod(menuText="TimerTree") //Timer that keeps sending data
	public void TimerTree() {
		if (timerTREE!=null){
			Tools.appendToOutput("TimerTree set for node" + this.ID  + " ? "+this.timerTREE.isNodeTimer()+"\n");
			Tools.appendToOutput("TimerTree fire time: " + this.timerTREE.getFireTime()+"\n");
		}else{
			Tools.appendToOutput("NO timertree for node " + this.ID + "\n");
		}
			
		
	}
	@NodePopupMethod(menuText="Energy")
	public void energy() {
		Tools.appendToOutput("Remaining energy for node " + this.ID + ": " +this.battery.getEnergy()+"\n");
		float min = this.battery.getEnergy();
		int node = this.ID;
		for (Node n : Tools.getNodeList()){
			if (min>((GANode)n).getBattery().getEnergy() && this.ID!=1 && !GANode.terminals.contains(n.ID-1) && GANode.UpNodes.get(n.ID-1)==1){	//Not counting terminals!
				min = ((GANode)n).getBattery().getEnergy();
				node = n.ID;
			}
		}
		Tools.appendToOutput("Node "+node+" has the lower residual energy of "+min+"\n");
	}
	
	@NodePopupMethod(menuText="DeadDisconnectedNodes")
	public void listDeadAndDisconnected(){
		Tools.appendToOutput("Dead nodes: ");
		for(int i=0; i<GANode.UpNodes.size(); i++)
			if ( GANode.UpNodes.get(i) == 0 )	//Dead Node!
				Tools.appendToOutput((i+1) + " ");
		Tools.appendToOutput("\n");
		Tools.appendToOutput("Disconnected nodes: ");
		for(int i=0; i<GANode.DisconnectedNodes.size(); i++)
			if ( GANode.DisconnectedNodes.get(i) == 0) //Disconnected node!
				Tools.appendToOutput((i+1) + " ");
		Tools.appendToOutput("\n");
	}
	
	@NodePopupMethod(menuText="eventQueue")
	public void debugEventQueue() {
		System.out.println("-------------------------------------------");
//		System.out.println("EventQueue Size: " + Tools.getEventQueue().size() + " time " + Tools.getEventQueue().getNextEvent().time);
		System.out.println("-------------------------------------------");
	}

	public SimpleEnergy getBattery() {
		return battery;
	}

	public void setBattery(SimpleEnergy battery) {
		this.battery = battery;
	}

	/*public IEnergy getBateria() {
		return battery;
	}*/
	public boolean isGraphConnected(){
		for (Node n : Tools.getNodeList()){
			int edges = n.outgoingConnections.size();
			if (edges==0)
				return false;
		}
		return true;
	}
	
	public boolean isNodeConnected(){
		int edges = this.outgoingConnections.size();
		if (edges==0)
			return false;
		else
			return true;
	}

	public int getHopToSink(){
		return this.HopToSink;
	}
	
	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}


}
