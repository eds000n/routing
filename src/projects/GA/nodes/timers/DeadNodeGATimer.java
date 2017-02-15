package projects.GA.nodes.timers;

import projects.GA.nodes.nodeImplementations.GANode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class DeadNodeGATimer extends Timer {

	Node n;
	
	public DeadNodeGATimer(Node n) {
		super();
		this.n = n;
	}
	
	@Override
	public void fire() {
		((GANode)this.n).sendDeadNodeMessage(n.ID);
	}

}
