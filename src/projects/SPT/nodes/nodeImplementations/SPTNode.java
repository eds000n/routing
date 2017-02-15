package projects.SPT.nodes.nodeImplementations;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import projects.SPT.nodes.messages.MCIMessage;
import projects.SPT.nodes.messages.MSGTREE;
import projects.SPT.nodes.messages.SPTDataMessage;
import projects.SPT.nodes.timers.EndSPTTimer;
import projects.SPT.nodes.timers.EventEndSPTTimer;
import projects.SPT.nodes.timers.EventSPTTimer;
import projects.SPT.nodes.timers.GraphConnectivityTimer;
import projects.SPT.nodes.timers.MessageSPTTimer;
import projects.SPT.nodes.timers.SPTTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
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

public class SPTNode extends Node {
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
	public static int DataPackets =0;		//Quantity of data packets (just SPTDataMessage) sent through the network
	public static int Edges =0;				//Quantity of edges in the shortest path tree. It is setup at the end of the simulation so that all the events are gathered.
	public static int DataRate =0;
	public static int Density =0;

	public static int Notifications =0;
	public static double SimulationTime =0;
	public static Logging Energy = Logging.getLogger("SPTEnergy.txt", true);
	private IEnergy battery;


	private UniformDistribution uniformRandom =  new UniformDistribution(0,0.016);
	//Logging sptLog;
	public static Logging SPT = Logging.getLogger("SPTLog.txt", true);

	private ArrayList<Integer> lastFloodedCHs = new ArrayList<Integer>();
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
	}
	private enum Status {MONITORING, READY};
	public enum Roles { SINK, COLLABORATOR, RELAY };
	public enum TNO {TREE, MONITORING, AGGREGATION};
	private SPTTimer timerTREE;
	public SPTTimer getTimerTREE() {
		return timerTREE;
	}

	public void setTimerTREE(SPTTimer timerTREE) {
		this.timerTREE = timerTREE;
	}

	private SPTTimer timerAgg;
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

	// Position File: /home/edson/workspace/SinalgoALL/src/projects/SPT/PosFile1.pos
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


			//Sink start the flooding of message MCI for configuration initial
			if(msg instanceof MCIMessage) {
				MCIMessage mcimsg = (MCIMessage) msg;
				if(this.HopToSink > mcimsg.getHopToSink()){
					this.setColor(Color.GREEN);
					this.NextHopSink = mcimsg.getSenderID();
					this.HopToSink = mcimsg.getHopToSink();
					mcimsg.setHopToSink(this.HopToSink+1);
					mcimsg.setSenderID(this.ID);

					if(!this.sentmci)
					{
						MessageSPTTimer MCICNS = new MessageSPTTimer(mcimsg);
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
			if(msg instanceof SPTDataMessage){
				SPTDataMessage mdata = (SPTDataMessage)msg;

				if(this.ID == mdata.getDest()){
					if (!this.filhos.contains((Object)sender)){
						this.filhos.add(sender);
					}
				}

				if(this.ID == mdata.getDest()){

					if (!this.son.contains((Object) inbox.getSender().ID))
						son.add(inbox.getSender().ID);

					if ((this.myRole != Roles.SINK) ){

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
							MessageSPTTimer msgTimer = new MessageSPTTimer(mdata,Tools.getNodeByID(this.NextHopSink));
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

							MessageSPTTimer msgTimer = new MessageSPTTimer(mdata,Tools.getNodeByID(this.NextHopSink));
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

						}
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
						Tools.appendToOutput("handleMessages(): SPTDataMessage arrived to the SINK, Receivers: " +this.Receivers +"\n");
					}

				}
			}
		}
	}




	@Override
	public void handleNAckMessages(NackBox nackBox) {
		while(nackBox.hasNext()) {
			Message msg = nackBox.next();
			if(msg instanceof SPTDataMessage){
				SPTDataMessage Data = (SPTDataMessage) msg;

				if(Data.getAggPacket() > 1){
					perdidosAGG = perdidosAGG + Data.getAggPacket();
				}else{
					perdidos = perdidos+1;
				}

			}

		}

	}


	public void aproximationtree(){
		MessageSPTTimer msgtree = new MessageSPTTimer (new MSGTREE(1,this.NextHopSink,this.ID));
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
				EventSPTTimer t = new EventSPTTimer(i);
				t.startEventAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventStart"+i), this, i);
				if (i==1 && this.ID==1){
					GraphConnectivityTimer gct = new GraphConnectivityTimer(this);
					gct.startAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventStart"+i) - 0.1, this);
				}
			}
			
			double maxEndingTime = 0;
			for (int i = 1; i <= sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents"); i++) {
				EventEndSPTTimer t = new EventEndSPTTimer(i,this);
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
			this.setColor(Color.RED);
			this.myRole = Roles.SINK;
			this.mystatus = Status.READY;
			sendMCI();
		}

		//Event timers
		EndSPTTimer etimer = new EndSPTTimer();
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
		Tools.appendToOutput("HopToSink: " + this.HopToSink + "\n" +
				"NextHopSink: " + this.NextHopSink + "\n" +
				"OwnerID:" + this.ownerID +"\n"
				);
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
	 * the SPTTimer is scheduled to start in a time DataRate+10. 
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
		timerTREE = new SPTTimer(this,TNO.MONITORING);
		timerTREE.tnoStartRelative(DataRate+10, this, TNO.MONITORING);
	}

	//Node Sink start the Flooding of MCI
	public void sendMCI(){
		this.HopToSink = 0; 	// I am Sink
		this.NextHopSink = this.ID;	// I am SInk
		MessageSPTTimer mcimsg = new MessageSPTTimer (new MCIMessage(this.HopToSink+1,this.ID));
		mcimsg.startRelative(0.0001, this);
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
			Message mdata = new SPTDataMessage(this.ID,this.NextHopSink,"Sink",this.HopToSink,1,100,eventnum);
			MessageSPTTimer msgTimer = new MessageSPTTimer(mdata,Tools.getNodeByID(this.NextHopSink));
			time += (double)DataRate;
			msgTimer.startRelative(DataRate,this);
			//			sptLog.logln("Sdata t "+ (Global.currentTime+ time)
			//					+ " Ns "+ this.ID 
			//					+ " Ap " + 1
			//					+ " Nd " + 1);
			//Tools.appendToOutput("SData :"+ this.ID +"\n");
			Tools.appendToOutput("sendData(): Sending SPTDataMessage from: "+ this.ID + " to " + this.NextHopSink + "distance (in hops): "+this.HopToSink + "\n");
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
			sendMCI(); // Make one Flooding of MCI (Message of Configuration Initial)
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
			if (min>((SPTNode)n).getBateria().getEnergy() && this.ID!=1){
				min = ((SPTNode)n).getBateria().getEnergy();
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
			/*if (((SPTNode)n).getNextHopSink()==100000){
				return false;
			}*/
		}
		return true;
	}

}