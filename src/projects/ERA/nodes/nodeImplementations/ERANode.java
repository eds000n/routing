package projects.ERA.nodes.nodeImplementations;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import projects.ERA.nodes.messages.CHAnnouncementMessage;
import projects.ERA.nodes.messages.DeadNodeMessage;
import projects.ERA.nodes.messages.MCIMessage;
import projects.ERA.nodes.messages.MSGTREE;
import projects.ERA.nodes.messages.RequestRouteMessage;
import projects.ERA.nodes.messages.ERADataMessage;
import projects.ERA.nodes.messages.SetRepairRouteMessage;
import projects.ERA.nodes.timers.CHFormationTimer;
import projects.ERA.nodes.timers.CHSelectionTimer;
import projects.ERA.nodes.timers.CreateAdjListTimer;
import projects.ERA.nodes.timers.EndERATimer;
import projects.ERA.nodes.timers.EventEndERATimer;
import projects.ERA.nodes.timers.EventERATimer;
import projects.ERA.nodes.timers.GraphConnectivityTimer;
import projects.ERA.nodes.timers.MessageERATimer;
import projects.ERA.nodes.timers.RREQTimer;
import projects.ERA.nodes.timers.RepairDeadNodeTimer;
import projects.ERA.nodes.timers.ERATimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.TimerCollection;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.UniformDistribution;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.models.EnergyModel.IEnergy;
import sinalgo.models.EnergyModel.simple.SimpleEnergy;

public class ERANode extends Node {
	private int HopToSink   = 100000;  			// Distance in Hops to Sink
	private int NextHopSink		= 100000; 		// Next node to route the packets to reach the sink (My Relay of Data) 
	private int ownerID		= this.ID;			// My OwnerID
	private boolean senddata = false;			//
	private int eventnum = 0;					//
	public int getEventnum() {
		return eventnum;
	}

	public void setEventnum(int eventnum) {
		this.eventnum = eventnum;
	}

	private int aggregatePCKT =0;
	private int msgAggPckts=0;
	private int Disttree = 0;
	private boolean rota = false;
	private double nextsenddata = 0;
	private int Disttreerecv =0;
	private boolean sentmci = false;
	private boolean timerAggOn = false;

	public static int EventsAmount = 0;
	public static int EventsTimes = 0;
	public static int EventSize=0;
	public static int CommunicationRadius = 0;
	public static int DropRate = 0;
	public static int Receivers =0;			//Quantity of packets received by the SINK 
	public static int Detects =0;
	public static int Overheads =0;			//Quantity of packets used for configuring the network, ie, setting the distance in hops to the sink and the nexthopsink for each node
	public static int DataPackets =0;		//Quantity of data packets (just ERADataMessage) sent through the network
	public static int Edges =0;				//Quantity of edges in the shortest path tree. It is set at the end of the simulation so that all the events are gathered.
	public static int DataRate =0;
	public static int Density =0;

	public static int Notifications =0;
	public static double SimulationTime =0;
	public static Logging Energy = Logging.getLogger("ERAEnergy.txt", true);
	//private IEnergy battery;
	private SimpleEnergy battery;
	private static int Tch = 1000;									//maximum allotted time for CH selection
	private ArrayList<CHInfo> neighborCH = new ArrayList<>();		//list of CH neighbors
	private boolean CHcandidate = true;
	private ArrayList<PNInfo> parentNodeCH = new ArrayList<>();	//list of Parent Nodes (PN) for the CHs
	private int countCH = 0;
	
	private UniformDistribution uniformRandom =  new UniformDistribution(0,0.016);
	//Logging sptLog;
	public static Logging ERA = Logging.getLogger("ERALog.txt", true);

	private ArrayList<Integer> lastFloodedCHs = new ArrayList<Integer>();
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
	}
	private enum Status {MONITORING, READY};
	public enum Roles { SINK, COLLABORATOR, RELAY, CH };
	public enum TNO {TREE, MONITORING, AGGREGATION};
	private ERATimer timerTREE;
	public ERATimer getTimerTREE() {
		return timerTREE;
	}

	public void setTimerTREE(ERATimer timerTREE) {
		this.timerTREE = timerTREE;
	}

	private ERATimer timerAgg;
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
	private boolean sentmana=false;
	private ArrayList<Integer> filhos = new ArrayList<Integer>();		//List of nodes that are connected to the current node. Note that by connected we mean that the son node sends its data trough the current node.
	private ArrayList<Integer> filhossend = new ArrayList<Integer>();
	private ArrayList<Integer> filhosrecv = new ArrayList<Integer>();

	private static int perdidos =0;
	private static int enviados =0;
	private static int enviadosAgg = 0;
	private static int perdidosAGG =0;
	private static int packetrecvagg = 0;

	/***********************************************************/
	private boolean isDead = false ;
	private LinkedList<Integer> listAll = new LinkedList<Integer>();	//A list containing the IDs of ALL the neighbor nodes, 1-indexed
	public static ArrayList<Integer> UpNodes = new ArrayList<>(); 	//1: node awaken, 0: dead node 0-indexed
	public static ArrayList<Integer> DisconnectedNodes = new ArrayList<>(); 	//1: node connected, 0: disconnected 0-indexed
	public static List<Integer>[] AdjList; 		// Adjacency list 0-indexed
	private boolean runUpdateAdjacencyMatrix = false;
	public static ArrayList<Integer> terminals = new ArrayList<Integer>();		//List of terminal nodes, 0-indexed
	public static Logging debugLog = Logging.getLogger();	//Prints to the console
	/***********************************************************/
	// Position File: /home/edson/workspace/SinalgoALL/src/projects/ERA/PosFile1.pos
	@Override
	public void handleMessages(Inbox inbox) {
		// TODO Auto-generated method stub
		int sender;

		//Spent energy due to the listening mode
		this.battery.spend(EnergyMode.LISTEN,1);

		while(inbox.hasNext()) {
			Message msg = inbox.next();
			sender = inbox.getSender().ID;
			//Message processing
			this.battery.spend(EnergyMode.RECEIVE);


			if (msg instanceof DeadNodeMessage){		//Message sent from the dead node to the sink
				ReportDeadNode(msg);
			}
			if (msg instanceof SetRepairRouteMessage){	//Message to set repair (fix) the route of the nodes that became orphan
				SetRepairRoute(msg);
			}
			
			if (msg instanceof CHAnnouncementMessage){
				setCH(msg);
			}
			
			if (msg instanceof RequestRouteMessage){
				setRREQ(msg);
			}
			//Sink start the flooding of message MCI for configuration initial
			if(msg instanceof MCIMessage) {
				MCIMessage mcimsg = (MCIMessage) msg;
				if ( !listAll.contains( mcimsg.getSenderID() ) ) {
					listAll.add( mcimsg.getSenderID() );
				}
				
				if(this.HopToSink > mcimsg.getHopToSink()){
					this.setColor(Color.GREEN);
					this.NextHopSink = mcimsg.getSenderID();
					this.HopToSink = mcimsg.getHopToSink();
					mcimsg.setHopToSink(this.HopToSink+1);
					mcimsg.setSenderID(this.ID);

					if(!this.sentmci)
					{
						MessageERATimer MCICNS = new MessageERATimer(mcimsg);
						MCICNS.startRelative(this.HopToSink+1,this);
						this.sentmci = true;

						//						sptLog.logln("Flooding t " + (Global.currentTime)
						//							+ " Ns "+ this.ID 
						//							+ " Pi " + msg.hashCode() 
						//							+ " Pt " +"wsn-infra-sink");

						Overheads = Overheads +1;
						//Spent energy due to the transmission mode
						this.battery.spend(EnergyMode.SEND);
						Tools.appendToOutput("handleMessages() MCIMessage Overheads: " + this.Overheads + "\n"); 
					}
				}
			}

			if (msg instanceof MSGTREE){
				//Tools.appendToOutput("MSGTREE: from" + this.ID + " to " + this.NextHopSink + ", distance (in hops):" + this.HopToSink + "\n");
				MSGTREE msgtree = (MSGTREE) msg;
				
				if(this.ID == msgtree.getNexthop()){
					Tools.appendToOutput(this.ID + " received MSGTREE from " + msgtree.getSentnode() + " with disttree " + msgtree.getDisttree()+"\n");
					this.filhosrecv.add(msgtree.getSentnode());

					if(this.myRole != Roles.SINK)
					{
						this.setColor(Color.yellow);
						this.Disttree = this.Disttree + msgtree.getDisttree();
						this.Disttreerecv = this.Disttreerecv +1;

						if(this.filhos.size() == 1){
							this.filhossend.add(this.NextHopSink);
							broadcast(new MSGTREE(this.Disttree+1, this.NextHopSink,this.ID));
							this.setColor(Color.black);
						}
						else if(this.filhos.size()==this.Disttreerecv){
							broadcast(new MSGTREE(this.Disttree+1, this.NextHopSink,this.ID));
							this.filhossend.add(this.NextHopSink);
							this.setColor(Color.white);
						}
					}else{
						
						this.Disttree = this.Disttree + msgtree.getDisttree();
						this.Disttreerecv = this.Disttreerecv +1;
						//Tools.appendToOutput("filhos: "+this.filhos.size()+"  rec: "+ this.Disttreerecv);
						this.setColor(Color.black);	
						if(this.filhos.size() == 1){
							Edges = this.Disttree;
						}else{
							if(this.filhos.size()==this.Disttreerecv){
								Edges = this.Disttree;
							}
						}
						
						Tools.appendToOutput("handleMessages() SINK disttree: " + this.Disttree + ", disttreerecv: " + this.Disttreerecv + 
								", filhos: " + this.filhos.size() + ", Edges: " + this.Edges +"\n");
					}
				}	
			}

			//Sent information about event to Sink
			if(msg instanceof ERADataMessage){
				processData(msg);
			}
		}
	}

	private void processData(Message msg) {
		ERADataMessage mdata = (ERADataMessage)msg;

		/*if(this.ID == mdata.getDest()){
			if (!this.filhos.contains((Object)sender)){
				this.filhos.add(sender);
			}
		}*/

		if(this.ID == mdata.getDest()){

			//if (!this.son.contains((Object) inbox.getSender().ID))
			//	son.add(inbox.getSender().ID);

			if ((this.myRole != Roles.SINK) ){
				float eta = 0;
				for (PNInfo p : parentNodeCH )
					eta += p.residual_energy;
				eta /= parentNodeCH.size();
				
				for( int i=0; i<parentNodeCH.size(); i++ ){
					ArrayList<Integer> W = new ArrayList<>();
					if ( parentNodeCH.get(i).residual_energy >= eta ){
						W.add(i);
					}
					int max = Collections.max(W);
					int midx = W.indexOf(max);
					
					int routeTo = 0;
					ArrayList<Integer> Nop = new ArrayList<>();
					for (int k=0; k<W.size(); k++){
						Nop.add(W.get(k)/W.get(midx));
					}
					int sum = 0;
					for ( Integer n : Nop )
						sum +=n;
//					countCH = (countCH + 1)%W.size();
					int k=0;
					int nextNode = 0;
					int tmp = countCH;
					while(true){
						if ( tmp < Nop.get(k) ){
							nextNode = W.get(k);
							break;
						}else{
							tmp -= Nop.get(k);
							k++;
						}
					}
					
					countCH = (countCH + 1)%sum;
					DataPackets = DataPackets +1;
					MessageERATimer msgTimer = new MessageERATimer(mdata,Tools.getNodeByID(W.get(nextNode)));
					msgTimer.startRelative(0.1,this);
					this.battery.spend(EnergyMode.SEND);
				}
				/*
				double time = uniformRandom.nextSample();

				if(Global.currentTime > this.nextsenddata)
					this.senddata =true;


				this.aggregatePCKT = this.aggregatePCKT + mdata.getAggPacket();
				this.rota = true;
				if( (this.filhos.size()>1)){
					this.setColor(Color.blue);
				}else{
					this.setColor(Color.yellow);
				}

				//if ( ( (this.filhos.size()<2) && (this.myRole == Roles.RELAY)) ){
				if ( ( (this.filhos.size()<2) ) ){
					mdata.setDest(this.NextHopSink);
					mdata.setSender(this.ID);
					mdata.setHopToSink(this.HopToSink);
					MessageERATimer msgTimer = new MessageERATimer(mdata,Tools.getNodeByID(this.NextHopSink));
					enviados = enviados + 1;
					msgTimer.startRelative(0.1,this);	//The message is sent almost inmediately

					//									sptLog.logln("Sdata t "+ (Global.currentTime+ time)
					//										+ " Ns "+ this.ID 
					//										+ " Ap " + aggregatePCKT 
					//										+ " Nd " + 1);

					DataPackets = DataPackets +1;
					Tools.appendToOutput("handleMessages(): Datapackets: "+this.DataPackets + "\n");
					//Spent energy due to the transmission mode
					this.battery.spend(EnergyMode.SEND);
					if (this.myRole == Roles.RELAY)
						this.setColor(Color.CYAN);
					this.aggregatePCKT = 0;
				}//else if ((this.senddata) && (this.myRole != Roles.COLLABORATOR)){
				else if ((this.senddata)){
					mdata.setDest(this.NextHopSink);
					mdata.setSender(this.ID);
					mdata.setHopToSink(this.HopToSink);
					mdata.setEventNum(this.eventnum);
					mdata.setAggPacket(this.aggregatePCKT);
					nextsenddata = Global.currentTime + DataRate;

					MessageERATimer msgTimer = new MessageERATimer(mdata,Tools.getNodeByID(this.NextHopSink));
					msgTimer.startRelative(DataRate,this);	// The message is sent in DataRate seconds, so it holds a while for some other packets to reach this node and the dispatchs all of them together.
					//						    		sptLog.logln("Sdata t "+ (Global.currentTime+ time)
					//										+ " Ns "+ this.ID 
					//										+ " Ap " + aggregatePCKT 
					//										+ " Nd " + 1);
					DataPackets = DataPackets +1;
					Tools.appendToOutput("handleMessages(): Datapackets: "+this.DataPackets + "\n");
					this.rota = true;
					enviadosAgg = enviadosAgg + this.aggregatePCKT;
					this.senddata = false;
					this.aggregatePCKT = 0;
					//if(this.myrole == Roles.RELAY)
					this.setColor(Color.black);

				}*/
			}


			if(this.myRole == Roles.SINK)
			{
				//						sptLog.logln("Rdata t " + Global.currentTime
				//								+ " Ns " + inbox.getSender()
				//								+ " Ap " + mdata.getAggPacket()
				//								+ " Nd " + this.ID
				//								);
				Receivers = Receivers +1;
				packetrecvagg = packetrecvagg + mdata.getAggPacket();
				Tools.appendToOutput("handleMessages(): ERADataMessage arrived to the SINK, Receivers: " +this.Receivers +"\n");
			}

		}
	}

	private void setRREQ(Message msg) {
		RequestRouteMessage rrqe = (RequestRouteMessage) msg;
		PNInfo pni = new PNInfo(rrqe.getID(), rrqe.getLevel(), rrqe.getResidual_energy(), rrqe.getX(), rrqe.getY());
		if ( myRole == Roles.CH ){
			this.HopToSink = rrqe.getLevel() + 1;
			parentNodeCH.add(pni);
			
			RequestRouteMessage rrqm = new RequestRouteMessage(this.ID, this.HopToSink, battery.getEnergy(), getPosition().xCoord, getPosition().yCoord);
			RREQTimer t = new RREQTimer(this.ID, rrqm);
			t.startRelative(0.01, Tools.getNodeByID(this.ID));
		}
	}

	private void setCH(Message msg) {
		CHAnnouncementMessage cham = (CHAnnouncementMessage)msg;
//		if ( myRole != Roles.CH && ((ERANode)Tools.getNodeByID(cham.getID())).getMyRole() == Roles.CH ){
		if ( myRole != Roles.CH ){
			CHInfo chi = new CHInfo(cham.getID(), cham.getResidual_energy(), cham.getX(), cham.getY());
			if ( !neighborCH.contains(chi) ){
				neighborCH.add(chi);
			}
			CHcandidate = false;
			setColor(Color.GREEN);
			myRole = Roles.COLLABORATOR;
			eraseTimers(this.ID);			//Delete CH timer from this node
			
			/*if ( this.ID == 7 )
				debugMsg("7: " + myRole);
			if ( this.ID == 288 ){
				ERANode en = (ERANode)Tools.getNodeByID(cham.getID());
				System.out.println(en.myRole);
			}*/
			
			/*
			float mu = 0;
			for (Float e : energiesCH )
				mu += e;
			mu /= energiesCH.size();
			
			double dmin=1000;
			int idx = -1;
			for (int i=0; i < neighborCH.size(); i++){
				if ( energiesCH.get(i) >= mu ){//Join CH!
					double dx = cham.getX() - getPosition().xCoord;
					double dy = cham.getY() - getPosition().yCoord;
					double d = Math.sqrt( dx*dx + dy*dy );
					if ( d < dmin )
						idx = i;
				}
			}
			this.NextHopSink = neighborCH.get(idx);*/
		}
	}

	@Override
	public void handleNAckMessages(NackBox nackBox) {
		while(nackBox.hasNext()) {
			Message msg = nackBox.next();
			if(msg instanceof ERADataMessage){
				ERADataMessage Data = (ERADataMessage) msg;

				if(Data.getAggPacket() > 1){
					perdidosAGG = perdidosAGG + Data.getAggPacket();
				}else{
					perdidos = perdidos+1;
				}

			}

		}

	}


	public void aproximationtree(){
		MessageERATimer msgtree = new MessageERATimer (new MSGTREE(1,this.NextHopSink,this.ID));
		Tools.appendToOutput("aproximationtree(): Sending MSGTREE from " + this.ID + " to " + this.NextHopSink + " with disttree 1\n");
		double time = uniformRandom.nextSample();
		msgtree.startRelative(time, this);	
	}
	@Override
	/*
	 * Init is called for each node when they are generated. Then the actual simulation can be executed. 
	 * @see sinalgo.nodes.Node#init()
	 */
	public void init() {

		// TODO Auto-generated method stub
		myRole = Roles.RELAY;	

		//double endTime=0;


		try {
			SimulationTime = sinalgo.configuration.Configuration.getDoubleParameter("SimTime");
			EventsAmount = sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents");
			EventsTimes = sinalgo.configuration.Configuration.getIntegerParameter("Event/Time");
			eventEndTime = sinalgo.configuration.Configuration.getDoubleParameter("Event/EventEnd");
			Density = sinalgo.configuration.Configuration.getIntegerParameter("Density");
			for (int i = 1; i <= sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents"); i++) {
				EventERATimer t = new EventERATimer(i);
				t.startEventAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventStart"+i), this, i);
				if (i==1 && this.ID==1){
					GraphConnectivityTimer gct = new GraphConnectivityTimer(this);
					gct.startAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventStart"+i) - 0.1, this);
				}
			}
			
			double maxEndingTime = 0;
			for (int i = 1; i <= sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents"); i++) {
				EventEndERATimer t = new EventEndERATimer(i,this);
				double etime = sinalgo.configuration.Configuration.getDoubleParameter("Event/EventEnd"+i);
				t.startAbsolute(etime, this);
				if (maxEndingTime < etime)
					maxEndingTime = etime;
			}
			
			if (maxEndingTime <= eventEndTime){
				eventEndTime = maxEndingTime - 210;
				//SimulationTime = maxEndingTime -10;//100;
				System.out.println("maxEndingTime <= eventEndTime: " +eventEndTime + " " + SimulationTime);
			}

			DataRate = sinalgo.configuration.Configuration.getIntegerParameter("Event/DataRate");
			EventSize = sinalgo.configuration.Configuration.getIntegerParameter("Event/EventSize");
			CommunicationRadius = sinalgo.configuration.Configuration.getIntegerParameter("UDG/rMax");
			DropRate = sinalgo.configuration.Configuration.getIntegerParameter("TaxadePerda/dropRate");

		} catch (CorruptConfigurationEntryException e) {
			// TODO: handle exception
			e.printStackTrace();
			System.exit(0);
		}

		try {

			String energyModel = Configuration.getStringParameter("Energy/EnergyModel");
			if (energyModel.contains("Simple")){
				battery = new SimpleEnergy(this.ID);
			}
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Energy Model not found");
			e.printStackTrace();
		}

		
		if (this.ID==1){
//			startERA();
			this.setColor(Color.RED);
			this.myRole = Roles.SINK;
			this.mystatus = Status.READY;
			
			UpNodes.add(1);
			DisconnectedNodes.add(1);
			
			CHSelectionTimer chst = new CHSelectionTimer(this.ID);
			chst.startRelative(0.1, this);
			//CreateAdjListTimer calt = new CreateAdjListTimer(this); 
			//calt.startAbsolute(2000, this);
		}else{
//			startERA();
			UpNodes.add(1);
			DisconnectedNodes.add(1);
		}

		
		//Event timers
		EndERATimer etimer = new EndERATimer();
		etimer.startAbsolute(SimulationTime, this);

		/*Tools.appendToOutput("Ending Init: "+ this.ID +"\n");
		Tools.appendToOutput("Overheads: " + this.Overheads + "\n");*/
	}

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

	public IEnergy getBateria() {
		return battery;
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// TODO Auto-generated method stub
		if (this.ID == 1) highlight = true;
		super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.ID), 8, Color.WHITE);


	}

	// My methods

	@NodePopupMethod(menuText="Meu Papel")
	public void myRole() {
		Tools.appendToOutput(myRole+"\n");
	}
	@NodePopupMethod(menuText="Meu Status")
	public void myStatus() {
		Tools.appendToOutput(mystatus+"\n");
	}

	@NodePopupMethod(menuText="Exibir Tabela")
	public void myaggDist() {
		if ( myRole == Roles.CH ){
			Tools.appendToOutput( "CH " + this.ID + "\n");
			for ( int i=0; i< parentNodeCH.size(); i++ ){
				PNInfo pni = parentNodeCH.get(i);
				Tools.appendToOutput( " " + pni.ID + ":" + pni.residual_energy );
			}
			Tools.appendToOutput( "\n" );
		}else if ( myRole == Roles.COLLABORATOR ){
			Tools.appendToOutput(//"CH membership: " + this.HopToSink + "\n" +
					"CH membership: " + this.NextHopSink + "\n"// +
					//"OwnerID:" + this.ownerID +"\n"
					);
			for ( int i=0; i<neighborCH.size(); i++ ){
				CHInfo chi = neighborCH.get(i);
				Tools.appendToOutput(" " + chi.ID + ":" + chi.residual_energy);
			}
			Tools.appendToOutput("\n");
		}
		
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
		//if (Math.pow((xc - p.xCoord),2) + Math.pow((yc -p.yCoord),2) < Math.pow(r,2)){

			this.eventnum = eventID;
			return true;
		}
		else 
			return false;
	}

	/**
	 * This method is called when an event is detected by a node. Then, the node is painted with cyan, which 
	 * indicates that the node is monitoring (that is, gathering data), the node is set as COLLABORATOR and
	 * the ERATimer is scheduled to start in a time DataRate+10. 
	 * That timer fires the method timeout()
	 */
	public void startDetection(){
		myRole = Roles.COLLABORATOR;
		this.setColor(Color.cyan);
		//		sptLog.logln("Detection t "+(Global.currentTime) 
		//					+" Ns "+ this.ID 
		//					+" Ev " +this.eventnum);
		Detects = Detects +1;
		this.mystatus = Status.MONITORING;
		timerTREE = new ERATimer(this,TNO.MONITORING);
		timerTREE.tnoStartRelative(DataRate+10, this, TNO.MONITORING);
	}
	
	public void CHSelection(){
		debugMsg("calling from " + this.ID);
		for(Node node : Tools.getNodeList()){
			debugMsg(" = " + node.ID);
			ERANode enode = (ERANode)node; 
			enode.CHcandidate = true;
			enode.setColor(Color.BLUE); 
			enode.CHcandidate = true;
			MessageERATimer mcimsg = new MessageERATimer (new CHAnnouncementMessage(enode.ID, enode.battery.getEnergy(), enode.getPosition().xCoord, enode.getPosition().yCoord));
			float time = (enode.battery.getInitialEnergy() - enode.battery.getEnergy())/enode.battery.getInitialEnergy()*ERANode.Tch + 0.001f*enode.ID;
			mcimsg.startRelative(time, enode);
			Overheads = Overheads + 1;
			enode.battery.spend(EnergyMode.SEND);
			
			CHFormationTimer chtimer = new CHFormationTimer(enode);
			chtimer.startRelative(time + 0.1*Tools.getNodeList().size(), enode);
		}
	}
	
	public void ClusterFormation(){
		if  ( this.myRole == Roles.COLLABORATOR ){
			float mu = 0;
			for (CHInfo e : neighborCH )
				mu += e.residual_energy;
			mu /= neighborCH.size();

			double dmin=1000;
			int idx = -1;
			for (int i=0; i < neighborCH.size(); i++){
				CHInfo c = neighborCH.get(i);
				if ( c.residual_energy >= mu ){//Join CH!
					double dx = c.x - getPosition().xCoord;
					double dy = c.y - getPosition().yCoord;
					double d = Math.sqrt( dx*dx + dy*dy );
					if ( d < dmin )
						idx = i;
				}
			}
			this.NextHopSink = neighborCH.get(idx).ID;
		}
	}
	
	//Node Sink start the Flooding of MCI
	public void startERA(){
		this.HopToSink = 0; 	// I am Sink
		this.NextHopSink = this.ID;	// I am SInk
		
		//MessageERATimer mcimsg = new MessageERATimer (new MCIMessage(this.HopToSink+1,this.ID));
		MessageERATimer mcimsg = new MessageERATimer (new CHAnnouncementMessage(this.ID, battery.getEnergy(), getPosition().xCoord, getPosition().yCoord));
		float time = (battery.getInitialEnergy() - battery.getEnergy())/battery.getInitialEnergy()*Tch + 0.001f*this.ID;
		mcimsg.startRelative(time, this);
		setColor(Color.BLUE);
		//		sptLog.logln("Flooding t " + (Global.currentTime)
		//				+ " Ns "+ this.ID 
		//				+ " Pi " + mcimsg.hashCode() 
		//				+ " Pt " +"wsn-infra-sink");
		Overheads = Overheads + 1;
		//Spent energy due to the transmission mode
		this.battery.spend(EnergyMode.SEND);
	}

	public void sendData(){
		if (Global.currentTime > eventEndTime){ // This is executed when the current time is bigger than the time set up for the simulation
			if(this.filhos.size() == 0){
				aproximationtree();	
				this.setColor(Color.BLACK);
			}
			return;
		}
		//int myAgg = aggregatePCKT;
		int myMsgAgg = msgAggPckts;
		//clear counts
		aggregatePCKT = 0;
		msgAggPckts = 0;
		this.setColor(Color.orange);
		double time = uniformRandom.nextSample();

		if((this.myRole == Roles.COLLABORATOR) && (this.filhos.size() ==0)){
			this.setColor(Color.RED);
			this.timerTREE.tnoStartRelative(DataRate, this, TNO.MONITORING);	
			Message mdata = new ERADataMessage(this.ID,this.NextHopSink,"Sink",this.HopToSink,1,100,eventnum);
			MessageERATimer msgTimer = new MessageERATimer(mdata,Tools.getNodeByID(this.NextHopSink));
			time += (double)DataRate;
			msgTimer.startRelative(DataRate,this);
			//			sptLog.logln("Sdata t "+ (Global.currentTime+ time)
			//					+ " Ns "+ this.ID 
			//					+ " Ap " + 1
			//					+ " Nd " + 1);
			//Tools.appendToOutput("SData :"+ this.ID +"\n");
			Tools.appendToOutput("sendData(): Sending ERADataMessage from: "+ this.ID + " to " + this.NextHopSink + "distance (in hops): "+this.HopToSink + "\n");
			enviados = enviados + 1;
			Notifications = Notifications + 1;
			DataPackets = DataPackets+1;
			Tools.appendToOutput("sendData(): Datapackets: "+this.DataPackets + "\n");
			//Spent energy due to the transmission mode
			this.battery.spend(EnergyMode.SEND);
		}
	}

	public void printDegree(){
		//		sptLog.logln(
		//				"Degree "+
		//				"Node "+
		//				this.ID + " " + 
		//				this.outgoingConnections.size() 
		//		);

		if (myRole ==  Roles.SINK){
			//			sptLog.logln("Perdidos " + perdidos);
			//			sptLog.logln("Agregados Perdidos  " + perdidosAGG);
			//			sptLog.logln("Enviados " + enviados);
			//			sptLog.logln("Agregados Enviados "+ enviadosAgg);
			//			sptLog.logln("Recebidos "+ packetrecvagg);
		}
	}


	public void timeout(TNO tno){
		switch(tno){
		case TREE:
			startERA(); // Make one Flooding of MCI (Message of Configuration Initial)
			break;

		case MONITORING:
			if (this.myRole == Roles.COLLABORATOR) 
			{
				this.rota = true;
				sendData(); 
			}
			break;

		}
	}
	@NodePopupMethod(menuText="son")
	public void son() {
		Tools.appendToOutput("My Son "+this.son+"\n");
	}	
	@NodePopupMethod(menuText="Timers")
	public void timers() {
		Tools.appendToOutput("My timers "+this.getTimers().size()+"\n");
	}	
	
	@NodePopupMethod(menuText="Energy")
	public void energy() {
		Tools.appendToOutput("Remaining energy for node " + this.ID + ": " +this.battery.getEnergy()+"\n");
		float min = this.battery.getEnergy();
		int node = this.ID;
		for (Node n : Tools.getNodeList()){
			if (min>((ERANode)n).getBateria().getEnergy() && this.ID!=1 && !ERANode.terminals.contains(n.ID-1) && ERANode.UpNodes.get(n.ID-1)==1){
				min = ((ERANode)n).getBateria().getEnergy();
				node = n.ID;
			}
		}
		Tools.appendToOutput("Node "+node+" has the lower residual energy of "+min+"\n");
	}
	
	
	public int getNextHopSink(){
		return this.NextHopSink;
	}
	
	public boolean isGraphConnected(){
		Tools.appendToOutput("isGraphConnected(): testing graph connectivity\n");
		for (Node n : Tools.getNodeList()){
			int edges = n.outgoingConnections.size();
			//Connections con = n.outgoingConnections;
			if (edges==0)
				return false;
				//Tools.exit();
			/*if (((ERANode)n).getNextHopSink()==100000){
				return false;
			}*/
		}
		return true;
	}
	
	/*********************************************************
	 *********************************************************/
	 
	@NodePopupMethod(menuText="DeadDisconnectedNodes")
	public void listDeadAndDisconnected(){
		Tools.appendToOutput("Dead nodes: ");
		for(int i=0; i<ERANode.UpNodes.size(); i++)
			if ( ERANode.UpNodes.get(i) == 0 )	//Dead Node!
				Tools.appendToOutput((i+1) + " ");
		Tools.appendToOutput("\n");
		Tools.appendToOutput("Disconnected nodes: ");
		for(int i=0; i<ERANode.DisconnectedNodes.size(); i++)
			if ( ERANode.DisconnectedNodes.get(i) == 0) //Disconnected node!
				Tools.appendToOutput((i+1) + " ");
		Tools.appendToOutput("\n");
		Tools.appendToOutput("Terminals: ");
		for(int i=0; i<ERANode.terminals.size(); i++)
			Tools.appendToOutput((ERANode.terminals.get(i)+1) + " ");
		Tools.appendToOutput("\n");
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public void broadcastDeadNodeRepairMessage(int iD) {
		// TODO Auto-generated method stub
		this.battery.spend(EnergyMode.SEND);
		
		//Overheads += 1;
		//RepairDeadNodeMessage rdnm = new RepairDeadNodeMessage(this.ID);
		//this.broadcast(rdnm);
//		debugMsg("Broadcasted RepairDeadNodeMessage for " + this.ID , 2);
		
		ArrayList<Integer> neighbors = new ArrayList<>();
		for ( int i=0; i<this.listAll.size(); i++ ){
			//Overheads += 1;
			ERANode neighbor_node = (ERANode)Tools.getNodeByID(this.listAll.get(i)); 
			neighbor_node.battery.spend(EnergyMode.RECEIVE);
	
			if ( neighbor_node.listAll.contains(this.ID) ){
				neighbor_node.listAll.removeFirstOccurrence(this.ID);
				//neighbor_node.listAll.remove(this.ID);
			}
				
			if ( neighbor_node.NextHopSink == this.ID ){
				neighbor_node.battery.spend(EnergyMode.SEND);
				//MessageERATimer routeTimer = new MessageERATimer(rrm, Tools.getNodeByID( this.NextHopSink ));
				//routeTimer.startRelative(0.001, this);
				//Overheads += 1;
				neighbors.add(this.listAll.get(i));
			}
		}
		DeadNodeMessage dnm = new DeadNodeMessage(iD, this.battery.getEnergy());
		dnm.setNeighbors(neighbors);
		this.send(dnm, Tools.getNodeByID(this.NextHopSink));
//		debugMsg("Generated DeadNodeMessage for " + dnm.getNodeID() + ", started its tranmission to the sink" , 2);
	}
	
	private void ReportDeadNode(Message msg) {
		DeadNodeMessage dnm = (DeadNodeMessage)msg;
		battery.spend(EnergyMode.LISTEN);
		if (this.ID==1) {//Sink
			debugMsg(">>> SINK recieved ReportDeadMessage from node " + dnm.getNodeID() + " reported itself as dead with battery of " + dnm.getReportedEnergy());
			//debugMsg(">>> GANode.UpNodes " + GANode.UpNodes.get(dnm.getNodeID() - 1));
			ERANode.UpNodes.set(dnm.getNodeID() - 1, 0);
			ERANode.DisconnectedNodes.set(dnm.getNodeID()-1, 0);
			
			updateConnectedComponents(dnm.getNeighbors());		//Updates the connected components and fixes, if possible, the neighbors of the dead node so they don't lose connectivity.
			
			//debugMsg(">>> GANode.UpNodes " + GANode.UpNodes.get(dnm.getNodeID() - 1));
			int dn = 0;
			for ( Integer i : DisconnectedNodes )
				dn += i;
			debugMsg(">>> SINK number of connected nodes " + dn);
			
		}else{
			send(dnm, Tools.getNodeByID(this.NextHopSink));
			battery.spend(EnergyMode.SEND);
//			Overheads+=1;
//			debugMsg("node " + this.ID + " retransmitting DeadNodeMessage for node " + dnm.getNodeID() + " list of reconfiguring nodes " + dnm.getNeighbors());
		}
	}
	
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
				
				Collections.reverse(path);
				path.remove(0);
				int destiny = path.get(0);
				path.remove(0);
				path.add(nodes.get(i));
				SetRepairRouteMessage srrm = new SetRepairRouteMessage(this.ID);
				srrm.setPath(path);
				RepairDeadNodeTimer rdnt = new RepairDeadNodeTimer(Tools.getNodeByID(1), srrm, Tools.getNodeByID(destiny));
				rdnt.startRelative(0.0001, Tools.getNodeByID(1));
//				RepairDeadNodeGATimer rtimer = new RepairDeadNodeGATimer(this);
			}
		for ( int i=0; i<ERANode.DisconnectedNodes.size(); i++ ){
			if (ERANode.DisconnectedNodes.get(i)==1 && !visited.get(i))
				bfsMarkDisconnected(i);
		}
		
		checkTerminalConnectivity();
	}
	
	private void checkTerminalConnectivity() {
		int n = 0;
		for(int i=0; i<ERANode.terminals.size(); i++){
			if ( ERANode.DisconnectedNodes.get(ERANode.terminals.get(i)) == 0 ){
				//ERANode ntmp = (ERANode)Tools.getNodeByID(ERANode.terminals.get(i)+1);
				//eraseTimers(ERANode.terminals.get(i)+1);
				n++;
			}
		}
		if ( n == ERANode.terminals.size() )//All the terminals are disconnectd, finish the simulation
			sinalgo.tools.Tools.exit();
	}
	
	/**
	 * Erases timers of the node
	 * @param nodeID id of the node to erase its timers. 1-indexed
	 */
	public void eraseTimers(int nodeID){//I am running in asynchronous mode! 
		Tools.getEventQueue().removeAllEventsForThisNode(Tools.getNodeByID(nodeID));
		//Tools.getEventQueue().dropEvent(Event e);
		/*Node n = Tools.getNodeByID(nodeID);
		TimerCollection timerCollection = n.getTimers();
		Iterator<sinalgo.nodes.timers.Timer> it = timerCollection.iterator();
		if ( timerCollection.size() > 0 )
			while ( it.hasNext() ){
				it.remove();
			}*/
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
				if (ERANode.UpNodes.get(v)==0)
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
		if (!found && ERANode.UpNodes.get(v2)==1){//If it was not found and is up (has energy) then mark it as disconnected
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
				if (ERANode.DisconnectedNodes.get(v)==0)
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
//		debugMsg(v1 + " disconnected, marking the nodes that reach it as disconnected");
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
				if (ERANode.DisconnectedNodes.get(v)==0)
					continue;
				if (visited.get(v)==0){//not yet visited
					visited.set(v, 1);//Mark as visited
					q.add(v);
					ERANode.DisconnectedNodes.set(v, 0);
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

	public void createAdjacencyMatrix() {
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
				ERANode u = (ERANode) Tools.getNodeByID(i+1);
				for(Integer v : u.listAll){
					AdjList[i].add(v-1);
				}
				deg += u.listAll.size();
			}
			this.runUpdateAdjacencyMatrix = true;
		}
		
//		debugMsg("Updated adjacency matrix");
//		debugMsg("Average degree: " + deg/n);
		Tools.appendToOutput("Updated adjacency matrix!\n");
		for(int i=0; i<n; i++){
			System.out.print(i+": ");
			for(int j=0; j<AdjList[i].size(); j++)
				System.out.print(AdjList[i].get(j)+" ");
			System.out.println();
		}
	}

	/**
	 * Method to process the SetRepairRouteMessage. This message is generated in the sink and 
	 * retransmited trough the path until reaching the last node.
	 * @param msg
	 */
	private void SetRepairRoute(Message msg) {
		SetRepairRouteMessage srrm = (SetRepairRouteMessage)msg;
//		debugMsg("))SetRepairRouteMessage rcvd by " + this.ID + " path " + srrm.getPath());
//		debugMsg(")))" + this.NextHopSink + " " + srrm.getNextHop() + ", " + srrm.getHopsToSink());
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
	
	
}

class CHInfo{
	double x, y, residual_energy;
	int ID;		//1-indexed
	public CHInfo(int ID, double residual_energy, double x, double y){
		this.ID = ID;
		this.residual_energy = residual_energy;
		this.x = x;
		this.y = y;
	}
}

class PNInfo{
	double x, y, residual_energy;
	int ID;		//1-indexed
	int level;
	public PNInfo(int ID, int level, double residual_energy, double x, double y){
		this.ID = ID;
		this.level = level;
		this.residual_energy = residual_energy;
		this.x = x;
		this.y = y;
	}
}