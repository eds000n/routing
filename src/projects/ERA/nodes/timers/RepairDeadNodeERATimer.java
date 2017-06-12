package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import sinalgo.nodes.timers.Timer;

public class RepairDeadNodeERATimer extends Timer {

	ERANode n;
	
	public RepairDeadNodeERATimer (ERANode n){
		super();
		this.n = n;
	}
	@Override
	public void fire() {
		n.broadcastDeadNodeRepairMessage(n.ID);
	}

}
