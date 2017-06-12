package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class HCCRFDTimer extends Timer {

	private HCCRFDNode.TNO tno;
	HCCRFDNode n;
	public HCCRFDTimer(HCCRFDNode n, HCCRFDNode.TNO tno){
		this.tno = tno;
		this.n = n;
	}
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
//		((InfraNode)getTargetNode()).timeout(tno);
		n.timeout(tno);
		}
	
	public void tnoStartRelative(double time, Node n, HCCRFDNode.TNO tno){
		this.tno=tno;
		super.startRelative(time, n);
	}
	public void tnoStartGlobalRelative(double time, HCCRFDNode.TNO tno){
		this.tno=tno;
		super.startGlobalTimer(time);
	}
	public void tnoStartAbsolute(double time, Node n, HCCRFDNode.TNO tno){
		this.tno=tno;
		super.startAbsolute(time, n);
	}

}
