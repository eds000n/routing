package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraCHIntentMessage extends Message {
	
	public int ownerID;
	private int eventID;
	public InfraCHIntentMessage(int ownerID, int eventID){
		this.ownerID = ownerID;
		this.eventID = eventID;
	}
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	public int getEventID() {
		return eventID;
	}
	

}
