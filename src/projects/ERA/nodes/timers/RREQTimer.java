package projects.ERA.nodes.timers;

import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

public class RREQTimer extends Timer{

	int ID;
	Message m;
	public RREQTimer(int id, Message m){
		this.ID = id;
		this.m = m;
	}
	
	@Override
	public void fire() {
		Tools.getNodeByID(ID).broadcast(m);
	}

}
