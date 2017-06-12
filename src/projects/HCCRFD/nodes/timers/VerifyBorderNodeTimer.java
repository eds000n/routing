package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import sinalgo.nodes.timers.Timer;

public class VerifyBorderNodeTimer extends Timer{

	HCCRFDNode node;
	
	public VerifyBorderNodeTimer(HCCRFDNode node){
		this.node = node;
	}

	@Override
	public void fire() {
		
		this.node.SetNeighborHopBigger();
	}
}
