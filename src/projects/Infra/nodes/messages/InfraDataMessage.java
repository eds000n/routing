package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraDataMessage extends Message {
	private String payload;
	private int dest;
	private int relay;
	private int source;
	private int eventID;
	private int aggPackets;
	private String MyRole;
	
	public InfraDataMessage(int source, int aggPackets){
		this.source = source;
		this.aggPackets = aggPackets;
	}
	
	public InfraDataMessage(int source, int dest, int relay, int eventID, int aggPackets, String payload, String role){
		this.source = source;
		this.dest = dest;
		this.relay = relay;
		this.payload = payload;
		this.eventID = eventID;
		this.aggPackets = aggPackets;
		this.MyRole = role;
	}
	
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new InfraDataMessage(this.source, this.dest, this.relay, this.eventID, this.aggPackets, this.payload, this.MyRole);
	}
	
	public String getMyRole() {
		return MyRole;
	}

	public void setMyRole(String myRole) {
		MyRole = myRole;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public String getPayload(){
		return this.payload;
	}
	public int getDest(){
		return this.dest;
	}
	
	public void setDest(int dest) {
		this.dest = dest;
	}
	public int getRelay(){
		return this.relay;
	}
	public void setRelay(int relay){
		this.relay = relay;
	}
	public int getSource() {
		return source;
	}
	public int getEventID() {
		return eventID;
	}

	public int getAggPackets() {
		return aggPackets;
	}

	public void setAggPackets(int aggPackets) {
		this.aggPackets = aggPackets;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public void setEventID(int eventID) {
		this.eventID = eventID;
	}
	
}
