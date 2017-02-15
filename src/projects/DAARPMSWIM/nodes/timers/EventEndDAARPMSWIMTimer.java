package projects.DAARPMSWIM.nodes.timers;

import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class EventEndDAARPMSWIMTimer extends Timer {

	private int eventID;
	Node n;

	public EventEndDAARPMSWIMTimer(int eventID, Node n) {
		super();
		this.n = n;
		this.eventID = eventID;
	}

	@Override
	public void fire() {
		// TODO Auto-generated method stub
		
	}

}
