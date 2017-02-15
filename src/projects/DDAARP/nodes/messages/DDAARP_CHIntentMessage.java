package projects.DDAARP.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DDAARP_CHIntentMessage extends Message {
private int ChCandidate_Id;
private int Sender_Id;
private int HopToSInk;
private int HopsToCoordinator;


private int HoptoEvent;
	
	public DDAARP_CHIntentMessage(int senderID, int chcandidate, int hopToSInk, int hoptoevent, int hopstocoordinator) {
		this.Sender_Id = senderID;
		this.ChCandidate_Id = chcandidate;
		HopToSInk = hopToSInk;
		this.HoptoEvent = hoptoevent;
		this.HopsToCoordinator = hopstocoordinator;
}


	public int getHopsToCoordinator() {
		return HopsToCoordinator;
	}


	public void setHopsToCoordinator(int hopsToCoordinator) {
		HopsToCoordinator = hopsToCoordinator;
	}


	public int getChCandidate_Id() {
		return ChCandidate_Id;
	}



	public void setChCandidate_Id(int chCandidate_Id) {
		ChCandidate_Id = chCandidate_Id;
	}










	public int getSender_Id() {
		return Sender_Id;
	}










	public void setSender_Id(int sender_Id) {
		Sender_Id = sender_Id;
	}






	public int getHopToSInk() {
		return HopToSInk;
	}





	public void setHopToSInk(int hopToSInk) {
		HopToSInk = hopToSInk;
	}





	public int getHoptoEvent() {
		return HoptoEvent;
	}





	public void setHoptoEvent(int hoptoEvent) {
		HoptoEvent = hoptoEvent;
	}





	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
