package projects.DAARPMSWIM.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DAARPMSWIM_AnexaToSink extends Message {
	public int nexthop;
	public int EventNum;
	public int SenderId;
	
	
	public DAARPMSWIM_AnexaToSink(int nexthop,int eventnum, int sender) {
	
		this.nexthop = nexthop;
		this.EventNum = eventnum;
		this.SenderId = sender;
	}


	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return new DAARPMSWIM_AnexaToSink(nexthop,EventNum,SenderId);
	}


	public int getNexthop() {
		return nexthop;
	}


	public void setNexthop(int nexthop) {
		this.nexthop = nexthop;
	}


	public int getEventNum() {
		return EventNum;
	}


	public void setEventNum(int eventNum) {
		EventNum = eventNum;
	}


	public int getSenderId() {
		return SenderId;
	}


	public void setSenderId(int senderId) {
		SenderId = senderId;
	}
	
	

}
