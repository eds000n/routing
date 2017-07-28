package projects.DAARPMSWIM.nodes.messages;

import sinalgo.nodes.messages.DeliveryTimeAwareMessage;
import sinalgo.nodes.messages.Message;

public class DAARPMSWIM_DataMessage extends DeliveryTimeAwareMessage {
//public class DAARPMSWIM_DataMessage extends Message {
	private int dest;
	private int sender;
	private int HopToSink;
	private String payload;
	private int AggPacket;
	private double Energy;
	private int HopstoTree;
	private int EventNum;
	

	public DAARPMSWIM_DataMessage(int sender, int dest, String payload, int hoptosink, int aggpacket, double energy, int eventnum, double dtime) {
		this.dest = dest;
		this.sender = sender;
		this.HopToSink = hoptosink;
		this.payload = payload;
		this.AggPacket = aggpacket;
		this.Energy = energy;
		
		this.EventNum = eventnum;
		this.delivery_time = dtime;
	}


	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new DAARPMSWIM_DataMessage(sender,dest,payload,HopToSink,AggPacket,Energy,EventNum, delivery_time);
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


	public int getHopstoTree() {
		return HopstoTree;
	}


	public void setHopstoTree(int hopstoTree) {
		HopstoTree = hopstoTree;
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
