package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDDataMessage extends Message {
	private int dest;
	private int sender;
	private int HopToSink;
	private StringBuffer payload;
	private double timestamp;

	public HCCRFDDataMessage(int sender, int dest, StringBuffer payload, int hoptosink, double timestamp ) {
		this.dest = dest;
		this.sender = sender;
		this.HopToSink = hoptosink;
		this.payload = payload;
		this.timestamp = timestamp;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new HCCRFDDataMessage(sender,dest,payload,HopToSink,timestamp);
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
	public StringBuffer getPayload() {
		return payload;
	}


	public void setPayload(StringBuffer payload) {
		this.payload = payload;
	}
	
	public double getTimestamp(){
		return timestamp;
	}

}
