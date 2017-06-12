package projects.GA.nodes.timers;

import projects.GA.nodes.messages.SetRepairRouteMessage;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class RepairDeadNodeTimer extends Timer {

	Node sendingNode;
	Node destinyNode;
	SetRepairRouteMessage msg;
	public RepairDeadNodeTimer (Node sendingNode, SetRepairRouteMessage message, Node destiny){
		this.sendingNode = sendingNode;
		this.destinyNode = destiny;
		msg = message;
	}
	
	@Override
	public void fire() {
		sendingNode.send(msg, destinyNode);
	}

}
