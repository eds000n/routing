package projects.HCCRFD.nodes.messages;

import java.util.ArrayList;

import sinalgo.nodes.messages.Message;

public class SetRepairRouteMessage extends Message{

	private ArrayList<Integer> path;		//1-indexed
	private int nextHop;					//1-indexed
	private int hopsToSink;
	
	public SetRepairRouteMessage(int nt){
		path = new ArrayList<>();
		this.nextHop = nt;
		this.hopsToSink = 0;
	}
	
	@Override
	public Message clone() {
		SetRepairRouteMessage srrm = new SetRepairRouteMessage(this.nextHop);
		srrm.setPath(this.getPath());
		srrm.setHopsToSink(this.hopsToSink);
		return srrm;
	}
	
	/**
	 * 
	 * @return 0 if this is the last element in the path
	 */
	public int top(){
		if ( this.path.size() >= 0){
			int r = this.path.get(0);
			this.path.remove(0);
			return r;
		}
		return -1;
	}

	public ArrayList<Integer> getPath() {
		return path;
	}

	public void setPath(ArrayList<Integer> path) {
		this.path.addAll(path);
	}

	public int getNextHop() {
		return nextHop;
	}

	public void setNextHop(int nextHop) {
		this.nextHop = nextHop;
	}
	
	public int getHopsToSink() {
		return hopsToSink;
	}

	public void setHopsToSink(int hopsToSink) {
		this.hopsToSink = hopsToSink;
	}

}
