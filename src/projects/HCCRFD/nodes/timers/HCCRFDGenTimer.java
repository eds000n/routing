package projects.HCCRFD.nodes.timers;

import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

public class HCCRFDGenTimer extends Timer{

	Message m;
	
	public HCCRFDGenTimer(Message m){
		this.m = m;
	}
	@Override
	public void fire() {
		getTargetNode().broadcast(m);
	}

}
