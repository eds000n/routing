package projects.DDAARP.nodes.timers;

import projects.DDAARP.nodes.nodeImplementations.DDAARPNode;
import sinalgo.nodes.timers.Timer;

public class VerifyBorderNodeTimer extends Timer {

	DDAARPNode node;
	
	public VerifyBorderNodeTimer(DDAARPNode node){
		this.node = node;
	}
	@Override
	public void fire() {
		this.node.SetNeighborHopBigger();
	}

}
