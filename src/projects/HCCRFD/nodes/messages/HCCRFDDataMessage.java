package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.DeliveryTimeAwareMessage;
import sinalgo.nodes.messages.Message;

public class HCCRFDDataMessage extends DeliveryTimeAwareMessage {
	private int dest;
	private int sender;
	private int HopToSink;
	private StringBuffer payload;

	public HCCRFDDataMessage(int sender, int dest, StringBuffer payload, int hoptosink, double dtime) {
		this.dest = dest;
		this.sender = sender;
		this.HopToSink = hoptosink;
		this.payload = payload;
		this.delivery_time = dtime;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new HCCRFDDataMessage(sender,dest,payload,HopToSink, delivery_time);
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
	
}
