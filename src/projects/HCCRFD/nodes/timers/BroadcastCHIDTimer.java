package projects.HCCRFD.nodes.timers;

import projects.HCCRFD.nodes.messages.MessageBroadcastCHID;
import sinalgo.nodes.timers.Timer;

public class BroadcastCHIDTimer extends Timer{

	MessageBroadcastCHID mb;
	public BroadcastCHIDTimer(MessageBroadcastCHID mb){
		this.mb = mb;
	}
	
	@Override
	public void fire() {
		getTargetNode().broadcast(mb);
	}

}
