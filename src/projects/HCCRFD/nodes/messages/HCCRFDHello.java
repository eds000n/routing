package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDHello extends Message{
	int node_id;
	float x;
	float y;
	
	public HCCRFDHello(int id, float x, float y){
		this.node_id = id;
		this.x = x;
		this.y = y;
	}

	@Override
	public Message clone() {
		return this;
	}
}
