package sinalgo.models.EnergyModel.simple;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;

import projects.DAARPMSWIM.nodes.nodeImplementations.DAARPMSWIMNode;
import projects.DDAARP.nodes.nodeImplementations.DDAARPNode;
import projects.GA.nodes.messages.DeadNodeMessage;
import projects.GA.nodes.nodeImplementations.GANode;
import projects.GA.nodes.timers.RepairDeadNodeGATimer;
import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import projects.HCCRFD.nodes.timers.RepairDeadNodeHCCRFDTimer;
import projects.Infra.nodes.nodeImplementations.InfraNode;
import projects.SPT.nodes.nodeImplementations.SPTNode;
import projects.SPT.nodes.timers.RepairDeadNodeSPTTimer;
import sinalgo.models.EnergyModel.IEnergy;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.nodes.Node;
import sinalgo.nodes.TimerCollection;
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
		if (Tools.getNodeByID(nodeID) instanceof GANode )
			this.minEnergy = (float) (GANode.bVariation*this.totalEnergy);
		else
			this.minEnergy = (float) (0.1f*this.totalEnergy);
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
            this.totalEnergy = 5f;	//unit?
            this.nodeID = nodeID;
            if (Tools.getNodeByID(nodeID) instanceof GANode )
    			this.minEnergy = (float) (GANode.bVariation*this.totalEnergy);
    		else
    			this.minEnergy = (float) (0.1f*this.totalEnergy);
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

    /**
     * returns the residual (remaining) energy 
     */
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
					
					RepairDeadNodeGATimer rdnt = new RepairDeadNodeGATimer(Tools.getNodeByID(nodeID));
					rdnt.startRelative(0.5, Tools.getNodeByID(nodeID));
					
					((GANode)Tools.getNodeByID(this.nodeID)).setDead(true);
				}
				((GANode)Tools.getNodeByID(this.nodeID)).warningMessages++;
			}
			//if ( getEnergy()<= 0 && this.nodeID!=1 && this.isSinkNeighbor ){ //Validates if there is no more energy in the neighbors of the sink
			if ( getEnergy()<= 0 && this.nodeID!=1 ){ //Validates if there is no more energy
				
				//eraseTimers();
//				isDead = 1;
				if ( Tools.getNodeByID(this.nodeID) instanceof GANode ){
					GANode n = (GANode)Tools.getNodeByID(this.nodeID);
					if ( GANode.terminals.contains(this.nodeID-1) ){	//I only discard nodes by energy if they aren't terminals
						//System.out.println("ENERGY LOG: dead node: " + this.nodeID + " (terminal) at " + Tools.getGlobalTime() + " seconds at round " + this.energyPerRound.size());
					}else{
						//GANode.UpNodes.set(this.nodeID-1, 0);
						n.setColor(Color.DARK_GRAY);
						System.out.println("ENERGY LOG: dead node: " + this.nodeID + " at " + Tools.getGlobalTime() + " seconds at round " + this.energyPerRound.size());
						
						//Remove timers from this node.
						
					}
					
				}
				
				//FIXME: not reporting data into the log because it is not generating the MSGTREE message necessary for collecting the final routing tree.
				/*
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
				}*/
					
			}
		}else if (Tools.getNodeByID(nodeID) instanceof SPTNode){
			if ( getEnergy() < this.minEnergy && !SPTNode.terminals.contains(this.nodeID-1) ){
//			if ( getEnergy() <= 0.1 && !SPTNode.terminals.contains(this.nodeID-1) ){
				if ( !((SPTNode)Tools.getNodeByID(this.nodeID)).isDead() ){
					
					RepairDeadNodeSPTTimer rdnt = new RepairDeadNodeSPTTimer((SPTNode)Tools.getNodeByID(nodeID));
					rdnt.startRelative(0.5, Tools.getNodeByID(nodeID));
					
					((SPTNode)Tools.getNodeByID(this.nodeID)).setDead(true);
				}
				//((SPTNode)Tools.getNodeByID(this.nodeID)).warningMessages++;
			}
			if ( getEnergy()<= 0 && this.nodeID!=1 ){ //Validates if there is no more energy
				
			}
		}else if (Tools.getNodeByID(nodeID) instanceof DAARPMSWIMNode){
			;
		}else if (Tools.getNodeByID(nodeID) instanceof DDAARPNode ){
			;
		}else if (Tools.getNodeByID(nodeID) instanceof HCCRFDNode ){
//			if ( getEnergy() < this.minEnergy && !HCCRFDNode.terminals.contains(this.nodeID-1) ){
			if ( getEnergy() <= 0.1 && !HCCRFDNode.terminals.contains(this.nodeID-1) ){
				if ( !((HCCRFDNode)Tools.getNodeByID(this.nodeID)).isDead() ){
					HCCRFDNode n = (HCCRFDNode)Tools.getNodeByID(this.nodeID);
					RepairDeadNodeHCCRFDTimer rdnt = new RepairDeadNodeHCCRFDTimer(n);
					rdnt.startRelative(0.5,  n);
					
					n.setDead(true);
					
					/*****************************************************************
					 * This should not be here!!!
					 */
					n.debugMsg(">>> distribued " + nodeID + " reported itself as dead with battery of " + getEnergy(),2);
//					System.out.println(">>> distribued " + nodeID + " reported itself as dead with battery of " + getEnergy());
					ArrayList<Integer> neighbors = new ArrayList<>();
					for ( int i=0; i<n.listAll.size(); i++ ){
						HCCRFDNode neighbor_node = (HCCRFDNode)Tools.getNodeByID(n.listAll.get(i)); 
//						neighbor_node..spend(EnergyMode.RECEIVE);
				
						if ( neighbor_node.listAll.contains(nodeID) ){
							neighbor_node.listAll.removeFirstOccurrence(nodeID);
							//neighbor_node.listAll.remove(this.ID);
						}
						int d = 0;
						if ( neighbor_node.my_ch == -1 ) //I am CH
							d = neighbor_node.FNS(1);
						else
							d = neighbor_node.FNS(neighbor_node.my_ch);
//						if ( neighbor_node.NextHopSink == this.ID ){
						if ( d == nodeID ){
//							neighbor_node.battery.spend(EnergyMode.SEND);
//							Overheads += 1;
							neighbors.add(n.listAll.get(i));
						}
					}
					((HCCRFDNode)Tools.getNodeByID(1)).updateConnectedComponents(neighbors);
					/******************************************************************
					 * This should not be here!!!
					 */
				}
			}
			if ( getEnergy()<= 0 && this.nodeID!=1 ){ //Validates if there is no more energy
//				Tools.removeNode(Tools.getNodeByID(this.nodeID));
//				Tools.removeNode(n);
			}
		}
		
		//System.out.println("ENERGY LOG: residual energy " + getEnergy() + " for node " + this.nodeID);
		
//		return isDead;
		
	}
}

