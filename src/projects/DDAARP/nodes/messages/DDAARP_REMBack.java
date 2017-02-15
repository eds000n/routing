package projects.DDAARP.nodes.messages;

import java.util.ArrayList;

import sinalgo.nodes.messages.Message;

/**
 * The sink sends back a Route Establishment Message Back to 
 * the sensors that will be included in the routing infraestructure.
 * When a sensor node receives the REMBack message it updates its
 * routing table updating the NextHop field and sendind the REMBack
 * message to the next ID.
 * @author edson
 *
 */
public class DDAARP_REMBack extends Message{

	private int SenderID;
	public ArrayList<Integer> path;
	
	public DDAARP_REMBack(int senderID){
		this.setSenderID(senderID);
		path = new ArrayList<Integer>();
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

	public int getSenderID() {
		return SenderID;
	}

	public void setSenderID(int senderID) {
		SenderID = senderID;
	}

}
