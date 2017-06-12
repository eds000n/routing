package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import sinalgo.nodes.timers.Timer;

public class GenConfigurationTimer extends Timer {

	@Override
	public void fire() {
		((HCCRFDNode)getTargetNode()).ReconfigHCCRFD();
	}

}
