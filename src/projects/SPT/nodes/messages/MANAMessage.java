package projects.SPT.nodes.messages;

import sinalgo.nodes.messages.Message;

public class MANAMessage extends Message {
	public int HopToSinkEvent;
	public int SenderID;
	public int DetectionID;

	
	public MANAMessage(int hopToSinkEvent, int senderID, int detectionID) {

		HopToSinkEvent = hopToSinkEvent;
		SenderID = senderID;
		DetectionID = detectionID;
	}



	@Override
	public Message clone() {
		// TODO Auto-generated method stub
	
		return new MANAMessage(this.HopToSinkEvent,this.SenderID, this.DetectionID); 
	}



	public int getHopToSinkEvent() {
		return HopToSinkEvent;
	}



	public void setHopToSinkEvent(int hopToSinkEvent) {
		HopToSinkEvent = hopToSinkEvent;
	}



	public int getSenderID() {
		return SenderID;
	}



	public void setSenderID(int senderID) {
		SenderID = senderID;
	}



	public int getDetectionID() {
		return DetectionID;
	}



	public void setDetectionID(int detectionID) {
		DetectionID = detectionID;
	}
	
	
	

}
