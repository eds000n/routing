package projects.DAARPMSWIM.nodes.timers;

import projects.DAARPMSWIM.nodes.nodeImplementations.DAARPMSWIMNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class DAARPMSWIMTimer extends Timer {

	private DAARPMSWIMNode.TNO tno;
	DAARPMSWIMNode n;
	public DAARPMSWIMTimer(DAARPMSWIMNode n, DAARPMSWIMNode.TNO tno){
		this.tno = tno;
		this.n = n;
	}
	
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
//		((InfraNode)getTargetNode()).timeout(tno);
		n.timeout(tno);
		}
	
	public void tnoStartRelative(double time, Node n, DAARPMSWIMNode.TNO tno){
		this.tno=tno;
		super.startRelative(time, n);
	}
	public void tnoStartGlobalRelative(double time, DAARPMSWIMNode.TNO tno){
		this.tno=tno;
		super.startGlobalTimer(time);
	}
	public void tnoStartAbsolute(double time, Node n, DAARPMSWIMNode.TNO tno){
		this.tno=tno;
		super.startAbsolute(time, n);
	}

}
