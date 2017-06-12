package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class RepairDeadNodeHCCRFDTimer extends Timer {
	Node n;
	
	public RepairDeadNodeHCCRFDTimer(Node n) {
		super();
		this.n = n;
	}

	@Override
	public void fire() {
		((HCCRFDNode)this.n).broadcastDeadNodeRepairMessage(n.ID);
	}

}
