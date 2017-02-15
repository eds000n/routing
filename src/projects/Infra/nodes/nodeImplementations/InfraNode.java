/**InfraEventAlertMessage
 * 
 */
package projects.Infra.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.lang.Math;

import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;
import projects.Infra.nodes.messages.InfraAnnounceCHMessage;
import projects.Infra.nodes.messages.InfraDataMessage;
import projects.Infra.nodes.messages.InfraEventAlertMessage;
import projects.Infra.nodes.messages.InfraFindRelay;
import projects.Infra.nodes.messages.InfraRelayResponde;
import projects.Infra.nodes.messages.InfraRoleMigrationMessage;
import projects.Infra.nodes.messages.InfraSKAnnounceMessage;
import projects.Infra.nodes.messages.InfraWantRelay;
import projects.Infra.nodes.timers.EndTimer;
import projects.Infra.nodes.timers.EventEndInfraTimer;
import projects.Infra.nodes.timers.EventTimer;
import projects.Infra.nodes.timers.InfraTimer;
import projects.Infra.nodes.timers.MessageTimer;
//import projects.PEAR.nodes.messages.MCIMessage;
//import projects.PEAR.nodes.messages.MSGTREE;

import projects.Infra.nodes.timers.MessageLeoTimer;
import projects.SPT.nodes.messages.MSGTREE;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox; 
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.UniformDistribution;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.models.EnergyModel.IEnergy;
import sinalgo.models.EnergyModel.simple.SimpleEnergy;


/** 
 * @author Heitor, Leandro e Guilherme 
 * Infra node implementation
 * Infra by Nakamura
 */
 
public class InfraNode extends Node {
	public class RoutingTableEntry{
		public int nexthop;
		public int hops;
		public RoutingTableEntry(int nexthop, int hops) {
			this.nexthop = nexthop;
			this.hops = hops;
		}
		
	}

	private IEnergy bateria;

	private ArrayList<Integer> chcandidates = new ArrayList<Integer>(); //CH Candidates List
	private enum Status {MONITORING, ANNOUNCING, CLUSTERING1, CLUSTERING2, READY};
	private enum Roles { SINK, COLLABORATOR, COLLABORATORAGG, COORDINATOR, RELAY };
	public enum TNO {VICINITY, CLUSTERING1, CLUSTERING2, SENDDATA, AGGREGATION, MONITORING, APPROXIMATION };
	private Roles myRole; 
	private ArrayList<Integer> ids = new ArrayList<Integer>(); 
	private Status myStatus;
	private InfraTimer timerVicinity, timerAPPROXIMATION;  
	private int ownerID=this.ID;
	private int ownerId_HopstoSink = 10000;
//	private boolean sentData=false;
	private HashMap<Integer, RoutingTableEntry> routingtable = new HashMap<Integer, RoutingTableEntry>();
	private int myeventID=-1;
	private int minoraggdist;
	private int aggPackets=0;
	private int msgAggPckts = 0;
	
	private boolean senddata =false;
	private boolean cluster_candidate = false;
	private boolean sendapproximation =false;
	private boolean sendapproximation2 =false;
	private int Disttree = 0;
	private int Disttreerecv =0;
	private ArrayList<Integer> son = new ArrayList<Integer>();
//	private ArrayList<InfraDataMessage> dataMessages = new ArrayList<InfraDataMessage>();
	private UniformDistribution uniformRandom =  new UniformDistribution(0,0.016);
	private ArrayList<Integer> lastFloodedCHs = new ArrayList<Integer>();
	private ArrayList<Integer> lastFloodedSKs= new ArrayList<Integer>();
	private boolean timerAggOn=false;
	private boolean skFlood = false;
	private boolean rota = false;
	private double timeapproximaiton = 0;
	//Logging infraLog;
	public static Logging Infra = Logging.getLogger("InfraLog.txt", true);
	public static Logging EnergyNetwork = Logging.getLogger("InFRAEnergy.txt", true);
	
	private int HoptoCoordinator = 0;
	private double nextskFlood=0;
	private double eventEndTime;
	private ArrayList<Integer> filhossend = new ArrayList<Integer>();
	private ArrayList<Integer> filhosrecv = new ArrayList<Integer>();
	private double nextsenddata = 0;
	private double erro =0;
	private static int perdidos = 0;
	private static int enviados =0;
	private static int enviadosAgg =0;
	private static int perdidosAGG = 0;
	private static int packetrecvagg = 0;
	public static int	probSend;	
	public static int EventsAmount = 0;
	public static int EventsTimes = 0;
	public static int EventSize=0;
	public static int CommunicationRadius = 0;
	public static int DropRate = 0;
	public static int Recivers =0;
	public static int Density =0;
	public static int Overheads =0;
	public static int DataPackets =0;
	public static int Edges =0;
	public static int WorstCase =0;
	public static double DataRate =0;
	public static int Notifications =0;
	
	public static double SimulationTime =0;
	
	
	private static int Event1 = 0;
	private static int Event2 = 0;
	private static int Event3 = 0;
	private static int Event4 = 0;
	private static int Event5 = 0;
	private static int Event6 = 0;
	
	public static int Detects = 0;
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#checkRequirements()
	 */
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#handleMessages(sinalgo.nodes.messages.Inbox)
	 */
	@Override
	public void handleMessages(Inbox inbox) {
		// TODO Auto-generated method stub
		int sender;
		//Spent energy due to the listening mode
		this.bateria.spend(EnergyMode.LISTEN,1);
		
		while(inbox.hasNext()) {
			Message msg = inbox.next();
			sender = inbox.getSender().ID;
			//Message processing
			this.bateria.spend(EnergyMode.RECEIVE,1);
		
			
			// Event Alert Handler
			if (msg instanceof InfraEventAlertMessage
					&& this.myeventID == ((InfraEventAlertMessage)msg).getEventID()	
					){
				this.ElectionLeader(msg);
			}
			
			//CH FLood Handler
			if (msg instanceof InfraAnnounceCHMessage){
				this.chFloodHandler(msg,sender);
			}
			//Sink Flood Handler
			if (msg instanceof InfraSKAnnounceMessage){
				this.skFloodHandler(msg,sender);
			}	
			//Data message Handler
			if (msg instanceof InfraDataMessage){
				this.dataMessageHandler(msg,sender);
			}
			//Role Migration Announcement Handler
			if (msg instanceof InfraRoleMigrationMessage 
					&& this.myeventID == ((InfraRoleMigrationMessage)msg).getEventID()){
				this.roleMigrationHandler(msg);
			}
			
			
			if (msg instanceof MSGTREE){
				MSGTREE msgtree = (MSGTREE) msg;
				
				
				if(this.ID == msgtree.getNexthop()){
					this.filhosrecv.add(msgtree.getSentnode());
							
					if(this.myRole != Roles.SINK)
					{
						this.setColor(Color.yellow);
						this.Disttree = this.Disttree + msgtree.getDisttree();
						this.Disttreerecv = this.Disttreerecv +1;
						
						if(this.son.size() == 1){
							if(this.myRole == Roles.COLLABORATOR){
								this.filhossend.add(this.routingtable.get(this.ownerID).nexthop);
								broadcast(new MSGTREE(this.Disttree, this.routingtable.get(this.ownerID).nexthop,this.ID));
								this.setColor(Color.red);
								
							}else if(this.myRole == Roles.RELAY || this.myRole == Roles.COORDINATOR){
									this.filhossend.add(this.routingtable.get(1).nexthop);
									broadcast(new MSGTREE(this.Disttree+1, this.routingtable.get(1).nexthop,this.ID));	
									this.setColor(Color.red);
									this.Edges = this.Edges+1;
								}
							} else if(this.son.size()==this.Disttreerecv){
								if(this.myRole == Roles.COLLABORATOR){
									this.filhossend.add(this.routingtable.get(this.ownerID).nexthop);
									broadcast(new MSGTREE(this.Disttree, this.routingtable.get(this.ownerID).nexthop,this.ID));	
									this.setColor(Color.red);
								}else
									if(this.myRole == Roles.RELAY || this.myRole == Roles.COORDINATOR){
										this.filhossend.add(this.routingtable.get(1).nexthop);
										broadcast(new MSGTREE(this.Disttree+1, this.routingtable.get(1).nexthop,this.ID));
										this.setColor(Color.red);
									}
						}
					
					}else{
						this.Disttree = this.Disttree + msgtree.getDisttree();
						this.Disttreerecv = this.Disttreerecv +1;
						Tools.appendToOutput("filhos: "+this.son.size()+"  rec: "+ this.Disttreerecv);
						this.setColor(Color.black);
						if(this.son.size() == 1){
							//Edges = this.Disttree;
							this.setColor(Color.pink);
						}else{
							if(this.son.size()==this.Disttreerecv){
								//Edges = this.Disttree;
								this.setColor(Color.pink);
							}
						}
						
					}
				}	
			}
		
			
		
			//Nó Relay precisa colocar como nexthop outro relay pq tem um colaborador como nexthop
			if(msg instanceof InfraFindRelay){
				if (this.ID == ((InfraFindRelay)msg).getSource() ){
					MessageTimer mwrTimer = new MessageTimer((new InfraWantRelay(this.ID,this.routingtable.get(1).hops)));
					double time = new UniformDistribution(0,.005).nextSample();
					mwrTimer.startRelative(time,this);
					this.setColor(Color.magenta);
//					infraLog.logln("Flooding t " + (Global.currentTime + time)
//							+ " Ns "+ this.ID 
//							+ " Pi " + msg.hashCode() 
//							+ " Pt " +"wsn-infra-ch");
					Overheads = Overheads + 1;
					this.bateria.spend(EnergyMode.SEND,1);
				}
			}
			if(msg instanceof InfraWantRelay){
				if (  (this.myRole == Roles.RELAY) && ( this.routingtable.get(1).hops < ((InfraWantRelay)msg).getHopToSink() )){
					MessageTimer mrrTimer = new MessageTimer((new InfraRelayResponde(this.ID,((InfraWantRelay)msg).getSource(),this.routingtable.get(1).hops)));
					double time = new UniformDistribution(0,.005).nextSample();
					mrrTimer.startRelative(time,this);
					this.setColor(Color.red);
//					infraLog.logln("Flooding t " + (Global.currentTime + time)
//							+ " Ns "+ this.ID 
//							+ " Pi " + msg.hashCode() 
//							+ " Pt " +"wsn-infra-ch");
					Overheads = Overheads + 1;
					this.bateria.spend(EnergyMode.SEND,1);
				}
			}
			if(msg instanceof InfraRelayResponde){
				if (  (this.ID == ((InfraRelayResponde)msg).getSource())){
					this.routingtable.get(1).nexthop = ((InfraRelayResponde)msg).getID();
					this.setColor(Color.black);
				}
			}
		}		
	}
	public void ElectionLeader(Message msg){
		//Tools.appendToOutput("ID Role: "+this.ID+".."+this.myRole.toString()+"\n");
		if((this.myRole == Roles.COORDINATOR) )
		{
			Tools.appendToOutput("COORD: "+this.ID+"\n");
			if ( (this.routingtable.get(1).hops > ((InfraEventAlertMessage)msg).getOwnerId_HopstoSink() ) ||
						((this.routingtable.get(1).hops == ((InfraEventAlertMessage)msg).getOwnerId_HopstoSink()) && (this.ID> ((InfraEventAlertMessage)msg).getCandidateId())))
				{
					Tools.appendToOutput("COORD-to-COLL: "+this.ID+"\n");
					this.myRole = Roles.COLLABORATOR;
					this.routingtable.remove(this.ownerID);
					this.setColor(Color.pink);
					this.ownerID = ((InfraEventAlertMessage)msg).getCandidateId();
					
					Tools.appendToOutput("ID OWNER: "+this.ID+" "+this.ownerID+"\n");
					//routingtable.put(this.ownerID, new RoutingTableEntry(this.ownerID,((InfraEventAlertMessage)msg).getOwnerId_HopstoSink()));
					this.ownerId_HopstoSink = ((InfraEventAlertMessage)msg).getOwnerId_HopstoSink();
					this.routingtable.put(ownerID,new RoutingTableEntry(((InfraEventAlertMessage)msg).getMyID(),((InfraEventAlertMessage)msg).getHopstoCoordinator()));
					this.HoptoCoordinator = ((InfraEventAlertMessage)msg).getHopstoCoordinator();
					this.cluster_candidate = false;
					MessageLeoTimer annmsg = new MessageLeoTimer(new InfraEventAlertMessage(this.ID, this.routingtable.get(1).hops,this.myeventID,this.HoptoCoordinator+1, this.ID, ((InfraEventAlertMessage)msg).getCandidateId(),((InfraEventAlertMessage)msg).getOwnerId_HopstoSink()));
					double time = new UniformDistribution(0,0.05).nextSample();
					annmsg.startRelative(0.1+(this.ownerId_HopstoSink/100), this);
					Overheads = Overheads + 1;
					this.bateria.spend(EnergyMode.SEND,1);
				
					
				}
		} else if (this.myRole == Roles.COLLABORATOR)
		{
			if ( ((this.ownerId_HopstoSink > ((InfraEventAlertMessage)msg).getOwnerId_HopstoSink()) 
					||	( (this.ownerId_HopstoSink == ((InfraEventAlertMessage)msg).getOwnerId_HopstoSink()) && (this.ownerID > ((InfraEventAlertMessage)msg).getCandidateId()))))
					{
						Tools.appendToOutput("COLL-to-COLL: "+this.ID+"\n");
				
							this.routingtable.remove(this.ownerID);
							this.ownerID = ((InfraEventAlertMessage)msg).getCandidateId();
							Tools.appendToOutput("ID OWNER: "+this.ID+" "+this.ownerID+"\n");
							this.routingtable.put(ownerID,new RoutingTableEntry(((InfraEventAlertMessage)msg).getMyID(),((InfraEventAlertMessage)msg).getHopstoCoordinator()));
							this.ownerId_HopstoSink = ((InfraEventAlertMessage)msg).getOwnerId_HopstoSink();
							//Tools.appendToOutput("Id M: "+((InfraEventAlertMessage)msg).getMyID()+"\n");
							//this.routingtable.get(ownerID).nexthop = ((InfraEventAlertMessage)msg).getMyID();
							this.HoptoCoordinator = ((InfraEventAlertMessage)msg).getHopstoCoordinator();
							this.setColor(Color.pink);
							MessageLeoTimer annmsg = new MessageLeoTimer(new InfraEventAlertMessage(this.ID, this.routingtable.get(1).hops,this.myeventID,this.HoptoCoordinator+1, this.ID, ((InfraEventAlertMessage)msg).getCandidateId(),((InfraEventAlertMessage)msg).getOwnerId_HopstoSink()));
							double time = new UniformDistribution(0,0.05).nextSample();
							annmsg.startRelative(0.1+(this.ownerId_HopstoSink/100), this);
							Overheads = Overheads + 1;
						
							this.bateria.spend(EnergyMode.SEND,1);
						
				
						}	
		}	
	}
	
	
	public void Approximation(){
		Tools.appendToOutput("Nó: "+this.ID+" APPROXIMATION\n");
		if(!this.sendapproximation){
//			if(this.myRole == Roles.COLLABORATOR || this.myRole == Roles.COLLABORATORAGG){
//				this.filhossend.add(this.routingtable.get(this.ownerID).nexthop);
//				broadcast(new MSGTREE(this.Disttree+1, this.routingtable.get(this.ownerID).nexthop,this.ID));
//				this.setColor(Color.pink);
//			}else{
				if(this.myRole == Roles.RELAY || this.myRole == Roles.COORDINATOR){
					this.filhossend.add(this.routingtable.get(1).nexthop);
					broadcast(new MSGTREE(this.Disttree+1, this.routingtable.get(1).nexthop,this.ID));	
					this.setColor(Color.red);
				}
			}
		//}
	}
	public void chFloodHandler(Message msg, int sender){
		InfraAnnounceCHMessage annchmsg = (InfraAnnounceCHMessage)msg;
		boolean maiorEvent=true;
		if(this.myRole == Roles.RELAY)
		this.setColor(Color.magenta);
		
		if(routingtable.containsKey(annchmsg.getchID()))
			maiorEvent = (routingtable.get(annchmsg.getchID()).hops)>annchmsg.getHopstoEvent();
			
		//if(maiorEvent && (this.myRole==Roles.RELAY || this.myRole==Roles.SINK))
		//		routingtable.put(annchmsg.getchID(),new RoutingTableEntry(sender,annchmsg.getHopstoEvent()));
			
		//if((this.myRole==Roles.RELAY && maiorEvent )){
		//	routingtable.put(annchmsg.getchID(),new RoutingTableEntry(sender,annchmsg.getHopstoEvent()));
		
			if(maiorEvent){
			routingtable.put(annchmsg.getchID(),new RoutingTableEntry(sender,annchmsg.getHopstoEvent()));
			annchmsg.setHopstoEvent(annchmsg.getHopstoEvent()+1);
			if (routingtable.containsKey(1))
				annchmsg.setHopstoSink(routingtable.get(1).hops);
			else 
				annchmsg.setHopstoSink(1000);
			
			annchmsg.setMyRole(this.myRole.toString());
			MessageTimer relayCH = new MessageTimer(annchmsg);
			relayCH.startRelative((annchmsg.getHopstoEvent())+1/20,this);
			Overheads = Overheads + 1;
			this.bateria.spend(EnergyMode.SEND,1);
		
		}
		
		//Atualizar a rota aqui mesmo (desse jeito só precisa de um flood do sink)
		if((this.myRole == Roles.RELAY)){
			 if((routingtable.get(1).hops-1)==annchmsg.getHopstoSink()){
					if (annchmsg.getHopstoEvent()<minoraggdist){
						minoraggdist = annchmsg.getHopstoEvent();
						routingtable.put(1,new RoutingTableEntry(sender,annchmsg.getHopstoSink()));
						
					}
			}
		}
		if(!(routingtable.containsKey(annchmsg.getchID())) && myRole == Roles.SINK){
            routingtable.put(annchmsg.getchID(),new RoutingTableEntry(sender,annchmsg.getHopstoEvent()));
//            if(Global.currentTime > this.nextskFlood)
//                this.skFlood =false;
//            if(!skFlood){
//                skFlood = true;
//                announceSK(annchmsg.getEventID());
////                this.nextskFlood = Global.currentTime +30;
         //   }
        }
		
	}
		
	public void aproximationtree(){
			if(this.myRole == Roles.COLLABORATOR){
		   	MessageTimer msgtree = new MessageTimer (new MSGTREE(0,this.routingtable.get(ownerID).nexthop,this.ID));
	    	this.setColor(Color.WHITE);
	    	msgtree.startRelative(0.01, this);
			}
			
			if(this.myRole == Roles.COORDINATOR){
			   	MessageTimer msgtree = new MessageTimer (new MSGTREE(1,this.routingtable.get(1).nexthop,this.ID));
		    	this.setColor(Color.WHITE);
		    	msgtree.startRelative(0.01, this);
				}
	    
//	    else{
//	    	MessageTimer msgtree = new MessageTimer (new MSGTREE(1,this.routingtable.get(1).nexthop,this.ID));
//	    	double time = uniformRandom.nextSample();
//	    	msgtree.startRelative(time, this);
//	    }
	    
	}

	public void skFloodHandler(Message msg, int sender){
		InfraSKAnnounceMessage annSKmsg = (InfraSKAnnounceMessage)msg;
		boolean maior = true;
		this.myStatus = Status.READY;
		
		if(routingtable.containsKey(annSKmsg.getSkID()))
				maior = (routingtable.get(annSKmsg.getSkID()).hops)>annSKmsg.getHops();
		if(maior){
			routingtable.put(annSKmsg.getSkID(),new RoutingTableEntry(sender,annSKmsg.getHops()));
			//minoraggdist = annSKmsg.getAggdistco();
		}
		if(!this.lastFloodedSKs.contains(annSKmsg.getEventID())){ // versao mais barata que nao garante a melhor arvore
			if (this.ID == 1)
				return;
			annSKmsg.setHops(annSKmsg.getHops()+1);
			MessageTimer relaySK = new MessageTimer(annSKmsg);
			annSKmsg.setAggdistco(evalAdistCo());
			relaySK.startRelative(this.routingtable.get(1).hops+1,this);
			this.lastFloodedSKs.add(annSKmsg.getEventID());
			Overheads = Overheads + 1;
			this.setColor(Color.green);
			//Spent energy due to the transmission mode
			this.bateria.spend(EnergyMode.SEND,1);
		}
		
		
	}
	public void dataMessageHandler(Message msg, int sender){
		// Role Migration
		InfraDataMessage dataMsg = ((InfraDataMessage)msg);
		
		if(this.ID == dataMsg.getRelay() ){
			if (!this.son.contains((Object)sender)){
				this.son.add(sender);
			}
			
			if ((this.myRole == Roles.COLLABORATOR)){
				this.setColor(Color.ORANGE);	
				dataMsg.setDest(this.routingtable.get(this.ownerID).nexthop);
				dataMsg.setSource(this.ID);
				dataMsg.setPayload("COLLABORATOR");
				MessageLeoTimer msgTimer = new MessageLeoTimer(dataMsg,Tools.getNodeByID(this.routingtable.get(this.ownerID).nexthop));
		    	msgTimer.startRelative(0.0000001,this);
				DataPackets = DataPackets + 1;
				this.bateria.spend(EnergyMode.SEND,1);
		   		this.rota = true;
				return;
			}
	
	
			// Relay Message
			if( (this.myRole == Roles.RELAY) && ( (dataMsg.getPayload() != "COLLABORATOR")))
			{
				if(Global.currentTime > this.nextsenddata)
					this.senddata =true;
						
				//Collect data to aggregation/fusion/reduction/coding here
				this.aggPackets = this.aggPackets + dataMsg.getAggPackets();
				//msgAggPckts += dataMsg.getAggPackets();
								
				if ( ( (this.son.size()<2) && (this.myRole == Roles.RELAY)) )
				{
					dataMsg.setSource(this.ID);
					dataMsg.setRelay(routingtable.get(1).nexthop);
					dataMsg.setDest(1);
					dataMsg.setEventID(this.myeventID);
					dataMsg.setMyRole(this.myRole.toString());
					MessageTimer msgTimer = new MessageTimer(dataMsg, Tools.getNodeByID(routingtable.get(1).nexthop));
					msgTimer.startRelative(0.0001, this);
					DataPackets = DataPackets + 1;
					this.bateria.spend(EnergyMode.SEND,1);
			
					this.setColor(Color.green);
					this.aggPackets = 0;
				}else if ((this.senddata) && (this.myRole == Roles.RELAY))
				{
					dataMsg.setSource(this.ID);
					dataMsg.setRelay(routingtable.get(1).nexthop);
					dataMsg.setDest(1);
					dataMsg.setEventID(this.myeventID);
					dataMsg.setMyRole(this.myRole.toString());
				    dataMsg.setAggPackets(this.aggPackets);
					nextsenddata = Global.currentTime + DataRate;
					MessageTimer msgTimer = new MessageTimer(dataMsg, Tools.getNodeByID(routingtable.get(1).nexthop));
					msgTimer.startRelative(0.0001, this);
					DataPackets = DataPackets + 1;
					this.bateria.spend(EnergyMode.SEND,1);
					this.rota = true;
					this.senddata = false;
					this.aggPackets = 0;
					this.setColor(Color.magenta);
				}
			}
			
			//Se eu sou um colaborador e recebi dados de um relay eu pesso apra ele procurar outro nexthop
			if ( (this.myRole == Roles.COLLABORATOR) && (dataMsg.getMyRole() == Roles.RELAY.toString()) &&
				(this.ID == dataMsg.getRelay()) && (this.myeventID == dataMsg.getEventID()))
			{
				MessageTimer findrelay = new MessageTimer((new InfraFindRelay(dataMsg.getSource())));
				double time = new UniformDistribution(0,.001).nextSample();
				findrelay.startRelative(time,this);
				this.setColor(Color.BLACK);
				Overheads = Overheads + 1;
				this.bateria.spend(EnergyMode.SEND,1);
			}
			
			//Sink Data Message Handler
			if (myRole == Roles.SINK && this.ID == dataMsg.getDest())
			{
				Recivers = Recivers + 1;//Recebiçoes e não qtd de dados recebidos
				packetrecvagg = packetrecvagg + dataMsg.getAggPackets();
				this.bateria.spend(EnergyMode.RECEIVE,1);
			}			
		}
	}
	
	
    @Override
	public void handleNAckMessages(NackBox nackBox) {
		while(nackBox.hasNext()) {
			Message msg = nackBox.next();
			if(msg instanceof InfraDataMessage){
				InfraDataMessage Data = (InfraDataMessage)msg;
				if(Data.getMyRole() == Roles.RELAY.toString()){
					perdidosAGG = perdidosAGG+ Data.getAggPackets();
				}else{
					perdidos = perdidos + 1;
				}
			}
		}
    }
	
    
       
	public void roleMigrationHandler(Message msg){
		if (myRole == Roles.COLLABORATOR || myRole == Roles.COORDINATOR){
			routingtable.remove(ownerID);
			ownerID = ((InfraRoleMigrationMessage)msg).getNewCH();
			routingtable.put(ownerID, new RoutingTableEntry(ownerID,1));
			if (myRole == Roles.COORDINATOR && ownerID != this.ID){
				myRole = Roles.COLLABORATORAGG;
//				timerVicinity.tnoStartRelative( 0.1*dataRate / (routingtable.get(1).hops + 1), this, TNO.AGGREGATION);
				this.setColor(Color.BLUE);
			}
		}
	}
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#init()
	 */
	@Override
	public void init() {
		timerVicinity = new InfraTimer(this,TNO.VICINITY);
		myRole = Roles.RELAY;
		myStatus = Status.MONITORING;
		try {
			SimulationTime = sinalgo.configuration.Configuration.getDoubleParameter("SimTime");
			EventsAmount = sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents");
			EventsTimes = sinalgo.configuration.Configuration.getIntegerParameter("Event/Time");
			Density = sinalgo.configuration.Configuration.getIntegerParameter("Density");
			probSend = sinalgo.configuration.Configuration.getIntegerParameter("ProbSend");
			
			for (int i = 1; i <= EventsAmount; i++) {
				EventTimer t = new EventTimer();
				t.startEventAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventStart"+i), this, i);	
			}
			
			for (int i = 1; i <= sinalgo.configuration.Configuration.getIntegerParameter("Event/NumEvents"); i++) {
				EventEndInfraTimer t = new EventEndInfraTimer(i,this);
				t.startAbsolute(sinalgo.configuration.Configuration.getDoubleParameter("Event/EventEnd"+i), this);
			}
			eventEndTime = sinalgo.configuration.Configuration.getDoubleParameter("Event/EventEnd");
			DataRate = sinalgo.configuration.Configuration.getDoubleParameter("Event/DataRate");
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
				bateria = new SimpleEnergy(this.ID);
			}
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Energy Model not found");
			e.printStackTrace();
		}
		
		if (this.ID==1){
			this.myRole = Roles.SINK;
			this.myStatus = Status.READY;
			routingtable.put(1,new RoutingTableEntry(1,0));
			announceSK(this.ID);
			
		}
		
		//Event timers
		EndTimer etimer = new EndTimer();
		etimer.startAbsolute(SimulationTime, this);
		
	}
 
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#neighborhoodChange()
	 */
	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
	
	}
	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#postStep()
	 */
	@Override
	public void postStep() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see sinalgo.nodes.Node#preStep()
	 */
	@Override
	public void preStep() {
		// TODO Auto-generated method stub

	}
	

	public IEnergy getBateria() {
		return bateria;
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		// TODO Auto-generated method stub
//		super.draw(g, pt, highlight);
		if (this.ID == 1) highlight = true;
		super.drawNodeAsDiskWithText(g, pt, highlight, Integer.toString(this.ID), 8, Color.WHITE);
	}
/*
 * Our methods
 */		
	public void startDetection() {
		myStatus = Status.ANNOUNCING;
		myRole = Roles.COORDINATOR;
		this.setColor(Color.ORANGE);
		Detects = Detects + 1;
		timerVicinity.tnoStartRelative(uniformRandom.nextSample(), this, TNO.VICINITY);
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
			this.myeventID = eventID;
		
			if (this.myeventID == 1)
				Event1 = Event1 + 1;
			if (this.myeventID == 2)
				Event2 = Event2 + 1;
			if (this.myeventID == 3)
				Event3 = Event3 + 1;
			if (this.myeventID == 4)
				Event4 = Event4 + 1;
			if (this.myeventID == 5)
				Event5 = Event5 + 1;
			if (this.myeventID == 6)
				Event6 = Event6 + 1;
			return true;
		}
		else 
			return false;
		
	}

	public void sendData(){
		if (Global.currentTime > eventEndTime){
			if ( ((this.son.size() == 0) && (this.myRole == Roles.COORDINATOR)) || ((this.son.size() == 0) && (this.myRole == Roles.COLLABORATOR)) ){
				if((this.myRole == Roles.COORDINATOR) && (this.filhossend.size()==0))
					this.Edges=Edges+1;
				
				aproximationtree();
				if(WorstCase< (this.routingtable.get(1).hops)){
					WorstCase = (this.routingtable.get(1).hops);
				}
			}
			return;
		}
		
		myStatus	= Status.MONITORING;
		int myMsgAgg = msgAggPckts;
		if( (this.senddata) && (this.myRole  ==  Roles.COORDINATOR))
		{
				if (this.myeventID == 1)
					myMsgAgg = (int) Math.round(Event1 - (erro * Event1));
				if (this.myeventID == 2)
					myMsgAgg = (int) Math.round(Event2 - (erro * Event2));
				if (this.myeventID == 3)
					myMsgAgg = (int) Math.round(Event3 - (erro * Event3));
				if (this.myeventID == 4)
					myMsgAgg = (int) Math.round(Event4 - (erro * Event4));
				if (this.myeventID == 5)
					myMsgAgg = (int) Math.round(Event5 - (erro * Event5));
				if (this.myeventID == 6)
					myMsgAgg = (int) Math.round(Event6 - (erro * Event6));
			
			
				enviadosAgg = enviadosAgg + myMsgAgg;
				Message m = new InfraDataMessage(this.ID,1,routingtable.get(1).nexthop,this.myeventID,myMsgAgg,"COORDINATOR",this.myRole.toString());
				MessageTimer msgTimer = new MessageTimer(m,Tools.getNodeByID(routingtable.get(1).nexthop));
				nextsenddata = Global.currentTime + (DataRate);
				msgTimer.startRelative(DataRate,this);
				DataPackets = DataPackets +1;
				Notifications = Notifications + 1;
				this.bateria.spend(EnergyMode.SEND,1);
				this.senddata = false;
				this.rota = true;
				timerVicinity.tnoStartRelative(DataRate, this, TNO.MONITORING);
		
		}else if( (this.senddata) && ((this.myRole == Roles.COLLABORATOR)))
		{
				Message m = new InfraDataMessage(this.ID,this.ownerID,routingtable.get(this.ownerID).nexthop,this.myeventID,1,"COLLABORATOR",this.myRole.toString());
			    MessageTimer msgTimer = new MessageTimer(m,Tools.getNodeByID(routingtable.get(ownerID).nexthop));
				msgTimer.startRelative(DataRate,this);
				enviados = enviados + 1;
				nextsenddata = Global.currentTime + (DataRate);
				DataPackets = DataPackets +1;
				Notifications = Notifications + 1;
				this.bateria.spend(EnergyMode.SEND,1);
				this.rota = true;
				this.senddata = false;
				timerVicinity.tnoStartRelative(DataRate, this, TNO.MONITORING);
		}
	}
	
	
	public void defineRole(){
		if(myStatus == Status.CLUSTERING1){
			myRole = Roles.COLLABORATOR;
			boolean ch = false;
			for(Integer id : this.ids){
				if(this.ID>id){
					ch = false;
					break;
				}else{ 
					ch = true;
				}
			}
			if(ch){
				myRole = Roles.COORDINATOR;
				this.setColor(Color.cyan);
			}
		}
	}
	public void refineRole(){
		boolean win = false;
		if (!(myRole == Roles.COORDINATOR)) return;
		for(Integer id: chcandidates){
			if(this.ID>id){
				myRole = Roles.COLLABORATOR;
				ownerID = Math.min(ownerID, id);
				win = false;
			}else{
				win = true;
			}
				
		}
		if (win){
//			routingtable.clear();
			routingtable.put(this.ID,new RoutingTableEntry(this.ID,0));
		}
	}
	public void announceCH(int eventID){
		MessageTimer annchmsg;
		if (routingtable.containsKey(1))
			annchmsg = new MessageTimer(new InfraAnnounceCHMessage(this.ID,eventID, 1,1,this.routingtable.get(1).hops, this.myRole.toString()));
		else
			annchmsg = new MessageTimer(new InfraAnnounceCHMessage(this.ID,eventID, 1,1, 1000, this.myRole.toString()));
		double time = uniformRandom.nextSample();
		annchmsg.startRelative(time,this);
		Overheads = Overheads + 1;
		this.bateria.spend(EnergyMode.SEND,1);
	}
	public void announceSK(int eventID){
		MessageTimer annSKmsg = new MessageTimer(new InfraSKAnnounceMessage(this.ID,1,evalAdistCo(),eventID));
		double time = uniformRandom.nextSample();
		annSKmsg.startRelative(time,this);
		Overheads = Overheads + 1;
		//Spent energy due to the transmission mode
		this.bateria.spend(EnergyMode.SEND,1);
	}
	public int evalAdistCo(){
		int soma=0;
		Set<Integer> keyset = routingtable.keySet();
		int next;
		Iterator<Integer> it = keyset.iterator();
		while(it.hasNext()){
			next = it.next();
			if (next !=1){
				soma += routingtable.get(next).hops;
			}
		}
		return soma;
	}
	/*
	 * timerVicinity timeout handler
	 */
	public void printDegree(){
		try{
//			infraLog.logln(
//					"Degree "+
//					"Node "+
//					this.ID + " " + 
//					this.outgoingConnections.size()
//					
//			);
			if (myRole == Roles.COORDINATOR){
//				infraLog.logln(
//						"SkDist "+
//						"Node "+
//						this.ID + " " + 
//						routingtable.get(1).hops
//						
//				);
//				infraLog.logln(
//						"Seed "+
//						sinalgo.tools.statistics.Distribution.getSeed()
//						
//				);
			}
			if (myRole == Roles.COLLABORATOR|| myRole == Roles.COLLABORATORAGG){
//					infraLog.logln(
//							"SkDist "+
//							"Node "+
//							this.ID + " " + 
//							routingtable.get(1).hops
//							
//					);
			}
			
			if (myRole == Roles.SINK){
//				infraLog.logln("Perdidos " + perdidos);
//				infraLog.logln("Agregados Perdidos " + perdidosAGG);
//				infraLog.logln("Enviados " + enviados);
//				infraLog.logln("Agregados Enviados " + enviadosAgg);
//				infraLog.logln("Recebidos "+ packetrecvagg);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void printTree(){
		try{
			if (myRole == Roles.COLLABORATOR || myRole == Roles.COLLABORATORAGG)
//				infraLog.logln("Tree "+ Global.currentTime +
//						" Nd " + this.ID +
//						" Father "+ routingtable.get(this.ownerID).nexthop
//						);
			if (myRole == Roles.COORDINATOR){
//				infraLog.logln("Tree "+ Global.currentTime +
//						" Nd " + this.ID +
//						" Father "+ routingtable.get(1).nexthop
//						);
			}
			if (myRole == Roles.RELAY && this.getColor() == Color.YELLOW){
//				infraLog.logln("Tree "+ Global.currentTime +
//						" Nd " + this.ID +
//						" Father "+ routingtable.get(1).nexthop
//						);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void Monitoring(){
		if(myRole == Roles.COLLABORATOR || myRole == Roles.COORDINATOR){
			this.rota = true; 
			this.senddata =true; 
			sendData();
		}
	}
	
	
	
	public void timeout(TNO tno){
		switch(tno){
		case VICINITY:
			this.myRole = Roles.COORDINATOR;
			this.setColor(Color.RED);
			this.HoptoCoordinator = 0;
			MessageTimer eannmsg = new MessageTimer(new InfraEventAlertMessage(this.ID, this.routingtable.get(1).hops, this.myeventID,this.HoptoCoordinator+1,this.ID,this.ID,this.routingtable.get(1).hops));
			double time = new UniformDistribution(0,0.025).nextSample();
			time = time* 0.025*(this.routingtable.get(1).hops);
			System.out.println("TIME: "+time);
			if (time==0)
				time=0.001;
			eannmsg.startRelative(time,this);
			myStatus = Status.CLUSTERING2;
			Overheads = Overheads + 1;
			//Spent with transmission mode
			this.bateria.spend(EnergyMode.SEND,1);
			timerVicinity.tnoStartRelative(10, this, TNO.CLUSTERING2);
			break;
		case CLUSTERING2:
			
			if (myRole == Roles.COORDINATOR){ 
				this.setColor(Color.BLUE);
				announceCH(this.myeventID);
				this.myStatus = Status.READY;
			}else if(myRole == Roles.COLLABORATOR){
				this.setColor(Color.GREEN);
			//	routingtable.remove(this.ID);
				this.myStatus = Status.READY;
			}
			timerVicinity.tnoStartRelative(DataRate, this, TNO.MONITORING);
			break;
		case MONITORING:
			Monitoring();
			break;
			
		case APPROXIMATION:
			if(this.ID==1){
			//	Edges = this.Disttree;
			}else{
			Approximation();}
			break;
		
			default:
			return;
		}
	}
	/*
	 * Pop-ups Methods to exhibit some useful information
	 */
	
	
	
	
	@NodePopupMethod(menuText="Meu Evento")
	public void myEventID() {
		Tools.appendToOutput(myeventID+"\n");
	}
	@NodePopupMethod(menuText="Meu Papel")
	public void myRole() {
		Tools.appendToOutput(myRole+"\n");
	}
	@NodePopupMethod(menuText="Meu Status")
	public void myStatus() {
		Tools.appendToOutput(myStatus+"\n");
	}
	@NodePopupMethod(menuText="Meu CH")
	public void myCH() {
		Tools.appendToOutput(ownerID+"\n");
	}
	
	@NodePopupMethod(menuText="filhosrecv")
	public void filhosrecv() {
		Tools.appendToOutput("Recebi "+this.filhosrecv+"\n");
	}	
	@NodePopupMethod(menuText="filhossend")
	public void filhossend() {
		Tools.appendToOutput("Enviei "+this.filhossend+"\n");
	}	
	
	@NodePopupMethod(menuText="AggDistance")
	public void myaggDist() {
		Tools.appendToOutput(evalAdistCo()+ " through" + routingtable.get(1).nexthop + "\n");
	}
	@NodePopupMethod(menuText="Exibe Tabela de Roteamento")
	public void printHashTable() {
		Set<Integer> keyset = routingtable.keySet();
		int next;
		Tools.appendToOutput("Routing Table of Node "+ Integer.toString(this.ID)+"\n" );
		Iterator<Integer> it = keyset.iterator();
		while(it.hasNext()){
			next = it.next();
			Tools.appendToOutput(next +" " 
					+routingtable.get(next).nexthop +" " 
					+ routingtable.get(next).hops+"\n");
		}
	}
	@NodePopupMethod(menuText="son")
	public void son() {
		Tools.appendToOutput("My Son "+this.son+"\n");
	}	
	



}
