package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class EventERATimer extends Timer {

	int eventID=-1;
	
	public EventERATimer(int eventID) {
		super();
		this.eventID = eventID;
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		if(((ERANode)getTargetNode()).insideEvent(getTargetNode().getPosition(),this.eventID)){
			ERANode.terminals.add(getTargetNode().ID-1);
			((ERANode)getTargetNode()).startDetection();
		}
			
	}
	public void startEventAbsolute(double absoluteTime, Node n, int eventID){
		this.eventID = eventID;
		startAbsolute(absoluteTime, n);
		
	}

}
