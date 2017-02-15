package projects.SPT.nodes.timers;

import projects.SPT.nodes.nodeImplementations.SPTNode;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class GraphConnectivityTimer extends Timer {

	SPTNode node;
	public GraphConnectivityTimer(SPTNode node){
		this.node = node;		
	}
	@Override
	public void fire() {
		if (!this.node.isGraphConnected())
			Tools.exit();
	}
	

}
