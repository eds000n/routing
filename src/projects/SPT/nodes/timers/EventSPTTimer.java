package projects.SPT.nodes.timers;

import projects.SPT.nodes.nodeImplementations.SPTNode;
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
		if(((SPTNode)getTargetNode()).insideEvent(getTargetNode().getPosition(),this.eventID)){
			SPTNode.terminals.add(getTargetNode().ID-1);
			((SPTNode)getTargetNode()).startDetection();
		}
			
	}
	public void startEventAbsolute(double absoluteTime, Node n, int eventID){
		this.eventID = eventID;
		startAbsolute(absoluteTime, n);
		
	}

}