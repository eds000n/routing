package projects.GA.nodes.timers;

import projects.GA.nodes.nodeImplementations.GANode;
import sinalgo.nodes.timers.Timer;

public class VerifyBorderNodeTimer extends Timer{

	GANode node;
	
	public VerifyBorderNodeTimer(GANode node){
		this.node = node;
	}

	@Override
	public void fire() {
		
		this.node.SetNeighborHopBigger();
	}
}