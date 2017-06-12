package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDJoinAccept extends Message{

	int ch_id;
	int node_id;
	
	public HCCRFDJoinAccept(int chid, int nid){
		this.ch_id = chid;
		this.node_id = nid;
	}
	
	@Override
	public Message clone() {
		return this;
	}

	public int getCh_id() {
		return ch_id;
	}

	public void setCh_id(int ch_id) {
		this.ch_id = ch_id;
	}

	public int getNode_id() {
		return node_id;
	}

	public void setNode_id(int node_id) {
		this.node_id = node_id;
	}

}
