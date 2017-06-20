package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDDataMessage extends Message {
	private int dest;
	private int sender;
	private int HopToSink;
	private StringBuffer payload;
	private double delivery_time = 0;

	public HCCRFDDataMessage(int sender, int dest, StringBuffer payload, int hoptosink, double delivery_time ) {
		this.dest = dest;
		this.sender = sender;
		this.HopToSink = hoptosink;
		this.payload = payload;
		this.delivery_time = delivery_time ;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new HCCRFDDataMessage(sender,dest,payload,HopToSink,delivery_time);
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
	
	public double getDeliveryTime(){
		return delivery_time;
	}
	
	private void accumDTime(double time){
		this.delivery_time += time;
	}

	/**
	 * delay = d_tranmission + d_propagation + d_processing + d_queue
	 * @param time delay time added to the packet
	 */
	public void accumDTime(){
		double d_tranmission = 0.131;			// depends on the packet size and the MAC (Medium Access Control)
												// 4096bytes/250kbps
		double d_propagation = 0.00000013;		// communication radius: 40
												// depends on the medium: 40m/(3*10^8m/s)
		double d_processing = 0;				// too small, can be ignored
		double d_queue = 0;						// too small, can be ignored
		accumDTime(d_tranmission + d_propagation + d_processing + d_queue);
	}
}
