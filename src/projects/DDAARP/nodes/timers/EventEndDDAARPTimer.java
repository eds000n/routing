package projects.DDAARP.nodes.timers;

import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class EventEndDDAARPTimer extends Timer {

	int eventID;
	Node n;
	public EventEndDDAARPTimer(int eventID, Node n) {
		super();
		this.n = n;
		this.eventID = eventID;
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		
	}

}
