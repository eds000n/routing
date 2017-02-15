package projects.DDAARP.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DDAARP_MSGUPDATECH extends Message {
	public int HopToCoordinator;
	public int HoptoSink;
	public int SenderID;
	public int OwnerID;
	public int EventNum;


	public DDAARP_MSGUPDATECH(int hopToCoordinator, int hoptosink, int senderID, int eventnum, int ownerid) {
		HopToCoordinator = hopToCoordinator;
		HoptoSink = hoptosink;
		SenderID = senderID;
		EventNum = eventnum;
		OwnerID = ownerid;
	}


	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
