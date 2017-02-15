package projects.DDAARP.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Class that implements the Route Establishment Message (REM)
 * which is generated when a node detects an event and it is sent
 * to the sink for the retrieval of the tree route.
 * The REM travels by using the NextHop field in the DDAARP Node.   
 * @author edson
 *
 */
public class DDAARP_REM extends Message{

	private int SenderID;
	private int CoordinatorID;
	
	public DDAARP_REM(int senderID, int coordinatorID){
		this.SenderID = senderID;
		this.setCoordinatorID(coordinatorID);
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

	public int getCoordinatorID() {
		return CoordinatorID;
	}

	public void setCoordinatorID(int coordinatorID) {
		CoordinatorID = coordinatorID;
	}

}
