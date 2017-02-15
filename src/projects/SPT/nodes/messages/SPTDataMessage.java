package projects.SPT.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * SPTDataMessage is the kind of message used when a node detects an event and has to send the 
 * gathered information to the sink. The payload is the actual information that is carried though
 * the network until reaching the sink. 
 * @author edson
 *
 */
public class SPTDataMessage extends Message {
	private int dest;
	private int sender;
	private int HopToSink;
	private String payload;
	private int AggPacket;
	private double Energy;
	private int EventNum;


	public SPTDataMessage(int sender, int dest, String payload, int hoptosink, int aggpacket, double energy, int eventnum) {
		this.dest = dest;
		this.sender = sender;
		this.HopToSink = hoptosink;
		this.payload = payload;
		this.AggPacket = aggpacket;
		this.Energy = energy;
		this.EventNum = eventnum;
	}



	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new SPTDataMessage(sender,dest,payload,HopToSink,AggPacket,Energy,EventNum);
	}


	public int getEventNum() {
		return EventNum;
	}


	public void setEventNum(int eventNum) {
		EventNum = eventNum;
	}


	public double getEnergy() {
		return Energy;
	}


	public void setEnergy(double energy) {
		Energy = energy;
	}



	public int getHopToSink() {
		return HopToSink;
	}


	public void setHopToSink(int hopToSink) {
		HopToSink = hopToSink;
	}


	public int getSender() {
		return sender;
	}


	public void setSender(int sender) {
		this.sender = sender;
	}


	public int getDest() {
		return dest;
	}


	public void setDest(int dest) {
		this.dest = dest;
	}
	public String getPayload() {
		return payload;
	}


	public void setPayload(String payload) {
		this.payload = payload;
	}


	public int getAggPacket() {
		return AggPacket;
	}


	public void setAggPacket(int aggPacket) {
		AggPacket = aggPacket;
	}
	
	

}
