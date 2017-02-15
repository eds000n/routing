
package projects.DDAARP.nodes.nodeImplementations;

import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.models.EnergyModel.simple.SimpleEnergy;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.UniformDistribution;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import projects.DDAARP.nodes.messages.DDAARP_AlertMessage;
import projects.DDAARP.nodes.messages.DDAARP_AnexaToSink;
import projects.DDAARP.nodes.messages.DDAARP_AnnouceCHMessage;
import projects.DDAARP.nodes.messages.DDAARP_CIM;
import projects.DDAARP.nodes.messages.DDAARP_CustoDaArvore;
import projects.DDAARP.nodes.messages.DDAARP_DataMessage;
import projects.DDAARP.nodes.messages.DDAARP_DestructRote;
import projects.DDAARP.nodes.messages.DDAARP_REM;
import projects.DDAARP.nodes.messages.DDAARP_REMBack;
import projects.DDAARP.nodes.messages.DDAARP_SinkFloodMessage;
import projects.DDAARP.nodes.timers.BorderCollectInformationTimer;
import projects.DDAARP.nodes.timers.DDAARPTimer;
import projects.DDAARP.nodes.timers.EndLeoTimer;
import projects.DDAARP.nodes.timers.EventLeoTimer;
import projects.DDAARP.nodes.timers.MessageLeoTimer;
import projects.DDAARP.nodes.timers.VerifyBorderNodeTimer;

//import projects.DDAARP.nodes.messages.*;
//import projects.DDAARP.nodes.timers.*;

public class DDAARPNode extends Node {

	public class EventKey {
		
		public int ID;

		public int eventID;
		
		public EventKey() {}
		
		public EventKey(int ID, int eventID) {
			this.ID = ID;
			this.eventID = eventID;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ID;
			result = prime * result + eventID;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EventKey other = (EventKey) obj;
			if (ID != other.ID)
				return false;
			if (eventID != other.eventID)
				return false;
			return true;
		}
		
		
	}

	
	public static class RoutingTableEntry{
		public double Energy;
		public int HopsToTree;
		public int HopsToSink;	
		public int NextHop;
		
		public RoutingTableEntry(double energy, int hopstotree, int hopstosink, int nexthop) {
			this.Energy = energy;
			this.HopsToTree = hopstotree;
			this.HopsToSink = hopstosink;
			this.NextHop = nexthop;
		}
	}

	private static int enviadosAgg = 0;
	private static int numpacket = 0;
	private static int recebidos =0;
	private static int packetrecvagg =0;
	private static int perdidos = 0;
	private static int perdidosAGG = 0;
	private static int enviados =0;
	private static int Event1 = 0;
	private static int Event2 = 0;
	private static int Event3 = 0;
	private static int Event4 = 0;
	private static int Event5 = 0;
	private static int Event6 = 0;
	public static int density = 0;
	
	private double erro = 0;
	public static int EventsAmount = 0;
	public static int EventsTimes = 0;
	public static int EventSize=0;
	public static int CommunicationRadius = 0;
	public static int DropRate = 0;
	public static int Recivers =0;
	public static int Detects =0;
	public static int Overheads =0;
	public static int DataPackets =0;
	public static int Edges =0;
	public static int DataRate =0;
	public static int Notifications =0;
	public static double SimulationTime =0;
	public static double ProbSend =1;
	
	private HashMap<EventKey, RoutingTableEntry> routingtable = new HashMap<EventKey, RoutingTableEntry>();
	private int HopToSink   = 100000;  			// Distance in Hops to Sink
	private int HopToEvent  = 100000; 			// Distance in Hops for to Event closer
	private int NextHopSink = 100000;
	private int HopToCoordinator = 100000;
	private int NextHop		= 100000; 			// My Relay of Data
	private int ownerID		= this.ID;			// My OwnerID
	private int ownerID_Hop	= 100000;			// Distance in Hops for my OwnerID
	private int ownerId_HopstoSink = 10000;
	private int ownerId_HoptoEvent = 10000;
	private int HopToEvent_Num = 100000;		// Hops to Event 
	private int Disttree = 0;
	private int Disttreerecv =0;
	/*private double Energy = 100;
	private double ConsumptionInTransmission  =0.09;
	private double ConsumptionInRecepition = 0.04;*/
	private SimpleEnergy battery;
	private boolean cluster_candidate = false;	// Candidate to coordinator
	private boolean rota = false;				// I am in routing of datas?
	private boolean senddata = false;			// Start Send data
	private int eventnum = 0;					//
	private int aggPackets=0;
	private int msgAggPckts = 0;
	private double nextsenddata=0;
	private boolean first = true;
	private boolean SendConfiguration = true;
	private boolean sentmci = false;
	private boolean sentchflood = false;
	private UniformDistribution uniformRandom =  new UniformDistribution(0,0.016);
	private UniformDistribution uniformRandomSend =  new UniformDistribution(0.0,1.0);
	public static Logging ddaarp = Logging.getLogger("ddaarpLog.txt", true);

	private enum Status {MONITORING, ANNOUNCING, CLUSTERING1, CLUSTERING2, READY};
	private enum Roles { SINK, COLLABORATOR, COORDINATOR, RELAY, AGGREGATOR };
	public enum TNO {TNO_TREE, TNO_VICINITY, TNO_CLUSTERING1, TNO_CLUSTERING2, TNO_MONITORING, TNO_EVENTS, TNO_RESPONSEROTA, TNO_ANEXATOSINK };
	private DDAARPTimer timerVICINITY, timerTIMEOUT;
	private Roles myrole;
	private Status mystatus;
	private int myeventID = -1;
	private ArrayList<Integer> filhos = new ArrayList<Integer>();
	private ArrayList<Integer> filhossend = new ArrayList<Integer>();
	private ArrayList<Integer> filhosrecv = new ArrayList<Integer>();
	
	private ArrayList<Integer> ReceivedNeighborHopBigger = new ArrayList<Integer>();
	private ArrayList<Integer> NeighborHopBigger = new ArrayList<Integer>();
	public static int[][] AdjMatrix ;
	private int isBorderNode = 0; //0: unknown, 1: is border node, 2: is NOT border node
	public ArrayList<Integer> node1 = new ArrayList<Integer>();
	public ArrayList<Integer> node2 = new ArrayList<Integer>();
	
	private LinkedList<Integer> listAll = new LinkedList<Integer>();	//A list containing the IDs of ALL the neighbor nodes
	
	private double eventEndTime;
	
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
	}
	
	@Override
	//## Incoming messages.
	public void handleMessages(Inbox inbox) {
		// TODO Auto-generated method stub
		int sender;
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			sender = inbox.getSender().ID;
			
			//##Nodes that is receiving the message to configuration of HopToSink and increment the HoptoSink of message and retransmit 
			if(msg instanceof DDAARP_SinkFloodMessage) {
				this.InitialConfiguration(msg);
			}
			
			//Collect and send adjacency matrix to the sink
			if(msg instanceof DDAARP_CIM){
				this.CollectInformation(msg);
			}
			
			//Request Route for data transmission
			if(msg instanceof DDAARP_REM){
				this.RequestRoute(msg);
			}
			//Build Path for data transmission
			if(msg instanceof DDAARP_REMBack){
				this.REMBuildRoute(msg);
			}
			//##Clustering and Election of Leader
			if (msg instanceof DDAARP_AlertMessage && ((DDAARP_AlertMessage)msg).getEventID()==this.eventnum){										
					this.ElectionLeader(msg);
			}
			
			//##Build the route of the new event to the existing structure
			if(msg instanceof DDAARP_AnexaToSink) {
				this.BuildRoute(msg, sender);
			}
				
			//##Node Coordination start the flooding of message MFE for network
			if(msg instanceof DDAARP_AnnouceCHMessage) {
				this.AnnouceNewEvent(msg);
			}
				
			//##Sent information about event to Sink
			if(msg instanceof DDAARP_DataMessage){
				this.SentInformationEvents(msg, sender);
			}
			
			//##Update Routes
		   if (msg instanceof DDAARP_DestructRote){
			   this.DestructRoute(msg,sender);
			}
		   
		   //##Calculate the cost of the routing structure.
			if (msg instanceof DDAARP_CustoDaArvore){
				this.CostRoutingStructure(msg);
			}
			
		}
	}
	
    private void REMBuildRoute(Message msg) {
		DDAARP_REMBack REMmsgback = (DDAARP_REMBack)msg;
		if(REMmsgback.path.size()>1){
			this.NextHop = REMmsgback.getSenderID();
			Tools.appendToOutput("remaining path: "+REMmsgback.path);
			REMmsgback.path.remove(0);
			
			MessageLeoTimer timer = new MessageLeoTimer(REMmsgback, Tools.getNodeByID(REMmsgback.path.get(0)));
			REMmsgback.setSenderID(this.ID);
			setColor(Color.PINK);
			timer.startRelative(0.001, this);
		}else{//This is the node that initially requested the route  
			sendData();
		}
		
	}

	private void RequestRoute(Message msg) {
    	DDAARP_REM REMmsg = (DDAARP_REM)msg;
    	
    	if(this.myrole!=Roles.SINK){
    		REMmsg.setSenderID(this.ID);
    		MessageLeoTimer timer = new MessageLeoTimer(REMmsg, Tools.getNodeByID(this.NextHop));
    		timer.startRelative(0.001, this);
    	}else{
    		//Run algorithm for route selection
    		ArrayList<Integer> route = new ArrayList<Integer>();
    		selectRoute(REMmsg.getCoordinatorID(), route);
    		route.remove(0);
    		//Send REM-Back
    		DDAARP_REMBack REMBack = new DDAARP_REMBack(this.ID);
    		REMBack.path.addAll(route);    		
    		MessageLeoTimer timer = new MessageLeoTimer(REMBack, Tools.getNodeByID(REMBack.path.get(0)));
    		timer.startRelative(0.001, this);
    	}
	}

    /**
     * Algorithm for selecting route. The routes that are selected are the ones that insert a smaller number
     * of steiner nodes. (A greedy approach, just a BFS)
     */
	private void selectRoute(int coordinatorID, ArrayList<Integer> route) {
		int min = 0;
		int source=0, target = coordinatorID-1;
		Queue<Integer> q = new LinkedList<Integer>();
		int n = AdjMatrix.length;
		int visited[] = new int[n+1];
		int dist[] = new int[n+1];
		int parent[] = new int[n+1];
		visited[0]=1;
		dist[0]=0;
		parent[0]=-1;
		q.add(source);
		
		while(!q.isEmpty()){
			int u = q.remove();
			int v = 0; //u
			while(v<n){
				if(AdjMatrix[u][v]==1 && visited[v]==0){
					
					visited[v]=1;
					parent[v] = u;
					
					dist[v]=dist[u]+1;
					if (v==target){
						q.clear();
						break;
					}
					
					q.add(v);
				}
				v++;
			}
		}	
		
		
		for(int i=0; i<n; i++)
			Tools.appendToOutput(dist[i]+" ");
		Tools.appendToOutput("\nDistances obtained: "+dist[target]+"\n");
		getRoute(parent, route, source, target);
		Tools.appendToOutput("Route obtained: "+route+"\n");
	}

	private void getRoute(int parent[], ArrayList<Integer> route, int s, int v){
		if(v==s)
			route.add(s+1);
		else if (parent[v]==-1)
			return;	//reached the sink
		else{
			getRoute(parent, route, s, parent[v]);
			route.add(v+1);
		}
			
	}
	
	private void CollectInformation(Message msg) {
    	DDAARP_CIM CIMmsg = (DDAARP_CIM)msg;
    	/*if (NeighborHopBigger.size()==0){//if it is border node then paint it orange and broadcast it
    		this.setColor(Color.ORANGE);
    		broadcast(CIMmsg);
    	}*/
    	if (this.isBorderNode==1)
    		return;
    	
    	/*if (this.ID==31 || this.ID==4 || this.ID==20 || this.ID==58 || this.ID==36){
    		Tools.appendToOutput("====\n");
    		Tools.appendToOutput("Node " + this.ID + " received CIMMessage from: "+ CIMmsg.getSenderID() + "\n");
			Tools.appendToOutput("NHB: " +this.NeighborHopBigger +"\n");
			Tools.appendToOutput("RNHB: "+this.ReceivedNeighborHopBigger+"\n");
			//Tools.appendToOutput("u: " + CIMmsg.node1 + "\n");
			//Tools.appendToOutput("v: " + CIMmsg.node2 + "\n");
			Tools.appendToOutput("====\n");
		}*/
    	
    	if(NeighborHopBigger.contains(CIMmsg.getSenderID())){
    		battery.spend(EnergyMode.RECEIVE);
    		if (!ReceivedNeighborHopBigger.contains(CIMmsg.getSenderID()))
    			ReceivedNeighborHopBigger.add((int)CIMmsg.getSenderID());
    		//ReceivedNeighborHopBigger.add(CIMmsg.getSenderID());
    		CIMmsg.addEdge(CIMmsg.getSenderID(), this.ID);
    		//node1.add(CIMmsg.getSenderID());
    		//node2.add(this.ID);
    		setColor(Color.MAGENTA);
    		
    		/*Tools.appendToOutput("====\n");
    		Tools.appendToOutput("u: " + CIMmsg.node1 + "\n");
			Tools.appendToOutput("v: " + CIMmsg.node2 + "\n");
			Tools.appendToOutput("====\n");*/
    		
    		//Verify if the node has received all the CIM messages from its neighbors with higher hop level
    		// If setA==setB, then A includes B and B includes A.
    		List<Integer> setA = new ArrayList<Integer>(NeighborHopBigger);
    		List<Integer> setB = new ArrayList<Integer>(ReceivedNeighborHopBigger);
    		setA.removeAll(ReceivedNeighborHopBigger);
    		setB.removeAll(NeighborHopBigger);
    		if (setA.size() == 0 && setB.size()==0){
    			if (this.myrole == Roles.SINK){
    				//The sink udpates the adjacency matrix
    				if (this.node1!=null && this.node2!=null){
    					CIMmsg.node1.addAll(this.node1);
    					CIMmsg.node2.addAll(this.node2);
    				}
    				updateAdjacencyMatrix(CIMmsg);
        		}
    			else{
    				CIMmsg.setSenderID(this.ID);
    				if (this.node1!=null && this.node2!=null){
    					CIMmsg.node1.addAll(this.node1);
    					CIMmsg.node2.addAll(this.node2);
    				}
    				setColor(Color.PINK);
    				
    				/*CIMmsg.node1.clear();
    				CIMmsg.node2.clear();
    				CIMmsg.node1.addAll(node1);
    				CIMmsg.node2.addAll(node2);*/
    				Overheads+=1;
    				MessageLeoTimer borderTimer = new MessageLeoTimer(CIMmsg);
    				borderTimer.startRelative(this.HopToSink+0.1,this);
    				Tools.appendToOutput("CollectInformation() BorderMessage Overheads: " + this.Overheads + "\n");
    				//sentBorderBack = true;
    				battery.spend(EnergyMode.SEND);
    			}
    		}else{
    			this.node1.addAll(CIMmsg.node1);
    			this.node2.addAll(CIMmsg.node2);
    		}
    	}
	}

	private void updateAdjacencyMatrix(DDAARP_CIM msg) {
		int n = Tools.getNodeList().size();
		if (AdjMatrix == null)
			AdjMatrix = new int[n][n];
		
		for(int i=0; i<msg.node1.size(); i++){
			Integer u = msg.node1.get(i)-1;	//the -1 is because the sink has ID=1		
			Integer v = msg.node2.get(i)-1;
			AdjMatrix[u][v] = 1;
			AdjMatrix[v][u] = 1;
		}
		
		Tools.appendToOutput("Updated adjacency matrix!\n");
		System.out.println("Updated adjacency matrix");
		for(int i=0; i<n; i++){
			System.out.print(i+": ");
			for(int j=0; j<n-1; j++)//Printing just inferior matrix
				if (AdjMatrix[i][j]!=0)
					System.out.print(j+" ");
					//System.out.print(AdjMatrix[i][j]+", ");
			//System.out.println(AdjMatrix[i][n-1]);
			System.out.println();
		}
	}

	@Override
	public void handleNAckMessages(NackBox nackBox) {
		while(nackBox.hasNext()) {
			Message msg = nackBox.next();
			if(msg instanceof DDAARP_DataMessage){
				if((this.myrole != Roles.COLLABORATOR) && (this.rota)){
					DDAARP_DataMessage mdata = (DDAARP_DataMessage)msg;
					mdata.setAggPacket(mdata.getAggPacket());
					MessageLeoTimer msgTimer = new MessageLeoTimer(mdata,Tools.getNodeByID(this.NextHop));
					msgTimer.startRelative(0.1,this);
//					infraLeoLog.logln("Sdata t "+ (Global.currentTime)
//						+ " Ns "+ this.ID 
//						+ " Ap " + aggPackets 
//						+ " Nd " + 1);
					
					DataPackets = DataPackets +1;
				}else{
					perdidos = perdidos+1;
				}

			}
		
		}
		
	}
    
	@Override
	public void init() {
		// TODO Auto-generated method stub
		myrole = Roles.RELAY;	
		//double endTime=0;
		
		try {
//			infraLeoLog = Logging.getLogger(
//					sinalgo.configuration.Configuration.getStringParameter("Log/LogFile"));
			SimulationTime = sinalgo.configuration.Configuration.getDoubleParameter("SimTime");
			ProbSend = sinalgo.configuration.Configuration.getDoubleParameter("ProbSend");
			EventsAmount = sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents");
			EventsTimes = sinalgo.configuration.Configuration.getIntegerParameter("Event/Time");
			density = sinalgo.configuration.Configuration.getIntegerParameter("Density");
			
			//daarpmswim = Logging.getLogger("daarpmswim"+ProbSend+"Log.txt", true);
			for (int i = 1; i <= EventsAmount; i++) {
				EventLeoTimer t = new EventLeoTimer(i);
				t.startEventAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventStart"+i), this, i);	
			}	
			eventEndTime = sinalgo.configuration.Configuration.getDoubleParameter("Event/EventEnd");
			DataRate = sinalgo.configuration.Configuration.getIntegerParameter("Event/DataRate");
			EventSize = sinalgo.configuration.Configuration.getIntegerParameter("Event/EventSize");
			CommunicationRadius = sinalgo.configuration.Configuration.getIntegerParameter("UDG/rMax");
			DropRate = sinalgo.configuration.Configuration.getIntegerParameter("TaxadePerda/dropRate");
			
			erro = sinalgo.configuration.Configuration.getDoubleParameter("TaxadePerda/dropRate");
			
		} catch (CorruptConfigurationEntryException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(0);
		}
		try {
			//Here, we have to get the battery implementation from Config.xml and inject into battery attribute
			String energyModel = Configuration.getStringParameter("Energy/EnergyModel");
			if (energyModel.contains("Simple")){
				battery = new SimpleEnergy(this.ID);
			}
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Energy Model not found");
			e.printStackTrace();
		}
		
		
		if (this.ID==1){
			this.setColor(Color.RED);
			this.myrole = Roles.SINK;
			this.mystatus = Status.READY;
			isBorderNode = 2;		//The sink is never border node
			sendMCI(); //Sink starts the flooding		
		}
		
		VerifyBorderNodeTimer borderTimer = new VerifyBorderNodeTimer(this);
		borderTimer.startAbsolute(1000,this);
		//Event timers
		EndLeoTimer etimer = new EndLeoTimer();
		etimer.startAbsolute(SimulationTime, this);
		
	}
	
	
	

	public boolean insideEvent(sinalgo.nodes.Position p, int eventID){
		double xc=0,yc=0, r=0;
		try{
			xc=sinalgo.configuration.Configuration.getDoubleParameter("Event/Xposition"+eventID);
			yc=sinalgo.configuration.Configuration.getDoubleParameter("Event/Yposition"+eventID);
			r = sinalgo.configuration.Configuration.getDoubleParameter("Event/EventSize");
		}catch (CorruptConfigurationEntryException e){
			e.printStackTrace();
			System.exit(0);
			
		}
		if (Math.pow((xc - p.xCoord),2) + Math.pow((yc -p.yCoord),2) < Math.pow(r/2,2)){
			this.eventnum = eventID;
			
			if (this.eventnum == 1)
				Event1 = Event1 + 1;
			if (this.eventnum == 2)
				Event2 = Event2 + 1;
			if (this.eventnum == 3)
				Event3 = Event3 + 1;
			if (this.eventnum == 4)
				Event4 = Event4 + 1;
			if (this.eventnum == 5)
				Event5 = Event5 + 1;
			if (this.eventnum == 6)
				Event6 = Event6 + 1;
			return true;
		}
		else 
			return false;
	}
	
	
	public void startDetection(){
		this.setColor(Color.BLUE);
		this.myeventID = this.ID;
		myrole = Roles.COLLABORATOR;
		this.cluster_candidate =true;
		Detects = Detects + 1;
		Tools.appendToOutput("startDetection(): node "+this.ID+" "+Detects+"\n");
		//timerVICINITY = new DDAARPTimer(this,TNO.TNO_VICINITY);
		//timeout(TNO.TNO_VICINITY);
		
		DDAARP_REM REMmsg = new DDAARP_REM(this.ID, this.ID);
		send(REMmsg, Tools.getNodeByID(this.NextHop));
	}
	
	//Node Sink start the Flooding of MCI
	public void sendMCI(){
		this.HopToSink = 0; 	// I am Sink
		this.NextHop =this.ID;  
		//Creates message to start the setup hop's nodes
		//MessageLeoTimer mcimsg = new MessageLeoTimer (new DDAARP_SinkFloodMessage(this.HopToSink,this.ID,this.Energy, "SINK"));
		MessageLeoTimer mcimsg = new MessageLeoTimer (new DDAARP_SinkFloodMessage(this.HopToSink,this.ID,this.battery.getEnergy(), "SINK"));
		//MessageLeoTimer mcimsg = new MessageLeoTimer (new DDAARP_SinkFloodMessage(this.HopToSink,this.ID,this.battery.getEnergy()));
		mcimsg.startRelative(0.001, this);
		//this.Energy = this.Energy - ConsumptionInTransmission;
	//	infraLeoLog.logln("Sink t " +(Global.currentTime + 0.00001)+ " Ns "+ this.ID);
		Overheads = Overheads + 1;
		
				        
	}


	/**
	 * After a time (1 second) every node verifies whether its a border node; in such a case 
	 * it sends the BorderMessage 
	 */
	public void SetNeighborHopBigger(){
		NeighborHopBigger.clear();
		for(Integer i: this.listAll){
			if ( this.HopToSink < ((DDAARPNode)Tools.getNodeByID(i)).getHopToSink())
				NeighborHopBigger.add(i);
		}
		if (NeighborHopBigger.size()==0){
			this.isBorderNode = 1;
			this.setColor(Color.ORANGE);
			BorderCollectInformationTimer borderTimer = new BorderCollectInformationTimer(new DDAARP_CIM(this.ID), this);
			borderTimer.startRelative(0.001, this);
			Overheads+=1;
		}
		else
			this.isBorderNode = 2;
	}
	
    public void SetNextHop(){
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
    }
    
    public void SetNextHopTree(int EventNum){
    	Iterator<EventKey> it = routingtable.keySet().iterator();
		EventKey candidato = it.next();
		EventKey Key;
		while(it.hasNext()){
			Key = it.next();
			if ((routingtable.get(candidato).HopsToTree > ( routingtable.get(Key).HopsToTree) && (Key.eventID != EventNum) )&& (this.ID != routingtable.get(Key).NextHop) ){
					candidato = Key;
				}else if( (routingtable.get(candidato).HopsToTree == routingtable.get(Key).HopsToTree) && ((Key.eventID != EventNum)) 
						&&((routingtable.get(candidato).Energy < (routingtable.get(Key).Energy)-5)) &&
						(this.ID != routingtable.get(Key).NextHop) ){
							candidato = Key;
				}	
			}
		
			this.NextHop =  candidato.ID;
			//this.HopToEvent = routingtable.get(candidato).HopsToTree+1;
			
		}
	
	public void announceCH(){
		Tools.appendToOutput("CH Flood "+ Integer.toString(this.ID)+"\n");
		this.HopToEvent = 0;
		
		//MessageLeoTimer annmfe = new MessageLeoTimer(new DDAARP_AnnouceCHMessage(this.HopToEvent, this.ID, this.eventnum,this.HopToSink,this.NextHop,this.Energy) );
		MessageLeoTimer annmfe = new MessageLeoTimer(new DDAARP_AnnouceCHMessage(this.HopToEvent, this.ID, this.eventnum,this.HopToSink,this.NextHop,this.battery.getEnergy()) );
		
		double time = uniformRandom.nextSample();
		annmfe.startRelative(8,this);
		//this.Energy = this.Energy - ConsumptionInTransmission;
		battery.spend(EnergyMode.SEND);
		
		Overheads = Overheads + 1;
	}
	
	public void aproximationtree(){
		MessageLeoTimer msgtree = new MessageLeoTimer (new DDAARP_CustoDaArvore(1,this.NextHop,this.ID));
	    double time = uniformRandom.nextSample();
		msgtree.startRelative(time, this);	
	}
	
	
	

	public void sendData(){
		if (Global.currentTime > eventEndTime+100){
			if(this.filhos.size() == 0){
				aproximationtree();
				this.setColor(Color.GREEN);
			}
			
			return;
		}
	
		mystatus = Status.MONITORING;
		int myAgg = aggPackets;
		int myMsgAgg = msgAggPckts;
		//clear counts
		aggPackets = 0;
		msgAggPckts = 0;
		
		double time = uniformRandom.nextSample();
		
		if((this.senddata) && (this.myrole == Roles.COORDINATOR)){

				if(this.eventnum ==1){
					SetNextHop();
				}else
					SetNextHopTree(this.eventnum);
			
				

				if (this.eventnum == 1)
					myMsgAgg = (int) Math.round(Event1 - (erro * Event1));
				if (this.eventnum == 2)
					myMsgAgg = (int) Math.round(Event2 - (erro * Event2));
				if (this.eventnum == 3)
					myMsgAgg = (int) Math.round(Event3 - (erro * Event3));
				if (this.eventnum == 4)
					myMsgAgg = (int) Math.round(Event4 - (erro * Event4));
				if (this.eventnum == 5)
					myMsgAgg = (int) Math.round(Event5 - (erro * Event5));
				if (this.eventnum == 6)
					myMsgAgg = (int) Math.round(Event6 - (erro * Event6));
				
					
				
			numpacket = numpacket +1;
			//Message mdata = new DDAARP_DataMessage(this.ID,this.NextHop,"COORDINATOR", this.HopToSink,myMsgAgg,this.Energy, this.eventnum);
			Message mdata = new DDAARP_DataMessage(this.ID,this.NextHop,"COORDINATOR", this.HopToSink,myMsgAgg,this.battery.getEnergy(), this.eventnum);
			
			MessageLeoTimer msgTimer = new MessageLeoTimer(mdata,Tools.getNodeByID(this.NextHop));
			nextsenddata = Global.currentTime + (DataRate);
			//Tools.appendToOutput("\n Nó: "+this.ID+" SendTo: "+this.NextHop);
			msgTimer.startRelative(DataRate,this);
			//this.Energy = this.Energy - ConsumptionInTransmission;
			battery.spend(EnergyMode.SEND);
			enviadosAgg = enviadosAgg + myMsgAgg;
			
			DataPackets = DataPackets +1;
			Notifications = Notifications + 1;
			this.rota = true;
			this.senddata = false;
			timerVICINITY.tnoStartRelative(DataRate, this, TNO.TNO_MONITORING);
		
		}else if((this.senddata) && (this.myrole == Roles.COLLABORATOR)){
			//Message mdata = new DDAARP_DataMessage(this.ID,this.NextHop,"COLLABORATOR",this.HopToSink,1,this.Energy, this.eventnum);
			Message mdata = new DDAARP_DataMessage(this.ID,this.NextHop,"COLLABORATOR",this.HopToSink,1,this.battery.getEnergy(), this.eventnum);
			
			nextsenddata = Global.currentTime + DataRate;
			MessageLeoTimer msgTimer = new MessageLeoTimer(mdata,Tools.getNodeByID(this.NextHop));
			msgTimer.startRelative(DataRate,this);
			//this.Energy = this.Energy - ConsumptionInTransmission;
			battery.spend(EnergyMode.SEND);
     		DataPackets = DataPackets +1;
     		Notifications = Notifications + 1;
			this.rota = true;
			this.senddata = false;
			timerVICINITY.tnoStartRelative(DataRate, this, TNO.TNO_MONITORING);
			enviados = enviados +1;
		}
		
	}
	

	public void DestructRoute(){
		this.rota = false;
		this.HopToEvent = 100000;
		Message DestructRoute = new DDAARP_DestructRote(this.NextHop, this.eventnum);
		MessageLeoTimer anexatosinkTimer = new MessageLeoTimer(DestructRoute);
		anexatosinkTimer.startRelative(0.000001,this);
		//this.Energy = this.Energy - ConsumptionInTransmission;
		battery.spend(EnergyMode.SEND);		
	}
	
	public void anexasink(){
		if(this.eventnum >1){
			this.SetNextHopTree(this.eventnum);
		}else this.SetNextHop();
		this.rota = true;
		Message anexatosink = new DDAARP_AnexaToSink(this.NextHop,this.eventnum,this.ID);
		MessageLeoTimer anexatosinkTimer = new MessageLeoTimer(anexatosink);
		anexatosinkTimer.startRelative(0.00001,this);
		//this.Energy = this.Energy - ConsumptionInTransmission;
		battery.spend(EnergyMode.SEND);
	}
	
	
	//****************************************************************************************************//
	public void InitialConfiguration(Message msg){
		DDAARP_SinkFloodMessage mcimsg = (DDAARP_SinkFloodMessage) msg;
		battery.spend(EnergyMode.RECEIVE);
		
		/*if (this.checkConnectivity){
			if (!isGraphConnected())
				sinalgo.tools.Tools.exit();
			this.checkConnectivity=false;
		}*/
		
		if ( !listAll.contains( mcimsg.SenderID ) ) {
			listAll.add( mcimsg.SenderID );
		}
		
		if ( this.HopToSink > (mcimsg.HopToSink+1) ) {
			this.setColor( Color.GREEN );			
			
			this.NextHopSink = mcimsg.SenderID;
			this.HopToSink = mcimsg.HopToSink+1;
			
			DDAARP_SinkFloodMessage sfm = new DDAARP_SinkFloodMessage(this.HopToSink, this.ID, this.battery.getEnergy(),"SINK");
			MessageLeoTimer sfmTimer = new MessageLeoTimer(sfm);
			sfmTimer.startRelative(this.HopToSink+0.1,this);
			battery.spend(EnergyMode.SEND);
			Overheads = Overheads + 1;
			this.sentmci = true;
			Tools.appendToOutput("InitialConfiguration() DDAARP_SinkFloodMessage Overheads: " + this.Overheads + "\n");
		}
		/*if(mcimsg.HopToSink < this.HopToSink){

			if (!(routingtable.containsKey(new EventKey(mcimsg.SenderID,1) ) )){
				routingtable.put(new EventKey(mcimsg.SenderID,1), new RoutingTableEntry(mcimsg.Energy,this.HopToEvent,mcimsg.HopToSink,mcimsg.SenderID));
			}else if (routingtable.get(new EventKey(mcimsg.SenderID,1)).HopsToSink >  mcimsg.HopToSink ){
				routingtable.put(new EventKey(mcimsg.SenderID,1), new RoutingTableEntry(mcimsg.Energy,this.HopToEvent,mcimsg.HopToSink,routingtable.get(mcimsg.SenderID).NextHop));
			}
			//Setar o nexthop
			SetNextHop();

			//if(!this.sentmci){
				//MessageLeoTimer mciTimer = new MessageLeoTimer((new DDAARP_SinkFloodMessage(this.HopToSink,this.ID, this.Energy, "RELAY")));
				MessageLeoTimer mciTimer = new MessageLeoTimer((new DDAARP_SinkFloodMessage(this.HopToSink,this.ID, this.battery.getEnergy(), "RELAY")));
				//double time = new UniformDistribution(1,1.2).nextSample();
				mciTimer.startRelative(this.HopToSink+1,this);
				//this.Energy = this.Energy - ConsumptionInTransmission;
				battery.spend(EnergyMode.SEND);
				this.sentmci = true;
				Overheads = Overheads + 1;
				this.setColor(Color.GRAY);
			//}
		}*/
		
	}
	
	/**
	 * If the node has no neighbors with higher hop level, then this is a border node and it broadcasts a CIM message
	 */
	public void StartCollectingInformation(){
		if (NeighborHopBigger.size()==0){
			isBorderNode = 1;
			this.setColor(Color.ORANGE);
			MessageLeoTimer cimTimer = new MessageLeoTimer(new DDAARP_CIM(this.ID));
			cimTimer.startRelative(0.001, this);
			Overheads+=1;
		}else
			isBorderNode = 2;
	}
	
	//****************************************************************************************************//
	//ALTERAR O ALGORITMO DE ELEIÇÃO DE LIDER PARA QUE O CRITÉRIO DE DESEMPATE AO INVÉZ DE SER O ID, SERÁ
	//A QUANTIDADE DE VIZINHOS POSSÍVEIS A SEREM O NEXTHOP DO COORDENADOR ELEITO
	//FAZER ISSO VAI MELHOR A QUANTIDADE DE ROTAS DISPONÍVEIS PARA O BALANCEAMENTO DE ENERGIA
	//******************************************************************************************************//
	public void ElectionLeader(Message msg){
		if((myrole == Roles.COORDINATOR) ){
			if(this.eventnum == 1){
				if(this.HopToSink > ((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink() ){
					myrole = Roles.COLLABORATOR;
					this.ownerID = ((DDAARP_AlertMessage)msg).getCandidateId();
					this.ownerId_HopstoSink = ((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink();
					this.NextHop = ((DDAARP_AlertMessage)msg).getSenderID();
					this.HopToCoordinator = ((DDAARP_AlertMessage)msg).getHopstoCoordinator();
					this.cluster_candidate = false;
					this.setColor(Color.GRAY);
					//this.Energy = this.Energy -ConsumptionInRecepition;
					battery.spend(EnergyMode.RECEIVE);
					MessageLeoTimer annmsg = new MessageLeoTimer(new DDAARP_AlertMessage(this.ID, this.HopToSink,this.eventnum,this.HopToEvent,this.HopToCoordinator+1, this.ID, ((DDAARP_AlertMessage)msg).getCandidateId(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()));
					double time = new UniformDistribution(0,0.5).nextSample();
					annmsg.startRelative(time, this);
					//this.Energy = this.Energy - ConsumptionInTransmission;
					battery.spend(EnergyMode.SEND);
					Overheads = Overheads + 1;
					
				}else if( (this.HopToSink == ((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()) && (this.ID> ((DDAARP_AlertMessage)msg).getCandidateId()) ){
					
					myrole = Roles.COLLABORATOR;
					this.ownerID = ((DDAARP_AlertMessage)msg).getCandidateId();
					this.ownerId_HopstoSink = ((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink();
					this.NextHop = ((DDAARP_AlertMessage)msg).getSenderID();
					this.HopToCoordinator = ((DDAARP_AlertMessage)msg).getHopstoCoordinator();
					this.cluster_candidate = false;
					this.setColor(Color.GRAY);
					//this.Energy = this.Energy -ConsumptionInRecepition; 
					battery.spend(EnergyMode.RECEIVE);
					MessageLeoTimer annmsg = new MessageLeoTimer(new DDAARP_AlertMessage(this.ID, this.HopToSink,this.eventnum,this.HopToEvent,this.HopToCoordinator+1, this.ID,((DDAARP_AlertMessage)msg).getCandidateId(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()));
					double time = new UniformDistribution(0,0.5).nextSample();
					annmsg.startRelative(time, this);
					//this.Energy = this.Energy - ConsumptionInTransmission;
					battery.spend(EnergyMode.SEND);
					Overheads = Overheads + 1;
				}
					
			}else 
			if(this.eventnum > 1){
				if(this.HopToEvent > ((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent()){
					myrole = Roles.COLLABORATOR;
					this.ownerID = ((DDAARP_AlertMessage)msg).getCandidateId();
					this.ownerId_HoptoEvent = ((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent();
					this.NextHop = ((DDAARP_AlertMessage)msg).getSenderID();
					this.HopToCoordinator = ((DDAARP_AlertMessage)msg).getHopstoCoordinator();	
					this.cluster_candidate = false;
					this.setColor(Color.GRAY);
					//this.Energy = this.Energy - ConsumptionInRecepition;
					battery.spend(EnergyMode.RECEIVE);
					MessageLeoTimer annmsg = new MessageLeoTimer(new DDAARP_AlertMessage(this.ID, this.HopToSink,this.eventnum,this.HopToEvent,this.HopToCoordinator+1, this.ID,((DDAARP_AlertMessage)msg).getCandidateId(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()));
					double time = new UniformDistribution(0,0.5).nextSample();
					annmsg.startRelative(time, this);
					//this.Energy = this.Energy - ConsumptionInTransmission;
					battery.spend(EnergyMode.SEND);
				
					Overheads = Overheads + 1;
					
				}else if( (this.HopToEvent == ((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent()) && (this.ID> ((DDAARP_AlertMessage)msg).getCandidateId()) ){
					myrole = Roles.COLLABORATOR;
					this.ownerID = ((DDAARP_AlertMessage)msg).getCandidateId();
					this.ownerId_HoptoEvent = ((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent();
					this.NextHop = ((DDAARP_AlertMessage)msg).getSenderID();
					this.HopToCoordinator = ((DDAARP_AlertMessage)msg).getHopstoCoordinator();
					this.cluster_candidate = false;
					this.setColor(Color.GRAY);
					//this.Energy = this.Energy - ConsumptionInRecepition;
					battery.spend(EnergyMode.RECEIVE);
					MessageLeoTimer annmsg = new MessageLeoTimer(new DDAARP_AlertMessage(this.ID, this.HopToSink,this.eventnum,this.HopToEvent,this.HopToCoordinator+1, this.ID,((DDAARP_AlertMessage)msg).getCandidateId(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()));
					double time = new UniformDistribution(0,0.5).nextSample();
					annmsg.startRelative(time, this);
					//this.Energy = this.Energy - ConsumptionInTransmission;
					battery.spend(EnergyMode.SEND);
					Overheads = Overheads + 1;
				}
					
			}
		}else if (this.eventnum == 1){
			if ( (this.myrole == Roles.COLLABORATOR) && ( (this.ownerId_HopstoSink > ((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()) ||
					((this.ownerId_HopstoSink == ((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()) && (this.ownerID > ((DDAARP_AlertMessage)msg).getCandidateId())))){
				
				this.ownerID = ((DDAARP_AlertMessage)msg).getCandidateId();
				this.ownerId_HopstoSink = ((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink();
				
				this.NextHop = ((DDAARP_AlertMessage)msg).getSenderID();
				this.HopToCoordinator = ((DDAARP_AlertMessage)msg).getHopstoCoordinator();
				this.setColor(Color.GRAY);
				//this.Energy = this.Energy - ConsumptionInRecepition;
				battery.spend(EnergyMode.RECEIVE);
				MessageLeoTimer annmsg = new MessageLeoTimer(new DDAARP_AlertMessage(this.ID, this.HopToSink,this.eventnum,this.HopToEvent,this.HopToCoordinator+1, this.ID,((DDAARP_AlertMessage)msg).getCandidateId(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()));
				double time = new UniformDistribution(0,0.5).nextSample();
				annmsg.startRelative(time, this);
				//this.Energy = this.Energy - ConsumptionInTransmission;
				battery.spend(EnergyMode.SEND);
				Overheads = Overheads + 1;

			}
		}
		else if ( (this.myrole == Roles.COLLABORATOR) && ( (this.ownerId_HoptoEvent > ((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent()) ||
				((this.ownerId_HoptoEvent == ((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent()) && (this.ownerID > ((DDAARP_AlertMessage)msg).getCandidateId())))){
			
			this.ownerID = ((DDAARP_AlertMessage)msg).getCandidateId();
			this.ownerId_HoptoEvent = ((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent();
			
			this.NextHop = ((DDAARP_AlertMessage)msg).getSenderID();
			this.HopToCoordinator = ((DDAARP_AlertMessage)msg).getHopstoCoordinator();	
			//this.Energy = this.Energy - ConsumptionInRecepition;
			battery.spend(EnergyMode.RECEIVE);
			MessageLeoTimer annmsg = new MessageLeoTimer(new DDAARP_AlertMessage(this.ID, this.HopToSink,this.eventnum,this.HopToEvent,this.HopToCoordinator+1, this.ID,((DDAARP_AlertMessage)msg).getCandidateId(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoEvent(),((DDAARP_AlertMessage)msg).getOwnerId_HopstoSink()));
			double time = new UniformDistribution(0,0.5).nextSample();
			annmsg.startRelative(time, this);
			//this.Energy = this.Energy - ConsumptionInTransmission;
			battery.spend(EnergyMode.SEND);
			
			Overheads = Overheads + 1;

		}

	}
	
	public void CostRoutingStructure(Message msg){
		DDAARP_CustoDaArvore dAARPMSWIM_CustoDaArvore = (DDAARP_CustoDaArvore) msg;
		if(this.ID == dAARPMSWIM_CustoDaArvore.getNexthop()){
			this.filhosrecv.add(dAARPMSWIM_CustoDaArvore.getSentnode());
					
			if(this.myrole != Roles.SINK)
			{
				this.setColor(Color.yellow);
				this.Disttree = this.Disttree + dAARPMSWIM_CustoDaArvore.getDisttree();
				this.Disttreerecv = this.Disttreerecv +1;
		
				if(this.filhos.size() == 1){
					this.filhossend.add(this.NextHop);
					broadcast(new DDAARP_CustoDaArvore(this.Disttree+1, this.NextHop,this.ID));
					
				}
				else if(this.filhos.size()==this.Disttreerecv){
					broadcast(new DDAARP_CustoDaArvore(this.Disttree+1, this.NextHop,this.ID));
					this.filhossend.add(this.NextHop);
				}
			
			}else{
				this.Disttree = this.Disttree + dAARPMSWIM_CustoDaArvore.getDisttree();
				this.Disttreerecv = this.Disttreerecv +1;
				Tools.appendToOutput("filhos: "+this.filhos.size()+"  rec: "+ this.Disttreerecv);
				this.setColor(Color.black);	
				if(this.filhos.size() == 1){
//					infraLeoLog.logln("Arestas "+ this.Disttree
//					);
					Edges = this.Disttree;
				}else if(this.filhos.size()==this.Disttreerecv){
						//infraLeoLog.logln("N: "+this.Disttreerecv +" Arestas "+ this.Disttree);
						Edges = this.Disttree;
				}
				
			}
		}	
	}
	
	public void AnnouceNewEvent (Message msg){
		DDAARP_AnnouceCHMessage annmfe = (DDAARP_AnnouceCHMessage)msg;
		
		if(this.HopToEvent > annmfe.getHopToEvent() ){
			
			if (!(routingtable.containsKey(new EventKey(annmfe.getSenderID(), annmfe.getEventnum())))){
				routingtable.put( new EventKey (annmfe.getSenderID(),annmfe.getEventnum()), new RoutingTableEntry(annmfe.getEnergy(),annmfe.getHopToEvent(),annmfe.getHopToSink(),annmfe.getNextHopSender()));
			}else if (routingtable.get( new EventKey (annmfe.getSenderID(),annmfe.getEventnum())).HopsToTree >  annmfe.getHopToEvent()){
				routingtable.put(new EventKey (annmfe.getSenderID(),annmfe.getEventnum()), new RoutingTableEntry(annmfe.getEnergy(),annmfe.getHopToEvent(),annmfe.getHopToSink(),annmfe.getNextHopSender()));
			}
	
			if((this.myrole != Roles.SINK) && (this.myrole != Roles.COLLABORATOR) && (this.myrole != Roles.COORDINATOR) && (!this.rota)){
				if(this.eventnum != annmfe.getEventnum()){
					this.sentchflood = false;
				}	
				this.HopToEvent = annmfe.getHopToEvent()+1;
				this.HopToEvent_Num = annmfe.getEventnum();
				this.eventnum = annmfe.getEventnum();
				//this.Energy = this.Energy - ConsumptionInRecepition;
				battery.spend(EnergyMode.RECEIVE);
				double Rand = uniformRandomSend.nextSample();
				Tools.appendToOutput("Rand: "+Rand+" <= Prob: "+ ProbSend+"\n");
				if ( Rand <= ProbSend){
					SendConfiguration = true;	
				}else{
				SendConfiguration = false;
				}
				 
				if ((!this.sentchflood) && (SendConfiguration)){
					Tools.appendToOutput("\n...Enviou  \n");
					//MessageLeoTimer annchTimer = new MessageLeoTimer(new DDAARP_AnnouceCHMessage(this.HopToEvent, this.ID,this.eventnum,this.HopToSink,this.NextHop,this.Energy));
					MessageLeoTimer annchTimer = new MessageLeoTimer(new DDAARP_AnnouceCHMessage(this.HopToEvent, this.ID,this.eventnum,this.HopToSink,this.NextHop,this.battery.getEnergy()));
					annchTimer.startRelative(((this.HopToEvent+1)/2),this);
					//this.Energy = this.Energy - ConsumptionInTransmission;
					battery.spend(EnergyMode.SEND);
//					infraLeoLog.logln("Flooding t " + (Global.currentTime )
//						+ " Ns "+ this.ID 
//						+ " Pi " + msg.hashCode() 
//						+ " Pt " +"wsn-infra-ch");
					
					Overheads = Overheads + 1;
					
					this.sentchflood = true;
					
				}else{
					Tools.appendToOutput("Não Enviou  \n");
					this.sentchflood = true;
				}
			}
		}	
	}
	
	public void BuildRoute(Message msg, int sender){
		DDAARP_AnexaToSink msganexasink = (DDAARP_AnexaToSink) msg;
		if(this.ID == msganexasink.nexthop){
			if((this.myrole != Roles.SINK) && (this.myrole != Roles.COLLABORATOR) && (this.myrole != Roles.COORDINATOR)){
				this.myrole = Roles.RELAY;
				this.HopToEvent = 0;
				//this.Energy = this.Energy - ConsumptionInRecepition;
				battery.spend(EnergyMode.RECEIVE);
				if (!this.filhos.contains((Object)sender)){
					this.filhos.add(sender);
				}
				
				if(!this.rota){
					this.eventnum = msganexasink.EventNum;
					this.rota = true;
					if((this.eventnum ==1)){
						SetNextHop();
					}else {
						SetNextHopTree(msganexasink.EventNum);
					}
					Overheads = Overheads +1;
				}else{
					return;
				}

				this.setColor(Color.yellow);
				Message anexa = new DDAARP_AnexaToSink(this.NextHop,this.eventnum,this.ID);
				MessageLeoTimer anexaTimer = new MessageLeoTimer(anexa);
				anexaTimer.startRelative(0.000001,this);
				//Tools.appendToOutput("\n Nó: "+this.ID+" sendAnex : "+this.NextHop);
    			//this.Energy = this.Energy - ConsumptionInTransmission;
				battery.spend(EnergyMode.SEND);
				announceCH();
			}
		}
	}

	
	public void SentInformationEvents(Message msg, int sender){
		DDAARP_DataMessage mdata = (DDAARP_DataMessage)msg;
		
		Tools.appendToOutput("\n Nó: "+this.ID+" Received: "+mdata.getSender());
		if(this.ID == mdata.getDest()){
		
			if (!this.filhos.contains((Object)sender)){
				this.filhos.add(sender);
			}
			
			//this.Energy = this.Energy - ConsumptionInRecepition;
			battery.spend(EnergyMode.RECEIVE);
					
			if ((this.myrole != Roles.SINK) )
				if ( (  (this.myrole == Roles.RELAY) && (mdata.getPayload() != "COLLABORATOR"))){
		

					//Tools.appendToOutput("\n Nó: "+this.ID+" Received: "+mdata.getSender());
				
					double time = uniformRandom.nextSample();
				
					if(Global.currentTime > this.nextsenddata)
						this.senddata =true;
				
					
					this.aggPackets = this.aggPackets + mdata.getAggPacket();
					this.rota = true;
					this.setColor(Color.GREEN);
					
					if(this.myrole != Roles.COLLABORATOR){
						if((this.eventnum ==1) && (mdata.getEventNum() == 1)){
							SetNextHop();
						}else
							SetNextHopTree(mdata.getEventNum());
					}
					
					if ( ((this.filhos.size()<2) && (this.myrole == Roles.RELAY)) ){
						this.HopToEvent = 0;
						mdata.setDest(this.NextHop);
						mdata.setSender(this.ID);
						mdata.setHopToSink(this.HopToSink);
						mdata.setEnergy(this.battery.getEnergy());
						mdata.setHopstoTree(this.HopToEvent);
						MessageLeoTimer msgTimer = new MessageLeoTimer(mdata,Tools.getNodeByID(this.NextHop));
						Tools.appendToOutput("\n Nó: "+this.ID+" SendTo: "+this.NextHop+ " Ag: "+mdata.getAggPacket());
						msgTimer.startRelative(0.00001,this);
						//this.Energy = this.Energy - ConsumptionInTransmission;
						battery.spend(EnergyMode.SEND);
												
						DataPackets = DataPackets + 1;
					if (this.myrole == Roles.RELAY){
						this.setColor(Color.GREEN);
						
					}								
				}else if( (this.senddata) && (this.myrole == Roles.RELAY)){
					this.HopToEvent = 0;	
					mdata.setDest(this.NextHop);
					mdata.setSender(this.ID);
					mdata.setHopToSink(this.HopToSink);
					mdata.setEventNum(this.eventnum);
					mdata.setHopstoTree(this.HopToEvent);
					mdata.setEnergy(this.battery.getEnergy());
					mdata.setAggPacket(this.aggPackets);
					nextsenddata = Global.currentTime + DataRate;
					//this.Energy = this.Energy - ConsumptionInTransmission;
					battery.spend(EnergyMode.SEND);
					MessageLeoTimer msgTimer = new MessageLeoTimer(mdata,Tools.getNodeByID(this.NextHop));
			    	msgTimer.startRelative(0.0000001,this);
			    	Tools.appendToOutput("\n Nó: "+this.ID+" SendTo: "+this.NextHop+ " Ag: "+mdata.getAggPacket());						
//			    		infraLeoLog.logln("Sdata t "+ (Global.currentTime+ time)
//							+ " Ns "+ this.ID 
//							+ " Ap " + aggPackets 
//							+ " Nd " + 1);
			    		DataPackets = DataPackets +1;
					this.rota = true;
					this.senddata = false;
					this.aggPackets = 0;
					//if(this.myrole == Roles.RELAY)
					this.setColor(Color.black);
						
					}
				}
			

			if(this.myrole == Roles.SINK){
				    Recivers = Recivers + 1;//Recebiçoes e não qtd de dados recebidos
				    packetrecvagg = packetrecvagg + mdata.getAggPacket();
				//}
			}
		}else if ( (this.ID != mdata.getDest()) && (this.filhos.contains((Object)sender) && (this.rota))){
			this.filhos.remove((Object)sender);
			if(this.myrole !=Roles.COLLABORATOR && this.myrole != Roles.COORDINATOR)
			   this.setColor(Color.magenta);
			       DestructRoute();
		}
	}
		
	public void DestructRoute(Message msg, int sender){
		DDAARP_DestructRote msgdestruct = (DDAARP_DestructRote) msg;
		   
		   if( (this.eventnum == msgdestruct.EventNum) && (this.ID == msgdestruct.NextHop) && (this.rota) && (this.filhos.contains((Object)sender))){
			   if(this.filhos.size()==1){
				   this.rota = false;
				   this.HopToEvent = 100000;
				   if(this.myrole == Roles.RELAY)
					   this.setColor(Color.magenta);
			   
				   this.filhos.remove((Object)sender);
			   
				   Message destruct = new DDAARP_DestructRote(this.NextHop,msgdestruct.EventNum);
				   MessageLeoTimer anexaTimer = new MessageLeoTimer(destruct);
				   anexaTimer.startRelative(0.0000001,this);
				   //this.Energy = this.Energy - ConsumptionInTransmission;
				   battery.spend(EnergyMode.SEND);
			   }else{this.filhos.remove((Object)sender);}
		      
		   }
	}
	public void Clustering(){
		myrole = Roles.COORDINATOR;
		this.HopToCoordinator = 0;
		this.setColor(Color.BLUE);
		MessageLeoTimer annmsg = new MessageLeoTimer(new DDAARP_AlertMessage(this.ID, this.HopToSink,this.eventnum,this.HopToEvent,this.HopToCoordinator+1, this.ID, this.ID, this.HopToEvent, this.HopToSink));
		double time = new UniformDistribution(0,0.05).nextSample();
		annmsg.startRelative(time, this);
		//this.Energy = this.Energy - ConsumptionInTransmission;
		battery.spend(EnergyMode.SEND);
//		infraLeoLog.logln("Detection t " + (Global.currentTime )
//				+ " Ns "+ this.ID 
//				+" Ev " +this.eventnum);
		Overheads = Overheads +1;
		Tools.appendToOutput("Event Ann. "+ Integer.toString(this.ID)+"\n");
		mystatus = Status.CLUSTERING2;
		timerVICINITY.tnoStartRelative(8, this, TNO.TNO_CLUSTERING2);		    
	}
	
	public void AlertNewEvent(){
		mystatus = Status.READY;	
		if (myrole == Roles.COORDINATOR){
			this.setColor(Color.BLUE);
			this.HopToEvent = 0;
			
			if ((this.eventnum ==1) && (this.first)){
				this.first = false;
				timerVICINITY.tnoStartRelative(0.0001, this, TNO.TNO_ANEXATOSINK);
			}else if(this.eventnum>=2){
					timerVICINITY.tnoStartRelative(0.0001, this, TNO.TNO_ANEXATOSINK);
				}	
		}else if(myrole == Roles.COLLABORATOR){
				this.mystatus = Status.MONITORING;
				timerVICINITY.tnoStartRelative(this.DataRate, this, TNO.TNO_MONITORING);
				this.setColor(Color.magenta);
				this.HopToEvent = 0;
				announceCH();
			}	
	}
	
	public void Monitoring(){
		if ((this.myrole == Roles.COLLABORATOR) || (this.myrole == Roles.COORDINATOR))
		{
			this.senddata = true;
			this.rota = true;
			sendData();  // envia dados	
		}
	}
	
	public void AnexToSink(){
		anexasink();
		if (this.mystatus!=Status.MONITORING){
			this.mystatus = Status.MONITORING;
			timerVICINITY.tnoStartRelative(this.DataRate, this, TNO.TNO_MONITORING);
		}
	}

	public void timeout(TNO tno){
		switch(tno){
			
		case TNO_VICINITY:
			Clustering(); //Groups the nodes that detected the event and elects a node as Leader.
			break;   

		case TNO_CLUSTERING2:
			AlertNewEvent();//Make the Route to the current event.
			
			break;
		
		case TNO_ANEXATOSINK:
			AnexToSink();
			break;
			
		case TNO_MONITORING:
			Monitoring(); //Monitoring the Ambient
			break;	
		}
	}
	
	public boolean isGraphConnected(){
		for (Node n : Tools.getNodeList()){
			int edges = n.outgoingConnections.size();
			if (edges==0)
				return false;
		}
		return true;
	}
	
	public int getHopToSink(){
		return this.HopToSink;
	}
	
//*************************************************************************************************//
	//## Daki pra baixo ão coisas do SINALGO
	
	

	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// TODO Auto-generated method stub
		super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.ID), 8, Color.WHITE);
		
	}
	
	
	// My methods
	@NodePopupMethod(menuText="Set Sink and Start Simulation")
	public void myPopupMethod() {
		this.setColor(Color.RED);
		this.myrole = Roles.SINK;
		this.mystatus = Status.READY;
		this.HopToSink = 0;
		sendMCI();
	}
	
	@NodePopupMethod(menuText="Meu Papel")
	public void myRole() {
		Tools.appendToOutput(myrole+"\n");
	}
	@NodePopupMethod(menuText="Meu Status")
	public void myStatus() {
		Tools.appendToOutput(mystatus+"\n");
	}
	
	@NodePopupMethod(menuText="NeighborHopBigger")
	public void NeighborHopBigger() {
		Tools.appendToOutput("NHB "+this.NeighborHopBigger+" size: "+this.NeighborHopBigger.size()+"\n");
	}
	@NodePopupMethod(menuText="ReceivedNeighborHopBigger")
	public void ReceivedNeighborHopBigger() {
		Tools.appendToOutput("RNHB "+this.ReceivedNeighborHopBigger+"size: "+this.ReceivedNeighborHopBigger.size()+"\n");
	}
	
	@NodePopupMethod(menuText="filhos")
	public void filhos() {
		Tools.appendToOutput("My filhos "+this.filhos+"size: "+this.filhos.size()+"\n");
	}
	@NodePopupMethod(menuText="filhosrecv")
	public void filhosrecv() {
		Tools.appendToOutput("Recebi "+this.filhosrecv+"\n");
	}	
	@NodePopupMethod(menuText="filhossend")
	public void filhossend() {
		Tools.appendToOutput("Enviei "+this.filhossend+"\n");
	}	
	@NodePopupMethod(menuText="Exibir Tabela")
	public void myaggDist() {
		Tools.appendToOutput("HopToSink: " + this.HopToSink + "\n" +
							 "NextHop: " + this.NextHop + "\n" +
							 "Energy: " + this.battery.getEnergy()+
							 "OwnerID:" + this.ownerID +"\n"+
							 "OwnerID_Hop:" + this.ownerID_Hop +"\n"+
							 "HopToEvent: " + this.HopToEvent + "\n" +
							 "eVENTnuM:: " + this.eventnum + "\n"+
							 "ROTA:  " + this.rota + "\n"+
							 "HopToEvent_num" + this.HopToEvent_Num + "\n"+
							 "HopToCoordinator" + this.HopToCoordinator + "\n"
							 );
	}	
	
	@NodePopupMethod(menuText="Exibe Tabela de Roteamento")
	public void printHashTable() {
		//Set<Integer> keyset = routingtable.keySet();
		Iterator<EventKey> it = routingtable.keySet().iterator();
		EventKey next;
		Tools.appendToOutput("Routing Table of Node "+ Integer.toString(this.ID)+"\n" );
	//	Iterator<Integer> it = keyset.iterator();
		while(it.hasNext()){
			next = it.next();
			Tools.appendToOutput(next.ID +" " 
					//+routingtable.get(next).NextHop +" " 
					+routingtable.get(next).HopsToSink+" "
					//+routingtable.get(next).HopsToTree+" "
					+routingtable.get(next).Energy+"\n"
					);
		}
	}	
	
	
	@NodePopupMethod(menuText="Exibe Tabela de Roteamento TREE")
	public void printHashTable2() {
		//Set<Integer> keyset = routingtable.keySet();
		Iterator<EventKey> it = routingtable.keySet().iterator();
		EventKey next;
		Tools.appendToOutput("Routing Table of Node "+ Integer.toString(this.ID)+"\n" );
	//	Iterator<Integer> it = keyset.iterator();
		while(it.hasNext()){
			next = it.next();
			if(this.eventnum != next.eventID){
			Tools.appendToOutput(next.ID +" " 
					//+routingtable.get(next).NextHop +" " 
					//+routingtable.get(next).HopsToSink+" "
					+routingtable.get(next).HopsToTree+" "
					+routingtable.get(next).Energy+"\n"
					);
			}
		}
	}	
	

}