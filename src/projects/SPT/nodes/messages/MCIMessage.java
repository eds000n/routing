package projects.SPT.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Message of Initial Configuration. This kind of message is used for 
 * initial flooding the network and set up the number of hops to the sink
 * (HopToSink) and the next hop (NextHopSink) for all the nodes in the
 * network.
 * @author edson
 *
 */
public class MCIMessage extends Message {
	public int HopToSink;
	public int SenderID;

	
	public MCIMessage(int hopToSink, int senderID) {
		HopToSink = hopToSink;
		SenderID = senderID;
	}


	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new MCIMessage(this.HopToSink,this.SenderID);
	}


	public int getHopToSink() {
		return HopToSink;
	}


	public void setHopToSink(int hopToSink) {
		HopToSink = hopToSink;
	}


	public int getSenderID() {
		return SenderID;
	}


	public void setSenderID(int senderID) {
		SenderID = senderID;
	}
	
	

}
