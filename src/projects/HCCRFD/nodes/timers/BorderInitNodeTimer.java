package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import sinalgo.nodes.timers.Timer;

public class BorderInitNodeTimer extends Timer {

	HCCRFDNode node;
	
	public BorderInitNodeTimer(HCCRFDNode node){
		this.node = node;
	}
	
	@Override
	public void fire() {
		this.node.StartCollectingInformation();
	}

}
