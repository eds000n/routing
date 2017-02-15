package projects.SPT.nodes.timers;

import projects.SPT.nodes.nodeImplementations.SPTNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class SPTTimer extends Timer {

	private SPTNode.TNO tno;
	SPTNode n;
	public SPTTimer(SPTNode n, SPTNode.TNO tno){
		this.tno = tno;
		this.n = n;
	}
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		//	((InfraNode)getTargetNode()).timeout(tno);
		n.timeout(tno);
	}
	
	public void tnoStartRelative(double time, Node n, SPTNode.TNO tno){
		this.tno=tno;
		super.startRelative(time, n);
	}
	public void tnoStartGlobalRelative(double time, SPTNode.TNO tno){
		this.tno=tno;
		super.startGlobalTimer(time);
	}
	public void tnoStartAbsolute(double time, Node n, SPTNode.TNO tno){
		this.tno=tno;
		super.startAbsolute(time, n);
	}

}
