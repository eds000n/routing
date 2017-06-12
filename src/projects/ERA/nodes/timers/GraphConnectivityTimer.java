package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class GraphConnectivityTimer extends Timer {

	ERANode node;
	public GraphConnectivityTimer(ERANode node){
		this.node = node;		
	}
	@Override
	public void fire() {
		if (!this.node.isGraphConnected())
			Tools.exit();
	}
	

}
