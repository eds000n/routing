package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraSKAnnounceMessage extends Message {
	private int skID;
	private int hops=0;
	private int aggdistco=10000;
	private int eventID=-1;
	public InfraSKAnnounceMessage(int skID, int hops, int aggdistco, int eventID){
		this.skID = skID;
		this.hops = hops;
		this.aggdistco = aggdistco;
		this.eventID = eventID;
	}
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new InfraSKAnnounceMessage(this.skID,this.hops, aggdistco, this.eventID);
	}
	public int getHops() {
		return hops;
	}
	public void setHops(int hops) {
		this.hops = hops;
	}
	public int getSkID() {
		return skID;
	}
	public int getAggdistco() {
		return aggdistco;
	}
	public void setAggdistco(int aggdistco) {
		 this.aggdistco =  aggdistco;
	}
	public int getEventID() {
		return eventID;
	}

}
