package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import sinalgo.nodes.timers.Timer;

public class ClusterFormationTimer extends Timer{

	@Override
	public void fire() {
		((HCCRFDNode)getTargetNode()).clusterFormation();
	}

}
