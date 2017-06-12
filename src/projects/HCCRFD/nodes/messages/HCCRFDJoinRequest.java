package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDJoinRequest extends Message{

	int node_id;
	int ch_id;
	
	/**
	 * 
	 * @param nid	node id
	 * @param chid	CH id
	 */
	public HCCRFDJoinRequest(int nid, int chid){
		this.node_id = nid;
		this.ch_id = chid;
	}
	
	@Override
	public Message clone() {
		return this;
	}
	
	public int getNode_id() {
		return node_id;
	}

	public void setNode_id(int node_id) {
		this.node_id = node_id;
	}

	public int getCh_id() {
		return ch_id;
	}

	public void setCh_id(int ch_id) {
		this.ch_id = ch_id;
	}

}
