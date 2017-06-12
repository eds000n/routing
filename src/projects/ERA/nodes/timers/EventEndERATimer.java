package projects.ERA.nodes.timers;

import projects.ERA.nodes.nodeImplementations.ERANode;
import projects.ERA.nodes.nodeImplementations.ERANode.Roles;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class EventEndERATimer extends Timer {

	int eventID=-1;
	Node n;
	public EventEndERATimer(int eventID, Node n) {
		super();
		this.n = n;
		this.eventID = eventID;
	}
	@Override
	public void fire() {
		if (((ERANode)this.n).getEventnum()==this.eventID){
			Tools.appendToOutput("The node " + n.ID + " detected the event " + this.eventID + "\n");
			Tools.appendToOutput("Amount of timers for this node: " + this.n.getTimers().size()+"\n");
			((ERANode)this.n).setMyRole(Roles.RELAY);
			/*for (Timer t:this.n.getTimers()){
				
				if (t instanceof ERATimer){
					t = null;
				}
			}*/
		}
	}
	
}
