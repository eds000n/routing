package projects.SPT.nodes.timers;

import projects.SPT.nodes.nodeImplementations.SPTNode;
import projects.SPT.nodes.nodeImplementations.SPTNode.Roles;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class EventEndSPTTimer extends Timer {

	int eventID=-1;
	Node n;
	public EventEndSPTTimer(int eventID, Node n) {
		super();
		this.n = n;
		this.eventID = eventID;
	}
	@Override
	public void fire() {
		if (((SPTNode)this.n).getEventnum()==this.eventID){
			Tools.appendToOutput("The node " + n.ID + " detected the event " + this.eventID + "\n");
			Tools.appendToOutput("Amount of timers for this node: " + this.n.getTimers().size()+"\n");
			((SPTNode)this.n).setMyRole(Roles.RELAY);
			/*for (Timer t:this.n.getTimers()){
				
				if (t instanceof SPTTimer){
					t = null;
				}
			}*/
		}
	}
	
}
