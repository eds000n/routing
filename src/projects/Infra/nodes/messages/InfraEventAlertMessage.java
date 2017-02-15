package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraEventAlertMessage extends Message {
	private int myID;
	private int HopToSink;
	private int eventID;
	private int HopstoCoordinator;
	private int SenderID;
	private int CandidateId;
	private int OwnerId_HopstoSink;
	
	public InfraEventAlertMessage(int myID, int hopToSink,int eventID, int hopstocoordinator, int senderid, int candidateid,  int owneridhopstosink) {
		this.myID = myID;
		this.eventID = eventID;
		this.myID = myID;
		this.HopToSink = hopToSink;
		this.eventID = eventID;
		this.SenderID = senderid;
		this.HopstoCoordinator = hopstocoordinator;
		this.CandidateId = candidateid;
		this.OwnerId_HopstoSink = owneridhopstosink;
	
	}
	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}
	public int getMyID() {
		return myID;
	}
	public void setMyID(int myID) {
		this.myID = myID;
	}
	public int getHopToSink() {
		return HopToSink;
	}
	public void setHopToSink(int hopToSink) {
		HopToSink = hopToSink;
	}
	public int getEventID() {
		return eventID;
	}
	public void setEventID(int eventID) {
		this.eventID = eventID;
	}
	public int getHopstoCoordinator() {
		return HopstoCoordinator;
	}
	public void setHopstoCoordinator(int hopstoCoordinator) {
		HopstoCoordinator = hopstoCoordinator;
	}
	public int getSenderID() {
		return SenderID;
	}
	public void setSenderID(int senderID) {
		SenderID = senderID;
	}
	public int getCandidateId() {
		return CandidateId;
	}
	public void setCandidateId(int candidateId) {
		CandidateId = candidateId;
	}
	public int getOwnerId_HopstoSink() {
		return OwnerId_HopstoSink;
	}
	public void setOwnerId_HopstoSink(int ownerId_HopstoSink) {
		OwnerId_HopstoSink = ownerId_HopstoSink;
	}
	
}
