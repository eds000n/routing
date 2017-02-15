package projects.DDAARP.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DDAARP_SinkFloodMessage extends Message {
	public int HopToSink;
	public int SenderID;
	public String Rolesent;
	public double Energy;

	public DDAARP_SinkFloodMessage(int hopToSink, int senderID,double energy, String rolesent) {
		HopToSink = hopToSink;
		SenderID = senderID;
		Rolesent = rolesent;
		Energy = energy;
	}


	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	

}
