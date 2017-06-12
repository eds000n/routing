package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class BeaconMessage extends Message{

	int next_node;
	int hop_count;				//between next_node and sink
	float remaining_energy;
	float distance_sn;			//between next_node and source (terminal)
	float distance_nn;			//between next_node and sink
	public BeaconMessage(){
		
	}
	@Override
	public Message clone() {
		return this;
	}

}
