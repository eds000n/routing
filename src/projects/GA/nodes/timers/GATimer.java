package projects.GA.nodes.timers;

import projects.GA.nodes.nodeImplementations.GANode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class GATimer extends Timer {

	private GANode.TNO tno;
	GANode n;
	public GATimer(GANode n, GANode.TNO tno){
		this.tno = tno;
		this.n = n;
	}
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
//		((InfraNode)getTargetNode()).timeout(tno);
		n.timeout(tno);
		}
	
	public void tnoStartRelative(double time, Node n, GANode.TNO tno){
		this.tno=tno;
		super.startRelative(time, n);
	}
	public void tnoStartGlobalRelative(double time, GANode.TNO tno){
		this.tno=tno;
		super.startGlobalTimer(time);
	}
	public void tnoStartAbsolute(double time, Node n, GANode.TNO tno){
		this.tno=tno;
		super.startAbsolute(time, n);
	}

}
