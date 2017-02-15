package projects.DAARPMSWIM.nodes.timers;

import projects.DAARPMSWIM.nodes.nodeImplementations.DAARPMSWIMNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class EventLeoTimer extends Timer {

	int eventID=-1;
	
	public EventLeoTimer(int eventID) {
		super();
		this.eventID = eventID;
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		if(((DAARPMSWIMNode)getTargetNode()).insideEvent(getTargetNode().getPosition(),this.eventID))
			((DAARPMSWIMNode)getTargetNode()).startDetection();
	}
	public void startEventAbsolute(double absoluteTime, Node n, int eventID){
		this.eventID = eventID;
		startAbsolute(absoluteTime, n);
		
	}

}
