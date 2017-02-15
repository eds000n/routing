package projects.DDAARP.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DDAARP_AlertMessage extends Message {
	private int myID;
	private int HopToSink;
	private int eventID;
	private int HopToEvent;
	private int SenderID;
	private int HopstoCoordinator;
	private int CandidateId;
	private int OwnerId_HopstoEvent;
	private int OwnerId_HopstoSink;
	
	public DDAARP_AlertMessage(int myID, int hopToSink,int eventID, int hoptoevent,int hopstocoordinator, int senderid, int candidateid, int owneridhoptoevent, int owneridhopstosink) {
		this.myID = myID;
		HopToSink = hopToSink;
		this.eventID = eventID;
		this.HopToEvent = hoptoevent;
		this.SenderID = senderid;
		this.HopstoCoordinator = hopstocoordinator;
		this.CandidateId = candidateid;
		this.OwnerId_HopstoEvent = owneridhoptoevent;
		this.OwnerId_HopstoSink = owneridhopstosink;
	}

	
	
	public int getOwnerId_HopstoEvent() {
		return OwnerId_HopstoEvent;
	}



	public void setOwnerId_HopstoEvent(int ownerId_HopstoEvent) {
		OwnerId_HopstoEvent = ownerId_HopstoEvent;
	}



	public int getOwnerId_HopstoSink() {
		return OwnerId_HopstoSink;
	}



	public void setOwnerId_HopstoSink(int ownerId_HopstoSink) {
		OwnerId_HopstoSink = ownerId_HopstoSink;
	}



	public int getCandidateId() {
		return CandidateId;
	}


	public void setCandidateId(int candidateId) {
		CandidateId = candidateId;
	}


	public int getHopstoCoordinator() {
		return HopstoCoordinator;
	}

	public void setHopstoCoordinator(int hopstoCoordinator) {
		HopstoCoordinator = hopstoCoordinator;
	}

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}


	public int getSenderID() {
		return SenderID;
	}

	public void setSenderID(int senderID) {
		SenderID = senderID;
	}

	public void setMyID(int myID) {
		this.myID = myID;
	}

	public void setHopToSink(int hopToSink) {
		HopToSink = hopToSink;
	}

	public void setEventID(int eventID) {
		this.eventID = eventID;
	}

	public void setHopToEvent(int hopToEvent) {
		HopToEvent = hopToEvent;
	}

	public int getHopToSink() {
		return HopToSink;
	}

	public int getMyID() {
		return myID;
	}

	public int getEventID() {
		return eventID;
	}

	public int getHopToEvent() {
		return HopToEvent;
	}

	

}
