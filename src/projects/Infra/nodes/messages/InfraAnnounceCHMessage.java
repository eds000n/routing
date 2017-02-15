package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraAnnounceCHMessage extends Message {
	private int chID;
	private int eventID=0;
	private int hopstoEvent=0;
	private String myRole=null;
	private int TTL=100; 
	private int hopstoSink= 1000;
	public InfraAnnounceCHMessage(int chID, int eventID, int TTL,int hops, int hopstoSink, String myRole){
		this.chID = chID;
		this.eventID = eventID;
		this.TTL = TTL;
		this.hopstoEvent = hops;
		this.myRole = myRole;
		this.hopstoSink = hopstoSink;
	}
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new InfraAnnounceCHMessage(this.chID, this.eventID, this.TTL, this.hopstoEvent, this.hopstoSink, this.myRole);
	}
	public int getchID(){
		return this.chID;
	}
	public int getEventID(){
		return this.eventID;
	}
	public int getTTL(){
		return this.TTL;
	}
	public int getHopstoEvent() {
		return hopstoEvent;
	}
	public int getHopstoSink() {
		return hopstoSink;
	}
	public void setHopstoEvent(int hops) {
		this.hopstoEvent = hops;
	}
	public void setHopstoSink(int hopstoSink) {
		this.hopstoSink = hopstoSink;
	}
	public String getMyRole() {
		return myRole;
	}
	public void setMyRole(String myRole) {
		this.myRole = myRole;
	}
	
}
