package projects.HCCRFD.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HCCRFDRequestReply extends Message{

	int source_id;						//if this is -1 then this is sent by the neighbor node
	int neighboringnode_id;				//if this is -1 then this is sent by the source 
	int hop_count;
	double remaining_energy;
	double nn_x;
	double nn_y;
	
	/**
	 * Constructor of the Join_Request and Join_Accept
	 * @param sid	source id
	 * @param nnid	neighbor node id
	 * @param hc	hop count
	 * @param re	remaining energy
	 * @param x		x position
	 * @param y		y position
	 */
	public HCCRFDRequestReply(int sid, int nnid, int hc, double re, double x, double y){
		this.source_id = sid;
		this.neighboringnode_id = nnid;
		this.hop_count = hc;
		this.remaining_energy = re;
		this.nn_x = x;
		this.nn_y = y;
	}
	
	@Override
	public Message clone() {
		return this;
	}

	public int getSource_id() {
		return source_id;
	}

	public void setSource_id(int source_id) {
		this.source_id = source_id;
	}

	public int getNeighboringnode_id() {
		return neighboringnode_id;
	}

	public void setNeighboringnode_id(int neighboringnode_id) {
		this.neighboringnode_id = neighboringnode_id;
	}

	public int getHop_count() {
		return hop_count;
	}

	public void setHop_count(int hop_count) {
		this.hop_count = hop_count;
	}

	public double getRemaining_energy() {
		return remaining_energy;
	}

	public void setRemaining_energy(double remaining_energy) {
		this.remaining_energy = remaining_energy;
	}

	public double getNn_x() {
		return nn_x;
	}

	public void setNn_x(double nn_x) {
		this.nn_x = nn_x;
	}

	public double getNn_y() {
		return nn_y;
	}

	public void setNn_y(double nn_y) {
		this.nn_y = nn_y;
	}

}
