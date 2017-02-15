package projects.GA.nodes.timers;

import projects.GA.nodes.messages.SetRouteMessage;
import projects.GA.utils.GATree;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class SetRouteTimer extends Timer{
	SetRouteMessage msg;
	Node sendingNode;
	public SetRouteTimer(Node n, SetRouteMessage setRouteMessage) {
		msg = setRouteMessage;
		sendingNode = n;
	}

	@Override
	public void fire() {
		for (GATree t : msg.trees){
			sendingNode.send(msg, Tools.getNodeByID(t.root));
		}
		
	}

}
