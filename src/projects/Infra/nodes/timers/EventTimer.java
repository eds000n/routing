package projects.Infra.nodes.timers;

import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;
import projects.Infra.nodes.nodeImplementations.InfraNode;

public class EventTimer extends Timer {
	int eventID=-1;
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		if(((InfraNode)getTargetNode()).insideEvent(getTargetNode().getPosition(),this.eventID))
			((InfraNode)getTargetNode()).startDetection();
	}
	public void startEventAbsolute(double absoluteTime, Node n, int eventID){
		this.eventID = eventID;
		startAbsolute(absoluteTime, n);
		
	}
}

