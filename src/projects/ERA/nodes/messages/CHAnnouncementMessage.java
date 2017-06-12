package projects.ERA.nodes.messages;

import sinalgo.nodes.messages.Message;

public class CHAnnouncementMessage extends Message {
	int ID; 	//1-indexed
	float residual_energy;
	float x,y;
	
	public CHAnnouncementMessage(int ID, float residual_energy, float x, float y){
		this.ID = ID;
		this.residual_energy = residual_energy;
		this.x = x;
		this.y = y;
	}

	public CHAnnouncementMessage(int ID, Float residual_energy, double x, double y) {
		this.ID = ID;
		this.residual_energy = residual_energy;
		this.x = (float) x;
		this.y = (float) y;
	}

	@Override
	public Message clone() {
		return this;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public float getResidual_energy() {
		return residual_energy;
	}

	public void setResidual_energy(float residual_energy) {
		this.residual_energy = residual_energy;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

}
