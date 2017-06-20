package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
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
		if(((HCCRFDNode)getTargetNode()).insideEvent(getTargetNode().getPosition(),this.eventID)){
			((HCCRFDNode)getTargetNode()).generateEvent = 1;
			((HCCRFDNode)getTargetNode()).startDetection();
			HCCRFDNode.terminals.add(getTargetNode().ID -1);
		}
	}
	public void startEventAbsolute(double absoluteTime, Node n, int eventID){
		this.eventID = eventID;
		startAbsolute(absoluteTime, n);
		
	}

}
