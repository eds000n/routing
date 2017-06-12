package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class CHSelectionTimer extends Timer{
	
	int nodeID;
	
	public CHSelectionTimer(int id){
		this.nodeID = id;
	}
	
	@Override
	public void fire() {
		((ERANode)Tools.getNodeByID(nodeID)).CHSelection();
	}

}
