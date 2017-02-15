package projects.Infra.nodes.timers;

import projects.Infra.nodes.nodeImplementations.InfraNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class InfraTimer extends Timer {
	private InfraNode.TNO tno;
	InfraNode n;
	public InfraTimer(InfraNode n, InfraNode.TNO tno){
		this.tno = tno;
		this.n = n;
	}
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
//		((InfraNode)getTargetNode()).timeout(tno);
		n.timeout(tno);

	}  
	public void tnoStartRelative(double time, Node n, InfraNode.TNO tno){
		this.tno=tno;
		super.startRelative(time, n);
	}
	public void tnoStartGlobalRelative(double time, InfraNode.TNO tno){
		this.tno=tno;
		super.startGlobalTimer(time);
	}
	public void tnoStartAbsolute(double time, Node n, InfraNode.TNO tno){
		this.tno=tno;
		super.startAbsolute(time, n);
	}
}
