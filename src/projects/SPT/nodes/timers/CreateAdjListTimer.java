package projects.SPT.nodes.timers;

import projects.SPT.nodes.nodeImplementations.SPTNode;
import sinalgo.nodes.timers.Timer;

public class CreateAdjListTimer extends Timer{

	SPTNode n;
	public CreateAdjListTimer(SPTNode n){
		this.n = n;
	}
	@Override
	public void fire() {
		this.n.createAdjacencyMatrix();
	}

}
