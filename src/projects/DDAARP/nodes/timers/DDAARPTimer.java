package projects.DDAARP.nodes.timers;

import projects.DDAARP.nodes.nodeImplementations.DDAARPNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class DDAARPTimer extends Timer {

	private DDAARPNode.TNO tno;
	DDAARPNode n;
	public DDAARPTimer(DDAARPNode n, DDAARPNode.TNO tno){
		this.tno = tno;
		this.n = n;
	}
	
	
	@Override
	public void fire() {
		// TODO Auto-generated method stub
//		((InfraNode)getTargetNode()).timeout(tno);
		n.timeout(tno);
		}
	
	public void tnoStartRelative(double time, Node n, DDAARPNode.TNO tno){
		this.tno=tno;
		super.startRelative(time, n);
	}
	public void tnoStartGlobalRelative(double time, DDAARPNode.TNO tno){
		this.tno=tno;
		super.startGlobalTimer(time);
	}
	public void tnoStartAbsolute(double time, Node n, DDAARPNode.TNO tno){
		this.tno=tno;
		super.startAbsolute(time, n);
	}

}
