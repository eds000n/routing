package projects.GA.nodes.timers;

import projects.GA.nodes.nodeImplementations.GANode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class EventSPTTimer extends Timer {

	int eventID=-1;
	
	public EventSPTTimer(int eventID) {
		super();
		this.eventID = eventID;
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		if(((GANode)getTargetNode()).insideEvent(getTargetNode().getPosition(),this.eventID)){
			((GANode)getTargetNode()).generateEvent = 1;
			((GANode)getTargetNode()).startDetection();
		}
	}
	public void startEventAbsolute(double absoluteTime, Node n, int eventID){
		this.eventID = eventID;
		startAbsolute(absoluteTime, n);
		
	}

}
