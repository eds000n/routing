package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class ERATimer extends Timer {

	private ERANode.TNO tno;
	ERANode n;
	public ERATimer(ERANode n, ERANode.TNO tno){
		this.tno = tno;
		this.n = n;
	}
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		//	((InfraNode)getTargetNode()).timeout(tno);
		n.timeout(tno);
	}
	
	public void tnoStartRelative(double time, Node n, ERANode.TNO tno){
		this.tno=tno;
		super.startRelative(time, n);
	}
	public void tnoStartGlobalRelative(double time, ERANode.TNO tno){
		this.tno=tno;
		super.startGlobalTimer(time);
	}
	public void tnoStartAbsolute(double time, Node n, ERANode.TNO tno){
		this.tno=tno;
		super.startAbsolute(time, n);
	}

}
