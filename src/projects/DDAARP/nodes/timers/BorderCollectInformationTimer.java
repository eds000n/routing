package projects.DDAARP.nodes.timers;

import projects.DDAARP.nodes.nodeImplementations.DDAARPNode;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

public class BorderCollectInformationTimer extends Timer {

	DDAARPNode node;
	Message msg;
	public BorderCollectInformationTimer(Message msg, DDAARPNode node){
		this.node = node;
		this.msg = msg;
	}
	@Override
	public void fire() {
		node.broadcast(msg);
	}

}
