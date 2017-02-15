package projects.DAARPMSWIM.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DAARPMSWIM_AnnouceCHMessage extends Message {
	private int HopToEvent;
	private int SenderID;
	private int eventnum;
	private int HopToSink;
	private int NextHopSender;
	private double Energy;
	
	
	
	public DAARPMSWIM_AnnouceCHMessage(int hopToEvent, int sender, int eventnum, int hoptosink, int nexthopsender, double energy) {
		HopToEvent = hopToEvent;
		SenderID = sender;
		this.eventnum = eventnum;
		this.HopToSink = hoptosink;
		this.NextHopSender = nexthopsender;
		this.Energy = energy;
		
	}

	

	

	public double getEnergy() {
		return Energy;
	}


	public void setEnergy(double energy) {
		Energy = energy;
	}


	public int getNextHopSender() {
		return NextHopSender;
	}


	public void setNextHopSender(int nextHopSender) {
		NextHopSender = nextHopSender;
	}




	public int getHopToSink() {
		return HopToSink;
	}


	public void setHopToSink(int hopToSink) {
		HopToSink = hopToSink;
	}


	public void setEventnum(int eventnum) {
		this.eventnum = eventnum;
	}


	public int getEventnum() {
		return eventnum;
	}


	public int getHopToEvent() {
		return HopToEvent;
	}

	public void setHopToEvent(int hopToEvent) {
		HopToEvent = hopToEvent;
	}

	public int getSenderID() {
		return SenderID;
	}

	public void setSenderID(int senderID) {
		SenderID = senderID;
	}






	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
