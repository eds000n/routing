package projects.SPT.nodes.timers;

import projects.SPT.nodes.nodeImplementations.SPTNode;
import sinalgo.nodes.timers.Timer;

public class RepairDeadNodeSPTTimer extends Timer {

	SPTNode n;
	
	public RepairDeadNodeSPTTimer (SPTNode n){
		super();
		this.n = n;
	}
	@Override
	public void fire() {
		n.broadcastDeadNodeRepairMessage(n.ID);
	}

}
