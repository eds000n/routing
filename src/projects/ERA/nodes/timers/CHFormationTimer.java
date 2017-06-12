package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import projects.ERA.nodes.nodeImplementations.ERANode.Roles;
import sinalgo.nodes.timers.Timer;

public class CHFormationTimer extends Timer{

	ERANode node;
	public CHFormationTimer(ERANode n){
		this.node = n;
		//getTargetNode();
	}
	@Override
	public void fire() {
		if ( node.getMyRole() == Roles.COLLABORATOR )	//Only for non-CH nodes
			node.ClusterFormation();
	}

}
