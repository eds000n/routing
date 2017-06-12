package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import sinalgo.nodes.timers.Timer;

public class CreateAdjListTimer extends Timer{

	ERANode n;
	public CreateAdjListTimer(ERANode n){
		this.n = n;
	}
	@Override
	public void fire() {
		this.n.createAdjacencyMatrix();
	}

}
