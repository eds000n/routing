package projects.HCCRFD.nodes.timers;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

public class BorderCollectInformationTimer extends Timer{
	Node node;
	Message msg;
	public BorderCollectInformationTimer(Message msg, Node n) {
		this.node = n;
		this.msg = msg;
	}
	@Override
	public void fire() {
		node.broadcast(msg);
	}

}
