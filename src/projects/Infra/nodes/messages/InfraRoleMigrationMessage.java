package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraRoleMigrationMessage extends Message {
	private int newCH;
	private int eventID;
	public InfraRoleMigrationMessage(int newCH, int eventID){
		this.newCH = newCH;
		this.eventID = eventID;
	}
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	public int getNewCH() {
		return newCH;
	}
	public int getEventID() {
		return eventID;
	}
	
	
}
