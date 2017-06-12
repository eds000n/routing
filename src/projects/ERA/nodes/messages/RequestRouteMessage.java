package projects.ERA.nodes.messages;

import sinalgo.nodes.messages.Message;

public class RequestRouteMessage extends Message{
	private int ID;
	private int level;
	private double residual_energy;
	private double x,y;
	
	public RequestRouteMessage(int ID, int level, double residual_energy, double x, double y){
		this.ID = ID;
		this.level = level;
		this.residual_energy = residual_energy;
		this.x = x;
		this.y = y;
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

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double  getResidual_energy() {
		return residual_energy;
	}

	public void setResidual_energy(double residual_energy) {
		this.residual_energy = residual_energy;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

}
