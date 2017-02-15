package projects.GA.nodes.timers;

import projects.GA.nodes.nodeImplementations.GANode;
import sinalgo.nodes.timers.Timer;

public class BorderInitNodeTimer extends Timer {

	GANode node;
	
	public BorderInitNodeTimer(GANode node){
		this.node = node;
	}
	
	@Override
	public void fire() {
		this.node.StartCollectingInformation();
	}

}
