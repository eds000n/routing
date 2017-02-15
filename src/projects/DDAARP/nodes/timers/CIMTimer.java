package projects.DDAARP.nodes.timers;

import projects.DDAARP.nodes.nodeImplementations.DDAARPNode;
import sinalgo.nodes.timers.Timer;

public class CIMTimer extends Timer{

	DDAARPNode n;
	
	public CIMTimer(DDAARPNode n){
		this.n = n;
	}
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		n.StartCollectingInformation();
	}

}
