package projects.DDAARP.nodes.messages;

import java.util.ArrayList;
import sinalgo.nodes.messages.Message;

/**
 * Class that implements the Collecting Information Message that is
 * generated from the border nodes and is sent back to the sink with
 * the adjacency matrix of the network.
 * @author edson
 *
 */
public class DDAARP_CIM extends Message {
	private int SenderID;
	public ArrayList<Integer> node1;
	public ArrayList<Integer> node2;
	
	public DDAARP_CIM(int SenderID){
		this.setSenderID(SenderID);
		node1 = new ArrayList<Integer>();
		node2 = new ArrayList<Integer>();
	}
	
	public void addEdge(Integer n1, Integer n2){
		this.node1.add(n1);
		this.node2.add(n2);
	}
	
	@Override
	public Message clone() {
		DDAARP_CIM msg = new DDAARP_CIM(SenderID);
		msg.node1.addAll(node1);
		msg.node2.addAll(node2);
		return msg;
	}
	public int getSenderID() {
		return SenderID;
	}
	public void setSenderID(int senderID) {
		SenderID = senderID;
	}
}
