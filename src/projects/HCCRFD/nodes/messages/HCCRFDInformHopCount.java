package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDInformHopCount extends Message{

	int node_id;		//Id of the sending node
	int hop_count;		//Between the node and the sink/ch
	int base_id;		//the ID of the node that the hop_count is measured from
	
	public HCCRFDInformHopCount(int bid, int nid, int hc){
		this.base_id = bid;
		this.node_id = nid;
		this.hop_count = hc;
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

	public int getHop_count() {
		return hop_count;
	}

	public void setHop_count(int hop_count) {
		this.hop_count = hop_count;
	}

	public int getBase_id() {
		return base_id;
	}

	public void setBase_id(int base_id) {
		this.base_id = base_id;
	}

}
