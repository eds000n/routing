	package projects.HCCRFD.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import projects.HCCRFD.nodes.messages.EnergyLevelMessage;
import projects.GA.utils.NEATOWriter;
import projects.HCCRFD.nodes.messages.BeaconMessage;
import projects.HCCRFD.nodes.messages.DeadNodeMessage;
import projects.HCCRFD.nodes.messages.HelloMessage;
import projects.HCCRFD.nodes.messages.MSGTREE;
import projects.HCCRFD.nodes.messages.MessageBroadcastCHID;
import projects.HCCRFD.nodes.messages.RequestRouteMessage;
import projects.HCCRFD.nodes.messages.SetRepairRouteMessage;
import projects.HCCRFD.nodes.messages.HCCRFDDataMessage;
import projects.HCCRFD.nodes.messages.HCCRFDInformHopCount;
import projects.HCCRFD.nodes.messages.HCCRFDJoinAccept;
import projects.HCCRFD.nodes.messages.HCCRFDJoinRequest;
import projects.HCCRFD.nodes.messages.HCCRFDRequestReply;
import projects.HCCRFD.nodes.timers.BorderCollectInformationTimer;
import projects.HCCRFD.nodes.timers.BroadcastCHIDTimer;
import projects.HCCRFD.nodes.timers.ClusterFormationTimer;
import projects.HCCRFD.nodes.timers.EndSPTTimer;
import projects.HCCRFD.nodes.timers.EventSPTTimer;
import projects.HCCRFD.nodes.timers.GenConfigurationTimer;
import projects.HCCRFD.nodes.timers.HCCRFDGenTimer;
import projects.HCCRFD.nodes.timers.MessageSPTTimer;
import projects.HCCRFD.nodes.timers.NNTableCreationTimer;
import projects.HCCRFD.nodes.timers.RFDConfigurationTimer;
import projects.HCCRFD.nodes.timers.RepairDeadNodeTimer;
import projects.HCCRFD.nodes.timers.HCCRFDTimer;
import projects.HCCRFD.nodes.timers.VerifyBorderNodeTimer;
import projects.HCCRFD.utils.NN_TableItem;
import projects.SPT.nodes.nodeImplementations.SPTNode.Roles;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.io.eps.Exporter;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.models.EnergyModel.simple.SimpleEnergy;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.runtime.Global;
import sinalgo.runtime.events.Event;
import sinalgo.runtime.nodeCollection.NodeCollectionInfoInterface;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.Distribution;
import sinalgo.tools.statistics.UniformDistribution;

public class HCCRFDNode extends Node {
//	private HashMap<EventKey, RoutingTableEntry> routingtable = new HashMap<EventKey, RoutingTableEntry>();
	
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
	private boolean senthello = false;
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
	
	public static int Notifications = 0;
	public static double SimulationTime = 0;
	public static Logging Energy = Logging.getLogger( "HCCRFDEnergy.txt", true );
	public static Logging debugLog = Logging.getLogger();	//Prints to the console
	//public double battery = 0.0;
	private SimpleEnergy battery;
	public List<Integer> previous = new ArrayList<Integer>();
	// private IEnergy bateria;

	private UniformDistribution uniformRandom = new UniformDistribution( 0, 0.016 );
	// Logging sptLog;
	public static Logging HCCRFD = Logging.getLogger( "HCCRFDLog.txt", true );

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
	}

	private enum Status {
		MONITORING,		//if detected an event
		READY,			//alive and ready to transmit!
		RELAY,			//node that agregates data
		DEAD,			//with no battery left
		DISCONNECTED,	//alive but disconnected from sink
	};

	public enum Roles {
		SINK,
//		COLLABORATOR,	//names: source node, node that detected an event
//		RELAY,
		UNSET,
		CH,
		CM
	};

	public enum TNO {
		TREE,
		MONITORING,
		SCHEDULE_FEEDBACK,
		TNO_FEEDBACK
	};

	private HCCRFDTimer timerTREE;
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
	public LinkedList<Integer> listAll = new LinkedList<Integer>();	//A list containing the IDs of ALL the neighbor nodes, 1-indexed
	//private Map<Node, LinkedList<Integer>> mapNeibohrs = new HashMap<Node, LinkedList<Integer>>();
	public static HashMap<Node, LinkedList<Integer>> mapVizinhosLocal = new HashMap<Node, LinkedList<Integer>>();
	private int childrenNodes = 0;
	private int receivedNodes = 0;
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
//	static Thread threadHCCRFD = new Thread( new ExecuteAG((HCCRFDNode)Tools.getNodeByID(1)));					//Thread executing the BRKHCCRFD algorithm
	public static ArrayList<Integer> terminals = new ArrayList<Integer>();		//List of terminal nodes, 0-indexed
	private boolean isDead = false ;
	
	private int isBorderNode = 0; //0: unknown, 1: is border node, 2: is NOT border node
	
	public ArrayList<Integer> node1 = new ArrayList<Integer>();
	public ArrayList<Integer> node2 = new ArrayList<Integer>();

//	public boolean requestedRoute = false;

	private boolean runUpdateAdjacencyMatrix = false;
	
	/*********************************************************/
	/****************** HCCRFD params ************************/
	public static ArrayList<Double> rnd = new ArrayList<>();	//List of RND sent by the nodes
	public static ArrayList<Double> RE = new ArrayList<>();				//Remaining Energy of the nodes
	public static ArrayList<Integer> last_round_ch = new ArrayList<>();	//last_round node i was elected as CH
	public static double Eavg;									//Average energy of the network
	public ArrayList<NN_TableItem> NN_Table;					//Table with the neighbors information
	public boolean is_configured = false;						//boolean flag telling if the node is configured, or not.
	public boolean have_retransmitted = false;					//boolean flag for counting the number of nodes in the tree.
	public int my_ch = -1;										//id of the CH, -1 if the node is a CH itself
	public ArrayList<Integer> CMs = new ArrayList<>();			//List of nodes that are member of this cluster, empty if this is a CM
	public static ArrayList<Integer> CHs = new ArrayList<>();	//List of CHs, cleaned at each round 1-indexed
	/********************** RFDMRP ***************************/
	public int region;											//The sink/ch divides the network into regions, 1-indexed
	public double max_dist;										//Maximum distance between the sink/ch and a ch/cm
	public boolean rfd_is_configured = false;					//flag to mark a node a configured for RFD
	private int hop_count = 100000; 							// Distance in Hops to Sink/HC
	/****************** Config params ************************/
	public static int RoundFreq = 0;
	public static int n_rnd = 1;								//current round
	/*********************************************************/
	
	
	@Override
	public void handleMessages( Inbox inbox ) {
		// TODO Auto-generated method stub
		int sender;
		
		if ( HCCRFDNode.DisconnectedNodes.get(this.ID-1)== 0 || HCCRFDNode.UpNodes.get(this.ID-1)==0 || getBattery().getEnergy()<= 0 )	// The nodes just process the packets as long as they are awake (have energy) 
			return;
		
		while ( inbox.hasNext() ) {
			Message msg = inbox.next();
			sender = inbox.getSender().ID;
			battery.spend(EnergyMode.RECEIVE);
			
//			if ( this.ID == 120 && Tools.get){
//				
//			}
			
			// Sink start the flooding of HelloMessage for initial configuration 
			if ( msg instanceof HelloMessage ) {
				InitialConfiguration(msg);
				//InitialConfiguration2(msg);
			}
			
			// Just the return of the message with energy levels and that stuff
			if (msg instanceof EnergyLevelMessage){
				CollectInformation(msg);
			}

			// Sent information about event to Sink
			if ( msg instanceof HCCRFDDataMessage ) {
				SendDataEvent(msg, sender);
			}

			if ( msg instanceof MessageBroadcastCHID ){
				BroadcastNConfigure(msg);
			}
			
			if ( msg instanceof HCCRFDRequestReply ){
				SetNNTable(msg);
			}
			
			if ( msg instanceof HCCRFDJoinRequest ){
				ProcessJoinRequest(msg);
			}
			
			if ( msg instanceof HCCRFDJoinAccept ){
				ProcessJoinAccept(msg);
			}
			
			if ( msg instanceof HCCRFDInformHopCount ){
				SetHopCount(msg);
			}
			
			if (msg instanceof DeadNodeMessage){		//Message sent from the dead node to the sink
				ReportDeadNode(msg);
			}
			
			/*if ( msg instanceof MSGTREE ) { //I guess this is not necessary since the sink has the last computed routing tree.
				GetRoutingTree(msg);
			}
			
			if (msg instanceof SetRepairRouteMessage){	//Message to set repair (fix) the route of the nodes that became orphan
				SetRepairRoute(msg);
			}*/
			
			if (msg instanceof MSGTREE){
				GetRoutingTree(msg);	
			}
		}
	}

	private void SetHopCount(Message msg) {
		HCCRFDInformHopCount m = (HCCRFDInformHopCount)msg;
		if ( !this.rfd_is_configured && HCCRFDNode.DisconnectedNodes.get(this.ID-1)==1 ){		//process only if the node haven't been configured yet
			if ( this.my_ch != -1 ){		//I am CM
				if ( m.getBase_id() == this.my_ch ){	//Only process the packets from my CH
					this.rfd_is_configured = true;
					this.hop_count = m.getHop_count();
					HCCRFDInformHopCount mm = new HCCRFDInformHopCount(m.getBase_id(), this.ID, m.getHop_count()+1);
					HCCRFDGenTimer t = new HCCRFDGenTimer(mm);
					t.startRelative(0.0001, this);
					this.battery.spend(EnergyMode.SEND);
					Overheads++;
				}
			}else if ( this.my_ch == -1 ){	// I am CH, I forward the message to other CHs and also configure my CMs
				if ( m.getBase_id() == 1 ){
					this.rfd_is_configured = true;
					//Forward the message to other CHs
					this.hop_count = m.getHop_count();
					HCCRFDInformHopCount mm1 = new HCCRFDInformHopCount(m.getBase_id(), this.ID, m.getHop_count()+1);
					HCCRFDGenTimer t1 = new HCCRFDGenTimer(mm1);
					t1.startRelative(0.0001, this);
					this.battery.spend(EnergyMode.SEND);
					Overheads++;
					//Configure my CMs
					HCCRFDInformHopCount mm2 = new HCCRFDInformHopCount(this.ID, this.ID, m.getHop_count()+1);
					HCCRFDGenTimer t2 = new HCCRFDGenTimer(mm2);
					t2.startRelative(0.0002, this);
					this.battery.spend(EnergyMode.SEND);
					Overheads++;
				}
			}
		}
	}

	private void ProcessJoinAccept(Message msg) {
		HCCRFDJoinAccept ja = (HCCRFDJoinAccept) msg;
		if ( ja.getNode_id() == this.ID ){
			
		}
	}

	/**
	 * processing the join request sent by a CM
	 * @param msg
	 */
	private void ProcessJoinRequest(Message msg) {
		HCCRFDJoinRequest jr = (HCCRFDJoinRequest)msg;
		if ( jr.getCh_id() == this.ID ){
			this.CMs.add(jr.getNode_id());
			HCCRFDJoinAccept ja = new HCCRFDJoinAccept(this.ID, jr.getNode_id());
			HCCRFDGenTimer t = new HCCRFDGenTimer(ja);
			t.startAbsolute(0.001, this);
		} else {
			//HCCRFDGenTimer t = new HCCRFDGenTimer(jr);
			//t.startAbsolute(0.001, this);
			;
		}
	}

	/**
	 * Processing of the HCCRFDRequestReply which is for updating the NNTable 
	 * @param msg
	 */
	private void SetNNTable(Message msg) {
		HCCRFDRequestReply rr = (HCCRFDRequestReply) msg;
		if ( rr.getNeighboringnode_id() == -1 && this.my_ch == -1 ){	//This is a REQUEST sent by a CM and should only be processed by a CH
			float ds = getDist(rr.getSource_id(), this.ID);
			NN_Table.add(new NN_TableItem(rr.getSource_id(), rr.getHop_count(), (float)rr.getRemaining_energy(), ds, ds));
		}else if ( rr.getSource_id() == -1 ){		//This is a REPLY sent by a CH and should only be processed by the requesting CM
			
		}
	}
	
	/**
	 * Forward Node Selection 
	 * @param dest is the destination node, if its within the communication radius, i. e., its a neighbor node, just send the packets directly
	 * otherwise use the RFD logic by choosing the node with higher probability p_ij
	 * @return
	 */
	public int FNS(int dest){
		float E_t = (float) (0.2*getBattery().getInitialEnergy());	//Threshold of 20%
		if ( getBattery().getEnergy() < E_t ){	
			;//setAsDead();
		}
		
		if ( getDist(this.ID, dest) <= CommunicationRadius )
			return dest;
		
		float sum = 0;
		int flag = 0;
		for ( int i=0; i<NN_Table.size(); i++ ){
			NN_TableItem n_j = NN_Table.get(i);
			if ( n_j.getRe_nn() >= E_t ){
				//int h = this.hop_count - n_j.getHc();
				float h = hop_count_difference(n_j);
				if( h >= 0 ){
					sum += h;
					flag = 1;
				}
			}
		}
		if ( flag == 0 )
			return dest;
		
		float maximum = 0;
		int forward_node = 0;
		
		for ( int i=0; i<NN_Table.size(); i++ ){
			NN_TableItem n_j = NN_Table.get(i);
			if ( n_j.getRe_nn() >= E_t ){
				//int h = this.hop_count - n_j.getHc();
				//int h =       i        -      j;
				float h = hop_count_difference(n_j);
				if( h >= 0 ){
					if( sum > 0){
						float p_ij = (float) (h*1.0/sum);
						if ( maximum < p_ij ){
							maximum = p_ij;
							forward_node = n_j.getNn();
						}
					}else{
						if ( getDist(this.ID, dest) > getDist(n_j.getNn(),dest) )
							forward_node = 	n_j.getNn();
						else
							forward_node = dest;
					}
				}
			}
		}
		return forward_node;
	}
	
	float hop_count_difference(NN_TableItem j){
		return ( ( this.hop_count - j.getHc() ) / getDist(this.ID, j.getNn()) )*j.getRe_nn();
	}
	
	private float getDist(Node i, Node j){
		float dx = (float) (i.getPosition().xCoord - j.getPosition().xCoord);
		float dy = (float) (i.getPosition().yCoord - j.getPosition().yCoord);
		return (float) Math.sqrt( Math.pow(dx, 2) + Math.pow(dy, 2) );
	}
	
	private float getDist(int i, int j){
		return getDist(Tools.getNodeByID(i), Tools.getNodeByID(j));
	}
	
	/**
	 * Function to process the MessageBroadcastCHID message
	 * @param msg
	 */
	private void BroadcastNConfigure(Message msg) {
		MessageBroadcastCHID mb = (MessageBroadcastCHID)msg;
		if ( !this.is_configured ){
			if ( mb.getChids().contains(this.ID) ){
				myRole = Roles.CH;
				setColor(Color.RED);
				my_ch = -1;
			}
			else{
				myRole = Roles.CM;
				setColor(Color.GREEN);
				
				int idx = mb.getCms().indexOf(this.ID);
				my_ch = mb.getCmch().get(idx); 
				///////////////////////////////////////////
				
				///////////////////////////////////////////
				//for (int i=0; )
				/*HCCRFDJoinRequest jreq = new HCCRFDJoinRequest(this.ID, chid);
				HCCRFDGenTimer t = new HCCRFDGenTimer(jreq);
				t.startRelative(0.1, n);*/
			}
			
			is_configured = true;
			BroadcastCHIDTimer t = new BroadcastCHIDTimer(mb);
			t.startRelative(0.001, this);
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
	 * Method to process the DeadNodeMessage. The sink udpates the UpNodes array and runs the algorithm so that now the node isn't
	 * considered anymore. The relay nodes just retransmit it to the sink;
	 * @param msg
	 */
	private void ReportDeadNode(Message msg) {
		DeadNodeMessage dnm = (DeadNodeMessage)msg;
		battery.spend(EnergyMode.LISTEN);
		if (this.ID==1) {//Sink
			debugMsg(">>> SINK recieved ReportDeadMessage from node " + dnm.getNodeID() + " reported itself as dead with battery of " + dnm.getReportedEnergy(), 2);
			//debugMsg(">>> HCCRFDNode.UpNodes " + HCCRFDNode.UpNodes.get(dnm.getNodeID() - 1));
			HCCRFDNode.UpNodes.set(dnm.getNodeID() - 1, 0);
			HCCRFDNode.DisconnectedNodes.set(dnm.getNodeID()-1, 0);
			
			updateConnectedComponents(dnm.getNeighbors());		//Updates the connected components and fixes, if possible, the neighbors of the dead node so they don't lose connectivity.
			
			//debugMsg(">>> HCCRFDNode.UpNodes " + HCCRFDNode.UpNodes.get(dnm.getNodeID() - 1));
			int dn = 0;
			for ( Integer i : DisconnectedNodes )
				dn += i;
			debugMsg(">>> SINK number of connected nodes " + dn);
			
			/*debugMsg("Reconfiguring due to DeadNode");
			if ( CHs.contains(dnm.getNodeID())){	//Only reconfigure if it a CH
				GenConfigurationTimer ctimer = new GenConfigurationTimer();
				ctimer.startRelative(0.1, this);
			}*/
			
		}else{
			if ( this.my_ch == -1 )	//I am CH
				send(dnm, Tools.getNodeByID(FNS(1)));
			else
				send(dnm, Tools.getNodeByID(FNS(this.my_ch)));
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
		this.hop_count = srrm.getHopsToSink();
//		if ( this.ID == ((HCCRFDNode)Tools.getNodeByID(this.NextHopSink)).NextHopSink )
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
	 * the HCCRFD does not consider battery use, this is not implemented!
	 * FIXME: not implementing battery methods
	 * @param msg
	 */
	private void CollectInformation(Message msg) {
		EnergyLevelMessage borderMessage = (EnergyLevelMessage)msg;
		if (this.isBorderNode==1)
    		return;
    	
    	if(NeighborHopBigger.contains(borderMessage.getSenderID())){
    		
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
    				borderTimer.startRelative(this.hop_count+0.1,this);
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
				if (HCCRFDNode.UpNodes.get(v)==0)
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
		if (!found && HCCRFDNode.UpNodes.get(v2)==1){//If it was not found and is up (has energy) then mark it as disconnected
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
				if (HCCRFDNode.DisconnectedNodes.get(v)==0)
					continue;
				if ( !visited.get(v) ) {
					visited.set(v, true);
					q.add(v);
				}
			}
		}
	}
	
	/**
	 * @param v1 node where to start the bfs. 0-indexed
	 * @param visited
	 */
	public void bfsRFD(int v1, ArrayList<Integer> order){
		Queue q = new LinkedList<Integer>();
		q.add(v1);
		ArrayList<Boolean> visited = new ArrayList<Boolean>(Collections.nCopies(AdjList.length, false));
		visited.set(v1, true);
		order.add(v1);
		while ( !q.isEmpty() ){
			int u = (int) q.poll();
			for(int i=0; i<AdjList[u].size(); i++){
				int v = AdjList[u].get(i);
//				if (HCCRFDNode.DisconnectedNodes.get(v)==0)
//					continue;
				if ( !visited.get(v) ) {
					visited.set(v, true);
					q.add(v);
					order.add(v);
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
				if (HCCRFDNode.DisconnectedNodes.get(v)==0)
					continue;
				if (visited.get(v)==0){//not yet visited
					visited.set(v, 1);//Mark as visited
					q.add(v);
					HCCRFDNode.DisconnectedNodes.set(v, 0);
					//if ( HCCRFDNode.terminals.contains(v) )
					//	HCCRFDNode.ter .set(v, 0);
					/*if ( HCCRFDNode.terminals.contains(v) ){
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
//				RepairDeadNodeHCCRFDTimer rtimer = new RepairDeadNodeHCCRFDTimer(this);
			}
		for ( int i=0; i<HCCRFDNode.DisconnectedNodes.size(); i++ ){
			if (HCCRFDNode.DisconnectedNodes.get(i)==1 && !visited.get(i))
				bfsMarkDisconnected(i);
		}
		
		checkTerminalConnectivity();
	}
	
	private void checkTerminalConnectivity() {
		int n = 0;
		for(int i=0; i<HCCRFDNode.terminals.size(); i++){
			if ( HCCRFDNode.DisconnectedNodes.get(HCCRFDNode.terminals.get(i)) == 0 ){
				eraseTimers(HCCRFDNode.terminals.get(i)+1);
				n++;
			}
		}
		/*if ( n == HCCRFDNode.terminals.size() ){
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
			BorderCollectInformationTimer borderTimer = new BorderCollectInformationTimer(new EnergyLevelMessage(this.ID, this.getBattery().getEnergy()), this);
			borderTimer.startRelative(0.001, this);
			Overheads+=1;
		}else
			isBorderNode = 2;
	}
	
	private void createAdjacencyMatrix(EnergyLevelMessage msg) {
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
				HCCRFDNode u = (HCCRFDNode) Tools.getNodeByID(i+1);
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
			HCCRFDNode.UpNodes.set(v.ID - 1, 0);		//Mark it as dead
			HCCRFDNode.DisconnectedNodes.set(v.ID - 1, 0);	//Mark it as disconnected
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
//		MessageSPTTimer msgtree = new MessageSPTTimer( new MSGTREE( 1, this.NextHopSink, this.ID, "Sink" ) );
		MessageSPTTimer msgtree = new MessageSPTTimer( new MSGTREE( 1, FNS(1), this.ID, "Sink" ) );
		double time = uniformRandom.nextSample();
		msgtree.startRelative( time, this );
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		myRole = Roles.UNSET;
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
			
			/*for (int i = 1; i <= sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents"); i++) {
				EventEndHCCRFDTimer t = new EventEndHCCRFDTimer(i,this);
				t.startAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventEnd"+i), this);
			}*/
			
			DataRate = sinalgo.configuration.Configuration.getIntegerParameter( "Event/DataRate" );
			EventSize = sinalgo.configuration.Configuration.getIntegerParameter( "Event/EventSize" );
			CommunicationRadius = sinalgo.configuration.Configuration.getIntegerParameter( "UDG/rMax" );
			DropRate = sinalgo.configuration.Configuration.getIntegerParameter( "TaxadePerda/dropRate" );
			RoundFreq = sinalgo.configuration.Configuration.getIntegerParameter("RoundFreq");
			
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

		if ( this.ID == 1 ) {
			this.setColor( Color.RED );
			this.hop_count = 0;
			this.myRole = Roles.SINK;
			this.mystatus = Status.READY;
			//this.numberOfNodes = Random.numberOfNodes;
			this.generateEvent = 1;
			
			//if (!isGraphConnected())1
			//	sinalgo.tools.Tools.exit();
			
			//this.nodes.add(new Vertex(this.ID, 1, this.getPosition().xCoord, this.getPosition().yCoord, this.getBattery().getEnergy()));
			//this.nodes.add(new Vertex(this.ID, 1, this.getPosition().xCoord, this.getPosition().yCoord, 0));
			UpNodes.add(1);
			DisconnectedNodes.add(1);
			RE.add(0d);
			rnd.add(0d);
			last_round_ch.add(-30);
			sendMCI();
			
			
			GenConfigurationTimer ctimer = new GenConfigurationTimer();
			ctimer.startRelative(1999, this);
			
		}else{
			UpNodes.add(1);
			DisconnectedNodes.add(1);
			RE.add(0d);
			rnd.add(0d);
			last_round_ch.add(-30);
			//this.nodes.add(new Vertex(this.ID, 0, this.getPosition().xCoord, this.getPosition().yCoord, this.getBattery().getEnergy()));
			//this.nodes.add(new Vertex(this.ID, 0, this.getPosition().xCoord, this.getPosition().yCoord, 0));
		}
		NN_Table = new ArrayList<>();

		VerifyBorderNodeTimer borderTimer = new VerifyBorderNodeTimer(this);
		borderTimer.startAbsolute(1000,this);
		//if (!isNodeConnected())
		//	sinalgo.tools.Tools.exit();

		
		// Event timers
		EndSPTTimer etimer = new EndSPTTimer();
		etimer.startAbsolute( SimulationTime, this );

	}


	public void ReconfigHCCRFD(){
		int edges = 0;
		for ( Node n : Tools.getNodeList() ){
			if ( ((HCCRFDNode)n).have_retransmitted == true )
				edges++;
		}
		//if ( HCCRFDNode.Edges < edges)
		HCCRFDNode.Edges += edges;
		HCCRFDNode.Edges += HCCRFDNode.terminals.size();
		
		//Cluster selection and formation
		ClusterFormationTimer chtimer = new ClusterFormationTimer(); 
//		chtimer.startAbsolute(1500, this);
		chtimer.startRelative(0.0001, this);
		
		//Setup the hop_count for CHs with respec to to the sink and for CMs with respect to its CH
		RFDConfigurationTimer rfdtimer = new RFDConfigurationTimer();
//		rfdtimer.startAbsolute(1501, this);
		rfdtimer.startRelative(1.0001, this);
		
		//Creation of NN Table
		NNTableCreationTimer nntimer = new NNTableCreationTimer();
//		nntimer.startAbsolute(1502, this);
		nntimer.startRelative(2.0001, this);
		
		n_rnd++;
		GenConfigurationTimer gt = new GenConfigurationTimer();
		gt.startRelative(RoundFreq*DataRate, this);
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Initial flooding of the network with MCIMessage to set the hopToSink and HextHopSink
	 * @param msg
	 */
	public void InitialConfiguration(Message msg){
		HelloMessage mcimsg = (HelloMessage) msg;
		
		/*if (this.checkConnectivity){
			if (!isGraphConnected())
				sinalgo.tools.Tools.exit();
			this.checkConnectivity=false;
		}*/
		
		if ( !listAll.contains( mcimsg.getSenderID() ) ) {
			listAll.add( mcimsg.getSenderID() );
		}

		if ( this.hop_count > (mcimsg.getHopToSink()+1) ) {
			this.setColor( Color.GREEN );			
			
			this.NextHopSink = mcimsg.getSenderID();
			if (this.NextHopSink == 1)
				this.battery.setIsSinkNeighbor(true);
			this.hop_count = mcimsg.getHopToSink()+1;

			//if(!this.sentmci){
				HelloMessage nmci = new HelloMessage(this.hop_count, this.ID);
				MessageSPTTimer MCICNS = new MessageSPTTimer( nmci );
				//MCICNS.startRelative( 0.0001, this );
				MCICNS.startRelative(this.hop_count+0.1,this);
				battery.spend(EnergyMode.SEND);
				Overheads = Overheads + 1;
				this.senthello = true;
				Tools.appendToOutput("handleMessages() HelloMessage Overheads: " + this.Overheads + "\n");
		}
	}
	
	/**
	 * After a time (1 second) every node verifies whether its a border node; in such a case 
	 * it sends the BorderMessage 
	 */
	public void SetNeighborHopBigger(){
		NeighborHopBigger.clear();
		for(Integer i: this.listAll){
			if ( this.hop_count < ((HCCRFDNode)Tools.getNodeByID(i)).getHopToSink())
				NeighborHopBigger.add(i);
		}
		if (NeighborHopBigger.size()==0){
			this.isBorderNode = 1;
			this.setColor(Color.ORANGE);
			BorderCollectInformationTimer borderTimer = new BorderCollectInformationTimer(new EnergyLevelMessage(this.ID, this.battery.getEnergy()), this);
			borderTimer.startRelative(0.001, this);
			Overheads+=1;
		}
		else
			this.isBorderNode = 2;
	}
	
	/**
	 * method called by the DeadNodeRepairHCCRFDTimer:
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
			HCCRFDNode neighbor_node = (HCCRFDNode)Tools.getNodeByID(this.listAll.get(i)); 
			neighbor_node.battery.spend(EnergyMode.RECEIVE);
	
			if ( neighbor_node.listAll.contains(this.ID) ){
				neighbor_node.listAll.removeFirstOccurrence(this.ID);
				//neighbor_node.listAll.remove(this.ID);
			}
			int d = 0;
			if ( neighbor_node.my_ch == -1 ) //I am CH
				d = FNS(1);
			else
				d = FNS(neighbor_node.my_ch);
//			if ( neighbor_node.NextHopSink == this.ID ){
			if ( d == this.ID ){
				neighbor_node.battery.spend(EnergyMode.SEND);
				//MessageSPTTimer routeTimer = new MessageSPTTimer(rrm, Tools.getNodeByID( this.NextHopSink ));
				//routeTimer.startRelative(0.001, this);
				Overheads += 1;
				neighbors.add(this.listAll.get(i));
			}
		}
		DeadNodeMessage dnm = new DeadNodeMessage(iD, this.battery.getEnergy());
		dnm.setNeighbors(neighbors);
		//this.send(dnm, Tools.getNodeByID(this.NextHopSink));
		this.send(dnm, Tools.getNodeByID(FNS(1)));
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
		if ( this.ID == 1 ) 
			System.out.println( "MSGTREE nextHop " + msgtree.getNexthop() + " destino " + msgtree.getDest() );
		if ( this.ID == msgtree.getNexthop() ) {
			this.filhosrecv.add( msgtree.getSentnode() );

			if ( this.myRole != Roles.SINK ) {
				this.setColor( Color.yellow );
				this.Disttree = this.Disttree + msgtree.getDisttree();
				this.Disttreerecv = this.Disttreerecv + 1;
				int n = FNS(1);
				if ( this.filhos.size() == 1 ) {
					
					this.filhossend.add( n );
					broadcast( new MSGTREE( this.Disttree + 1, n, this.ID ) );
					this.setColor( Color.black );
				} else if ( this.filhos.size() == this.Disttreerecv ) {
					broadcast( new MSGTREE( this.Disttree + 1, n, this.ID ) );
					this.filhossend.add( n );
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
				Edges = this.Disttree + 1;
			}
		}
	}
	
	/**
	 * Cluster formation
	 */
	public void clusterFormation(){
		// Round initialization: cleaning previous cluster information
//		ArrayList<Integer> order = new ArrayList<>();
//		ArrayList<Integer> order = new ArrayList<>(Collections.nCopies(AdjList.length, 0));
//				bfsRFD(0, order);
		//		System.out.print(" == order: ");
		//		for(int i=0; i<order.size(); i++ )
		//			System.out.print( order.get(i) + " " );
		//		System.out.println();
		float e_factor = 1.0f;
		boolean transfer = false;
		//		while ( true ){

		//			for ( int i=0; i<order.size(); i++ ){
		//				Node n = Tools.getNodeByID(order.get(i)+1);
		for ( Node n : Tools.getNodeList() ){
			((HCCRFDNode)n).is_configured = false;
			((HCCRFDNode)n).have_retransmitted = false;
			// Since I am not actually exchanging packets for updating the RE and the rnd, then I just "simulate" the exchange by 
			// spending the battery accordingly
			if ( !transfer ){
				((HCCRFDNode)n).getBattery().spend(EnergyMode.RECEIVE);
				((HCCRFDNode)n).getBattery().spend(EnergyMode.SEND);
				Overheads +=1 ;
				transfer = true;
			}

			HCCRFDNode.RE.set(n.ID-1, (double)(((HCCRFDNode)n).getBattery().getEnergy()) );
			HCCRFDNode.rnd.set(n.ID-1, Distribution.getRandom().nextDouble());
			((HCCRFDNode)n).CMs.clear();						//Clean the CMs of all the nodes, CM might become CH and vice versa
		}
		((HCCRFDNode)Tools.getNodeByID(1)).is_configured = true;	//only the sink is always "configured"
		Eavg = 0;
		int c = 0;
		/*for ( Double b : HCCRFDNode.RE ){
				if ( b > 0 ){	//Only consider alive nodes
					Eavg += b;
					c++;
				}
			}*/
		for( int i=0; i<HCCRFDNode.RE.size(); i++ ){
			double b = HCCRFDNode.RE.get(i);
			if ( b > 0 ){	//Only consider alive nodes
				Eavg += b;
				c++;
			}else{
				Tools.getNodeByID(i+1).setColor(Color.BLACK);
			}

		}
		Eavg /= c;
		//			HCCRFDNode.round++;								//add one to the rounds
		float p = 0.1f;//0.25									//probability of being CH
//		float T = p/(1-p*(HCCRFDNode.n_rnd%(1/p)));		//threshold
		
		//ArrayList<Integer> chs = new ArrayList<>();		//selected CHs, 1-indexed
		//			for ( int i=1; i<rnd.size(); i++ ){
		/*
		addNeighbors(CHs, order);
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0L;
		
		while ( true ){
			//				System.out.println(CHs.size() + ": " + CHs);
			//				System.out.println(order.size() + ": " + order);
			for ( int k=0; k<order.size(); k++ ){
				int i = order.get(k);
				if ( ( HCCRFDNode.n_rnd - last_round_ch.get(i) >=1/p || isSinkNeighbor(i) ) && T>=rnd.get(i) && RE.get(i)>= Eavg*e_factor && !HCCRFDNode.CHs.contains(i+1)){
					//				if ( T<=rnd.get(i) && RE.get(i)>= Eavg*e_factor ){
					HCCRFDNode.CHs.add(i+1);								//node i is CH
					last_round_ch.set(i, HCCRFDNode.n_rnd) ;
					break;
				}
				if ( HCCRFDNode.CHs.size() >= p*rnd.size() )
					break;
			}

			if ( HCCRFDNode.CHs.size() >= p*rnd.size() )
				break;
			else{
				addNeighbors(CHs, order);
				e_factor -= 0.0001;
				for ( Node n : Tools.getNodeList() )
					HCCRFDNode.rnd.set(n.ID-1, Distribution.getRandom().nextDouble());
			}
			elapsedTime = (new Date()).getTime() - startTime;
			if ( elapsedTime >= 4000 )
				break;
		}*/
		
		
		int times = 0;
		float T;
		while (true){
			HCCRFDNode.CHs.clear();
			CHs.add(1);
			T = p/(1-p*(HCCRFDNode.n_rnd%(1/p)));
			for ( int i=0; i<RE.size(); i++ ){
//				int i = order.get(k);
				if ( ( HCCRFDNode.n_rnd - last_round_ch.get(i) >=1/p-2 || isSinkNeighbor(i) ) && 
						T>=rnd.get(i) && RE.get(i)>= Eavg*e_factor && 
						!HCCRFDNode.CHs.contains(i+1) && 
						HCCRFDNode.DisconnectedNodes.get(i)==1 ){
					//				if ( T<=rnd.get(i) && RE.get(i)>= Eavg*e_factor ){
					HCCRFDNode.CHs.add(i+1);								//node i is CH
//					last_round_ch.set(i, HCCRFDNode.n_rnd);
//					break;
				}
//				if ( CHs.size() >= p*RE.size() && testCHConnectivity() )
//					break;
			}


//			for( int i=0; i<CHs.size(); i++ )
//				System.out.print(CHs.get(i)-1 +" ");
//			System.out.println();
//			if ( CHs.size() >= p*RE.size() && testCHConnectivity() ){
			if ( testCHConnectivity() ){
//				System.out.println("CHs (" + CHs.size() + "): " + CHs);
				break;
			}
				//if ( HCCRFDNode.CHs.size() >= p*rnd.size() )
				//	break;
			e_factor -= 0.0001;
			for ( Node n : Tools.getNodeList() )
				HCCRFDNode.rnd.set(n.ID-1, Distribution.getRandom().nextDouble());
			times++;
			if ( times >= 10 ){
				times = 0;
				p += 0.01;
			}

//			System.out.println("--------------------- " + p);
			if ( p > 1){
				debugMsg("p "+p,2);
				Tools.exit();
			}
			
		}

		int dn=0;
		for (int i=0; i<DisconnectedNodes.size(); i++)
			dn += DisconnectedNodes.get(i);
		System.out.print("T:" + T + " p:" + p + " ConnN:" + dn + " CHs (" + CHs.size() + "): "  );
		
		
		for( int i=0; i<CHs.size(); i++ ){
			System.out.print(CHs.get(i)-1 +" ");
			last_round_ch.set(CHs.get(i)-1, HCCRFDNode.n_rnd);
		}
		
		System.out.println();
		debugMsg("p "+p,2);
		//			if ( HCCRFDNode.CHs.size() < p*rnd.size() )
		//				continue;



		//			if ( testCHConnectivity() && HCCRFDNode.CHs.size() >=6 )
		//				break;
		//		}

		///////////////////////////////////////////
		//Deciding cluster membership
		ArrayList<Integer> cms = new ArrayList<>();
		ArrayList<Integer> cmch = new ArrayList<>();
		for ( Node n: Tools.getNodeList() ){
			if ( n.ID == 1 )
				continue;
			if ( HCCRFDNode.CHs.contains(n.ID) )
				continue;

			double dmin = 10000;
			int idx = -1;
			for (int i=0; i<HCCRFDNode.CHs.size(); i++){
				//				double dx = n.getPosition().xCoord - Tools.getNodeByID(HCCRFDNode.CHs.get(i)).getPosition().xCoord ;
				//				double dy = n.getPosition().yCoord - Tools.getNodeByID(HCCRFDNode.CHs.get(i)).getPosition().yCoord ;
				//				double d = Math.sqrt( Math.pow(dx, 2) + Math.pow(dy, 2) );
				double d = getDist(HCCRFDNode.CHs.get(i), n.ID);
				if ( dmin > d ){
					dmin = d;
					idx = HCCRFDNode.CHs.get(i);
				}
			}

			cms.add(n.ID);
			cmch.add(idx);
		}
		///////////////////////////////////////////

		MessageBroadcastCHID mb = new MessageBroadcastCHID(HCCRFDNode.CHs, cms, cmch);
		broadcast(mb);		//if this doesn't work, use a timer

		for ( Node n : Tools.getNodeList() ){
			// Since I am not actually exchanging packets for updating the JOIN_ACCEPT and the JOIN_REQUEST, then I just "simulate" the exchange by 
			// spending the battery accordingly, although each CM sends only one JOIN_REQUEST and receives only one JOIN_ACCEPT
			// and the CH receives many JOIN_REQUEST and sends many JOIN_ACCEPT, I will just approximate to see how it goes.
			// In addition, on the case on which the CM does not have direct communication with the CH, the CM sends more packets by relaying
			// ones that are not his own.
			// See what happens, I guess I still win over this, If I don't, I'll have to implement this thing because it consumes more energy 
			// then the current asumption
			((HCCRFDNode)n).getBattery().spend(EnergyMode.RECEIVE);
			((HCCRFDNode)n).getBattery().spend(EnergyMode.SEND);
			Overheads += 1;
		}

	}

	/**
	 * 
	 * @param i node to be tested as neighbor (0-indexed)
	 * @return
	 */
	private boolean isSinkNeighbor(int i) {
		for( int j=0; j<AdjList[0].size(); j++ ){
			int u = AdjList[0].get(j);
			if( i == u )
				return true;
		}
		return false;
	}

	/**
	 * BFS the graph by CH
	 */
	private boolean testCHConnectivity() {
		Queue q = new LinkedList<Integer>();
//		bfs();
		q.add(0);
		ArrayList<Boolean> visited = new ArrayList<Boolean>(Collections.nCopies(AdjList.length, false));
		visited.set(0, true);
		while(!q.isEmpty()){
			int u = (int)q.poll();
			for(int i=0; i<AdjList[u].size(); i++){
				int v = AdjList[u].get(i);
				if ( !CHs.contains(v+1) )
					continue;
				if (HCCRFDNode.DisconnectedNodes.get(v)==0)//if it's disconnected is ignored
					continue;
				if ( !visited.get(v) ) {
					visited.set(v, true);
					q.add(v);
				}
			}
		}
			
		for ( int i=0; i<visited.size(); i++ ){
			if ( !CHs.contains(i+1) )
				continue;
			if ( !visited.get(i) )
				return false;
		}
		return true;
	}

	public void NNCreateTable() {
		for ( Node node : Tools.getNodeList() ){
			HCCRFDNode u = (HCCRFDNode)node;
			u.getBattery().spend(EnergyMode.RECEIVE);
			u.getBattery().spend(EnergyMode.SEND);
			
			u.NN_Table.clear();
			
			for ( Edge e : u.outgoingConnections ){
				HCCRFDNode v = (HCCRFDNode) e.endNode ;
				if ( v == u )
					v = (HCCRFDNode) e.startNode;

				if ( HCCRFDNode.DisconnectedNodes.get(v.ID-1)==0 )	//Don't consider disconnected nodes
					continue;
				
				if ( u.my_ch == -1 ){	//I am CH, so my table is only of other CHs
					if ( v.my_ch != -1 )
						continue;
				}else{					//I am CM, so my table is only of other CMs in my cluster and my CH
					if ( v.my_ch!=-1 && v.my_ch != u.my_ch )	//neighbors in other cluster aren't considered
						continue;
				}
				
				float ds = getDist(this.ID, this.ID);
				float dn;
				if ( u.my_ch == -1 )
					dn = getDist(1, this.ID);
				else
					dn = getDist(u.my_ch, this.ID);
				NN_TableItem it = new NN_TableItem(v.ID, v.hop_count, v.getBattery().getEnergy(), ds, dn);
				u.NN_Table.add(it);
				
			}
		}
	}

	/**
	 * Initialization stage of RFDMRP, called from the sink
	 */
	public void initializeRFD() {
		if ( this.ID != 1 )
			return;
		for ( Node node : Tools.getNodeList() ){
			HCCRFDNode n = (HCCRFDNode)node;
			n.rfd_is_configured = false;
		}
		//Broadcasting the so called beacon message and the response with the respective locations and IDs
		HCCRFDInformHopCount mhc = new HCCRFDInformHopCount(this.ID, this.ID, this.hop_count+1);
		HCCRFDGenTimer t = new HCCRFDGenTimer(mhc);
		t.startRelative(0.0001, this);
		/*if ( this.ID == 1 ){
			for ( int i=0; i<CHs.size(); i++ ){
				HCCRFDNode n = (HCCRFDNode)Tools.getNodeByID(CHs.get(i));
				n.getBattery().spend(EnergyMode.RECEIVE);
				n.getBattery().spend(EnergyMode.SEND);
				
				//Now the sink computes the hop count with the distances and sends it to the nodes
				double dx = n.getPosition().xCoord - this.getPosition().xCoord;
				double dy = n.getPosition().yCoord - this.getPosition().yCoord;
				double d = Math.sqrt( Math.pow(dx, 2) + Math.pow(dy, 2));
				int hc = (int) (d/CommunicationRadius);
				
				//And informs the nodes with the inform_hopcount packet
				n.getBattery().spend(EnergyMode.RECEIVE);
				n.getBattery().spend(EnergyMode.SEND);
				n.hop_count = hc;		//FIXME
				
				
			}
		} else if ( this.my_ch == -1 ) {//this.myRole == Roles.CH ){
			for ( int i=0; i<CMs.size(); i++ ){
				HCCRFDNode n = (HCCRFDNode)Tools.getNodeByID(CHs.get(i));
				n.getBattery().spend(EnergyMode.RECEIVE);
				n.getBattery().spend(EnergyMode.SEND);
				
				//Now the ch computes the hop count with the distances and sends it to the nodes
				double d = getDist(n, this);
				int hc = (int) (d/CommunicationRadius);
				
				//And informs the nodes with the inform_hopcount packet
				n.getBattery().spend(EnergyMode.RECEIVE);
				n.getBattery().spend(EnergyMode.SEND);
				n.hop_count = hc;		//FIXME
				
				
			}
		}*/ /*else if ( this.myRole == Roles.CM ){
			
			for ( int i=0; i<((HCCRFDNode)Tools.getNodeByID(my_ch)).CMs.size(); i++ ){
				HCCRFDNode n = (HCCRFDNode)Tools.getNodeByID(CHs.get(i));
				n.getBattery().spend(EnergyMode.RECEIVE);
				n.getBattery().spend(EnergyMode.SEND);
				
				//Now the ch computes the hop count with the distances and sends it to the nodes
				double dx = n.getPosition().xCoord - this.getPosition().xCoord;
				double dy = n.getPosition().yCoord - this.getPosition().yCoord;
				double d = Math.sqrt( Math.pow(dx, 2) + Math.pow(dy, 2));
				int hc = (int) (d/CommunicationRadius);
				
				//And informs the nodes with the inform_hopcount packet
				n.getBattery().spend(EnergyMode.RECEIVE);
				n.getBattery().spend(EnergyMode.SEND);
				n.hop_count = hc;		//FIXME
				
				
			}
		}*/
		
	}
	
	/**
	 * 
	 * @param nodes in 1-indexed
	 * @param neighbors out 0-indexed
	 */
	public void addNeighbors(ArrayList<Integer> nodes, ArrayList<Integer> neighbors){
		neighbors.clear();
		ArrayList<Boolean> visited = new ArrayList<Boolean>(Collections.nCopies(AdjList.length, false));
		for(int i=0; i<nodes.size(); i++){
			int u = nodes.get(i)-1;
			if ( !visited.get(u) )
				visited.set(u, true);
			
			for ( int j=0; j<AdjList[u].size(); j++ ){
				if (HCCRFDNode.DisconnectedNodes.get(j)==0 )	//Don't consider disconnected nodes
					continue;
				int v = AdjList[u].get(j);
				visited.set(v, true);
				neighbors.add(v);
			}
		}
		Collections.reverse(neighbors);
	}

	/**
	 * computes the maximum distance between the sink and a node
	 * @return
	 */
	public double RFDget_max_dist(int type){
		double max=0;
		Node bs = null;
		ArrayList<Integer> nodelist = new ArrayList<>();
		if ( type == 1 ){
			bs = Tools.getNodeByID(this.ID);
			nodelist.addAll(this.CMs);
		}
		else if ( type == 2 ){//FIXME: what about when the CHs cannot communicate each other inside 
			bs = Tools.getNodeByID(1);
			nodelist.addAll(HCCRFDNode.CHs);
		}
		
		for ( Integer id : nodelist ){
			Node n = Tools.getNodeByID(id);
			if ( n.ID == 1)
				continue;
			double dx = n.getPosition().xCoord - bs.getPosition().xCoord;
			double dy = n.getPosition().yCoord - bs.getPosition().yCoord;
			double d = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
			if ( max < d )
				max = d;
		}
		return max;
	}
	
	/**
	 * Returns the number of regions on which the sink divides the network.
	 * @return
	 */
	public int RFDn_regions(int type){
		return (int) (RFDget_max_dist(type)/(CommunicationRadius/2));
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
		HCCRFDDataMessage mdata = (HCCRFDDataMessage) msg;

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
				
				have_retransmitted = true;

				//this.aggregatePCKT = this.aggregatePCKT + mdata.getAggPacket();
				this.rota = true;
				if( (this.filhos.size()>1)){
					this.setColor(Color.blue);
				}else{
					this.setColor(Color.yellow);
				}

				//if ( ( (this.filhos.size()<2) && (this.myRole == Roles.RELAY)) ){
				if ( ( (this.filhos.size()<2) ) ){	//just retransmit
					this.battery.spend(EnergyMode.SEND);
					if (this.battery.getEnergy()<this.battery.getMinEnergy() && !HCCRFDNode.terminals.contains(this.ID-1))
						debugMsg("***** this should happen few very few packets for node "+ this.ID, 2);
					
					//mdata.setDest(this.NextHopSink);
					int dest = 0;
					if ( this.my_ch == -1 )	//I am CH
						dest = FNS(1);				//<====================== using RFD
					else					//I am CM
						dest = FNS(this.my_ch);		//<====================== using RFD
					mdata.setDest(dest);
					mdata.setSender(this.ID);
					mdata.setHopToSink(this.hop_count);
					mdata.accumDTime();
					//mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getEnergy()));
					mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getTotalSpentEnergy()));
					MessageSPTTimer msgTimer = new MessageSPTTimer(mdata,Tools.getNodeByID(dest));
					enviados = enviados + 1;
					msgTimer.startRelative(0.1,this);	//The message is sent almost inmediately

					//									sptLog.logln("Sdata t "+ (Global.currentTime+ time)
					//										+ " Ns "+ this.ID 
					//										+ " Ap " + aggregatePCKT 
					//										+ " Nd " + 1);

					DataPackets = DataPackets +1;
					Tools.appendToOutput("SendDataEvent(): Datapackets: "+this.DataPackets + "\n");
					//Spent energy due to the transmission mode
					
//					if (this.myRole == Roles.RELAY)
					if ( this.mystatus == Status.RELAY )
						this.setColor(Color.CYAN);
					//this.aggregatePCKT = 0;
				}//else if ((this.senddata) && (this.myRole != Roles.COLLABORATOR)){
				else if ((this.senddata)){	//agregate packet
					have_retransmitted = true;
					this.battery.spend(EnergyMode.SEND);
					if (this.battery.getEnergy()<this.battery.getMinEnergy() && !HCCRFDNode.terminals.contains(this.ID-1))
						debugMsg("***** this should happen few very few packets for node "+ this.ID, 2);
//					mdata.setDest(this.NextHopSink);
					int dest = 0;
					if ( this.my_ch == -1 )	//I am CH
						dest = FNS(1);				//<====================== using RFD
					else					//I am CM
						dest = FNS(this.my_ch);		//<====================== using RFD
					mdata.setDest(dest);				
					mdata.setSender(this.ID);
					mdata.setHopToSink(this.hop_count);
					mdata.accumDTime();
					//System.out.println(this.ID + " RX: "+mdata.getPayload());
					//mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getEnergy()));
					mdata.setPayload(new StringBuffer(mdata.getPayload()+";" + this.ID +","+this.battery.getTotalSpentEnergy()));
					//System.out.println(this.ID + " TX: "+mdata.getPayload());
					//mdata.setEventNum(this.eventnum);
					//mdata.setAggPacket(this.aggregatePCKT);
					nextsenddata = Global.currentTime + DataRate;

					MessageSPTTimer msgTimer = new MessageSPTTimer(mdata,Tools.getNodeByID(dest));
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
				Tools.appendToOutput("SendDataEvent(): HCCRFDDataMessage arrived to the SINK, Receivers: " +this.Recivers +"\n");
				Tools.appendToOutput("SendDataEvent(): Updated energies: " + mdata.getPayload()+"\n");
				mdata.debugMsg();
//				debugMsg("delivery time: "+ mdata.ge);
				
			}
		}
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
		Tools.appendToOutput( "HopToSink: " + this.hop_count + "\n" + "NextHopSink: " + FNS(1) + "\n" + "OwnerID:" + this.ownerID + "\n" );
		if ( myRole == Roles.CH)
			Tools.appendToOutput("I am " + myRole + "\n");
		else if ( myRole == Roles.CM )
			Tools.appendToOutput("I am " + myRole + " and my CH is " + my_ch + "\n");
		
//		if ( myRole == Roles.COLLABORATOR && this.my_ch == -1 )	//A CH that detected and event
		if ( this.my_ch == -1 )	
			Tools.appendToOutput("I am CH, so inter-cluster routing only is done");
//		else if ( myRole == Roles.COLLABORATOR && this.my_ch != -1 )	//A CM that detected and event
		else 
			Tools.appendToOutput("I am CM, so intra-cluster I reach my CH (" + this.my_ch + ") first.");
		/*else if ( myRole == Roles.SINK )
			for( Node n : Tools.getNodeList() )
				Tools.appendToOutput( );*/
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
//		myRole = Roles.COLLABORATOR;

		this.setColor( Color.cyan );
		// sptLog.logln("Detection t "+(Global.currentTime)
		// +" Ns "+ this.ID
		// +" Ev " +this.eventnum);
		Detects = Detects + 1;
		this.mystatus = Status.MONITORING;
		timerTREE = new HCCRFDTimer( this, TNO.MONITORING );
		timerTREE.tnoStartRelative( DataRate+10, this, TNO.MONITORING );
		
		/****************************************************/
		/* For updating the NN_Table */
		HCCRFDRequestReply rr = new HCCRFDRequestReply(this.ID, -1, this.hop_count, (float)this.getBattery().getEnergy(), (float)getPosition().xCoord, (float)getPosition().yCoord);
		HCCRFDGenTimer t = new HCCRFDGenTimer(rr);
		t.startRelative(0.01, this);
		/****************************************************/
	}

	// Node Sink start the Flooding of MCI
	public void sendMCI() {
		this.hop_count = 0; 	// I am Sink
		this.NextHopSink = this.ID; // I am SInk
		//MessageSPTTimer mcimsg = new MessageSPTTimer( new MCIMessage( this.HopToSink, this.ID, this.numberOfNodes ) );
		//MessageSPTTimer mcimsg = new MessageSPTTimer( new MCIMessage( this.HopToSink, this.ID, 1) );
		MessageSPTTimer mcimsg = new MessageSPTTimer( new HelloMessage( this.hop_count, this.ID) );
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

//		if ( ( this.myRole == Roles.COLLABORATOR ) && ( this.filhos.size() == 0 ) ) {
		if ( this.filhos.size() == 0 ) {
			this.setColor( Color.RED );
			
			/*if(!this.requestedRoute){
//				RequestRouteMessage req = new RequestRouteMessage(this.ID, this.ID);
				RequestRouteMessage req = new RequestRouteMessage(this.ID);
				req.add(this.ID);
				MessageSPTTimer routeTimer = new MessageSPTTimer(req, Tools.getNodeByID( this.NextHopSink ));
				routeTimer.startRelative(0.0001, this);
				Overheads += 1;
				this.battery.spend(EnergyMode.SEND);
				this.requestedRoute = true;
			}*/
			
			
			this.timerTREE.tnoStartRelative( DataRate, this, TNO.MONITORING );
			//Message mdata = new HCCRFDDataMessage( this.ID, this.NextHopSink, "Sink", this.HopToSink );
			//Message mdata = new HCCRFDDataMessage( this.ID, this.NextHopSink, new StringBuffer(this.ID + "," + this.battery.getEnergy()), this.HopToSink );
			//Message mdata = new HCCRFDDataMessage( this.ID, this.NextHopSink, new StringBuffer(this.ID + "," + this.battery.getTotalSpentEnergy()), this.hop_count , Global.currentTime);
			//MessageSPTTimer msgTimer = new MessageSPTTimer( mdata, Tools.getNodeByID( this.NextHopSink ) );
			int d = 0;
			if ( this.my_ch == -1 ){	//I am CH
				//startRFDMRP(this.ID, 1);
				d = FNS(1);
			}else{ 	//I am CM
				//startRFDMRP(this.ID, this.my_ch);
				d = FNS(this.my_ch);
			}
			
//			Message mdata = new HCCRFDDataMessage( this.ID, d, new StringBuffer(this.ID + "," + this.battery.getTotalSpentEnergy()), this.hop_count , 0.0d);
			Message mdata = new HCCRFDDataMessage( this.ID, d, new StringBuffer(this.ID + "," + this.battery.getTotalSpentEnergy()), this.hop_count , 0);
//			NN_Table.size();
			MessageSPTTimer msgTimer = new MessageSPTTimer( mdata, Tools.getNodeByID( d ) );
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
				if ( this.mystatus == Status.MONITORING || this.mystatus == Status.READY ){
					this.rota = true;
					sendData();
				}
//				if ( this.myRole == Roles.COLLABORATOR ) {
//					this.rota = true;
//					sendData();
//				}
//				if ( this.myRole == Roles.CH ){
//					myRole = Roles.COLLABORATOR;
//					this.rota = true;
//					sendData();
//				}
				break;

			case SCHEDULE_FEEDBACK:
				//scheduleFeedback();
				break;

		}
	}
	
	@NodePopupMethod( menuText = "Neighbors" )
	public void neighbors() {
		Tools.appendToOutput( "I have " + this.listAll.size() + " Neighbors :" + this.listAll + "\n" );
		Tools.appendToOutput("My hopToSink "+this.hop_count +"\n");
		for(Integer i: this.listAll){
			Tools.appendToOutput("ID "+i+ " hopToSink "+ ((HCCRFDNode)Tools.getNodeByID(i)).getHopToSink() +"\n");
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
			if (min>((HCCRFDNode)n).getBattery().getEnergy() && this.ID!=1 && !HCCRFDNode.terminals.contains(n.ID-1) && HCCRFDNode.UpNodes.get(n.ID-1)==1){	//Not counting terminals!
				min = ((HCCRFDNode)n).getBattery().getEnergy();
				node = n.ID;
			}
		}
		Tools.appendToOutput("Node "+node+" has the lower residual energy of "+min+"\n");
	}
	
	@NodePopupMethod(menuText="DeadDisconnectedNodes")
	public void listDeadAndDisconnected(){
		Tools.appendToOutput("Dead nodes: ");
		for(int i=0; i<HCCRFDNode.UpNodes.size(); i++)
			if ( HCCRFDNode.UpNodes.get(i) == 0 )	//Dead Node!
				Tools.appendToOutput((i+1) + " ");
		Tools.appendToOutput("\n");
		Tools.appendToOutput("Disconnected nodes: ");
		for(int i=0; i<HCCRFDNode.DisconnectedNodes.size(); i++)
			if ( HCCRFDNode.DisconnectedNodes.get(i) == 0) //Disconnected node!
				Tools.appendToOutput((i+1) + " ");
		Tools.appendToOutput("\n");
	}
	
	@NodePopupMethod(menuText="eventQueue")
	public void debugEventQueue() {
		System.out.println("-------------------------------------------");
//		System.out.println("EventQueue Size: " + Tools.getEventQueue().size() + " time " + Tools.getEventQueue().getNextEvent().time);
		for ( Event e : Tools.getEventQueue() ){
			if ( e.getEventNode().ID == this.ID )
				Tools.appendToOutput(" " + e.time);
		}
		Tools.appendToOutput("\n");
		
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
		return this.hop_count;
	}
	
	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

}
