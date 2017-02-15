package projects.GA.nodes.messages;

import sinalgo.nodes.messages.Message;

public class RemoveMessage extends Message{

	int nodeID = -1;	//1-indexed
	
	public int getNodeID() {
		return nodeID;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public RemoveMessage(int nodeID){
		this.nodeID = nodeID;
	}
	
	@Override
	public Message clone() {
		return this;
	}

}
