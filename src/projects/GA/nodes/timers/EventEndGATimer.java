package projects.GA.nodes.timers;

import projects.GA.nodes.messages.RemoveMessage;
import projects.GA.nodes.nodeImplementations.GANode;
import projects.GA.nodes.nodeImplementations.GANode.Roles;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class EventEndGATimer extends Timer {
	int eventID=-1;
	Node n;
	
	public EventEndGATimer(int eventID, Node n) {
		super();
		this.n = n;
		this.eventID = eventID;
	}
	
	@Override
	public void fire() {
		if (((GANode)this.n).getEventnum()==this.eventID){
			Tools.appendToOutput("The node " + n.ID + " detected the event " + this.eventID + "\n");
			//Tools.appendToOutput("Amount of timers for this node: " + this.n.getTimers().size()+"\n");
			((GANode)this.n).setMyRole(Roles.RELAY);
			((GANode)this.n).RemoveNode(this.n.ID);
			//RemoveMessage rm = new RemoveMessage(this.n.ID);
			//this.n.send(rm, this.n.Ne);
		}	
	}

}
