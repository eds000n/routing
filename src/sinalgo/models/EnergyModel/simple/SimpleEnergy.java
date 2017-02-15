package sinalgo.models.EnergyModel.simple;


import java.awt.Color;
import java.util.Hashtable;
import java.util.Iterator;

import projects.DAARPMSWIM.nodes.nodeImplementations.DAARPMSWIMNode;
import projects.DDAARP.nodes.nodeImplementations.DDAARPNode;
import projects.GA.nodes.messages.DeadNodeMessage;
import projects.GA.nodes.nodeImplementations.GANode;
import projects.GA.nodes.timers.DeadNodeGATimer;
import projects.Infra.nodes.nodeImplementations.InfraNode;
import projects.SPT.nodes.nodeImplementations.SPTNode;
import sinalgo.models.EnergyModel.IEnergy;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;


public class SimpleEnergy implements IEnergy {
    
    private Float sleep;
    private Float transmission;
    private Float receive;
    private Float processing;
    private Float listen;
    private Float totalEnergy;
    private Float minEnergy;		//This is the minimum remaining energy in order to mark this node as dead.
    
    public Float getMinEnergy() {
		return minEnergy;
	}

	public void setMinEnergy(Float minEnergy) {
		this.minEnergy = minEnergy;
	}

	public void setTotalEnergy(Float totalEnergy) {
		this.totalEnergy = totalEnergy;
	}

	private int nodeID;		//1-indexed
    private boolean isSinkNeighbor = false;	//Used for the energy hole metric
   
    private Hashtable<Double, Float> energyPerRound = new Hashtable<Double, Float>();
   
    public Hashtable<Double, Float> getEnergyPerRound(){
            return energyPerRound;
    }
   
    public SimpleEnergy (int nodeID){
            this.sleep = Float.valueOf(0);
            this.transmission = Float.valueOf(0);
            this.receive = Float.valueOf(0);
            this.processing = Float.valueOf(0);
            this.listen = Float.valueOf(0);
            //this.totalEnergy = 29160f;
//            this.totalEnergy = 200f;
            this.totalEnergy = 50f;
            this.nodeID = nodeID;
            this.minEnergy = 0.05f*this.totalEnergy;
    }
   
    public void setIsSinkNeighbor(boolean isSinkNeighbor){
    	this.isSinkNeighbor = isSinkNeighbor;
    }
    
    public Float getTotalSpentEnergy(){
            return sleep + transmission + receive + processing + listen;
    }
   
    private void calculateEnergyPerRound(Float value){
            Double round = Tools.getGlobalTime();
            if (energyPerRound.containsKey(round)){
                    Float tmp = energyPerRound.get(round);
                    energyPerRound.put(round, tmp + value);
            }else{
                    energyPerRound.put(round, value);
            }
    }
   
    public void spend(EnergyMode mode, float time){
            switch (mode) {
            case LISTEN:
                    listen += Config.ENERG_ESCUTA;
                    calculateEnergyPerRound(Config.ENERG_ESCUTA);
                    break;
            case RECEIVE:
                    receive += Config.ENERG_RECEPCAO*time;
                    calculateEnergyPerRound(Config.ENERG_RECEPCAO*time);
                    break;
            case SEND:
                    transmission += Config.ENERG_TRANSMISSAO*time;
                    calculateEnergyPerRound(Config.ENERG_TRANSMISSAO*time);
                    break;
            case SLEEP:
                    sleep += Config.ENERG_SLEEP;
                    calculateEnergyPerRound(Config.ENERG_SLEEP);
                    break;
            case PROCESSING:
                    processing += 0;
                    calculateEnergyPerRound(0f);
                    break;
            case MONITOR:                  
                    break;
            default:
                    break;
            }              
    }

    public Float getEnergy() {
            return totalEnergy - getTotalSpentEnergy();
    }
   
    public Float getInitialEnergy(){
            return this.totalEnergy;
    }

	@Override
	public void spend(EnergyMode mode) {
		//The energy model assumes that the energy required for transmission and reception are the same
		spend(mode, 1);
		if (Tools.getNodeByID(this.nodeID) instanceof GANode){
			if ( getEnergy() < this.minEnergy && !GANode.terminals.contains(this.nodeID-1) ){
				if ( !((GANode)Tools.getNodeByID(this.nodeID)).isDead() ){
					DeadNodeMessage dnm = new DeadNodeMessage(nodeID, getEnergy());
					DeadNodeGATimer dnt = new DeadNodeGATimer(Tools.getNodeByID(nodeID));
//					dnt.startRelative(GANode.DataRate/2, Tools.getNodeByID(nodeID));
					dnt.startRelative(1, Tools.getNodeByID(nodeID));
					((GANode)Tools.getNodeByID(this.nodeID)).setDead(true);
				}
			}
			//if (getEnergy()<0 ){
			//if ( getEnergy()<= 0 && this.nodeID!=1 && this.isSinkNeighbor ){ //Validates if there is no more energy in the neigbors of the sink
			if ( getEnergy()<= 0 && this.nodeID!=1 ){ //Validates if there is no more energy 
//				isDead = 1;
				if ( Tools.getNodeByID(this.nodeID) instanceof GANode ){
					GANode n = (GANode)Tools.getNodeByID(this.nodeID);
					
					
					if ( GANode.terminals.contains(this.nodeID-1) ){	//I only discard nodes by energy if they aren't terminals
						//System.out.println("ENERGY LOG: dead node: " + this.nodeID + " (terminal) at " + Tools.getGlobalTime() + " seconds at round " + this.energyPerRound.size());
					}else{
						n.UpNodes.set(this.nodeID-1, 0);
						n.setColor(Color.DARK_GRAY);
						System.out.println("ENERGY LOG: dead node: " + this.nodeID + " at " + Tools.getGlobalTime() + " seconds at round " + this.energyPerRound.size());
					}
					
				}
				
				//FIXME: not reporting data into the log because it is not generating the MSGTREE message necessary for collecting the final routing tree.
				
				Iterator<Node> it = Tools.getNodeList().iterator();
				Node next;
				while(it.hasNext()){
					
					next = it.next();
					
					if (next instanceof SPTNode){
						SPTNode.Energy.logln(
							next.ID+"\t"+
							next.getPosition().xCoord+"\t"
							+next.getPosition().yCoord+"\t"
							+((SPTNode)next).getBateria().getEnergy()
						);
					} else if (next instanceof GANode){
						GANode.Energy.logln(
							next.ID+"\t"+
							next.getPosition().xCoord+"\t"
							+next.getPosition().yCoord+"\t"
							+((GANode)next).getBattery().getEnergy()
						);
					} else if (next instanceof InfraNode){
						InfraNode.EnergyNetwork.logln(
								next.ID+"\t"+
								next.getPosition().xCoord+"\t"
								+next.getPosition().yCoord+"\t"
								+((InfraNode)next).getBateria().getEnergy()
						);
					} else if (next instanceof DAARPMSWIMNode){
						DAARPMSWIMNode.DAARPEnergy.logln(
								next.ID+"\t"+
								next.getPosition().xCoord+"\t"
								+next.getPosition().yCoord+"\t"
								+((DAARPMSWIMNode)next).getBattery().getEnergy()
						);
					} else if (next instanceof DDAARPNode){
						;//TODO
					}
				}
				int obj = ((GANode)Tools.getNodeByID(this.nodeID)).objFunction;
				if ( obj== 1)
					;//sinalgo.tools.Tools.exit();
				else if ( obj==2 || obj==3 ){
					;//GANode.UpNodes.set(index, element)
				}
					
			}
		}
		
		//System.out.println("ENERGY LOG: residual energy " + getEnergy() + " for node " + this.nodeID);
		
//		return isDead;
		
	}

    
    
}

