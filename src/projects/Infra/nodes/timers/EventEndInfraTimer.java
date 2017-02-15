package projects.Infra.nodes.timers;

import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class EventEndInfraTimer extends Timer {

	int eventID;
	Node n;
	public EventEndInfraTimer(int eventID, Node n) {
		super();
		this.n = n;
		this.eventID = eventID;
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		
	}

}
