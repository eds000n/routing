package projects.GA.nodes.messages;

import java.util.ArrayList;

import sinalgo.nodes.messages.Message;

public class DeadNodeMessage extends Message {
	int nodeID; 				//dead node ID		(1-indexed)
	float reportedEnergy ;		//remaining energy on the node that reported itself as dead
	ArrayList<Integer> neighbors;	//list of nodes to be reconfigured	(1-indexed)

	public DeadNodeMessage (int nodeID, float reportedEnergy){
		this.nodeID = nodeID;
		this.reportedEnergy = reportedEnergy;
		neighbors = new ArrayList<>();
	}
	
	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public float getReportedEnergy() {
		return reportedEnergy;
	}

	public void setReportedEnergy(float reportedEnergy) {
		this.reportedEnergy = reportedEnergy;
	}
	
	public ArrayList<Integer> getNeighbors(){
		return neighbors;
	}
	
	public void setNeighbors(ArrayList<Integer> neighbors){
		this.neighbors.addAll(neighbors);
	}

	@Override
	public Message clone() {
		return this;
	}

}
