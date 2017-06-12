package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDEnergyLevel extends Message{
	int node_id;
	float remaining_energy;
	
	public HCCRFDEnergyLevel(int id, float re){
		this.node_id = id;
		this.remaining_energy = re;
	}

	@Override
	public Message clone() {
		return this;
	}
}
