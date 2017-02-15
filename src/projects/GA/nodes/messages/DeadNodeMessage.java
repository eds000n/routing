package projects.GA.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DeadNodeMessage extends Message {
	int nodeID; 				//dead node ID		(1-indexed)
	float reportedEnergy ;		//remaining energy on the node that reported itself as dead

	public DeadNodeMessage (int nodeID, float reportedEnergy){
		this.nodeID = nodeID;
		this.reportedEnergy = reportedEnergy;
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

	@Override
	public Message clone() {
		return this;
	}

}
