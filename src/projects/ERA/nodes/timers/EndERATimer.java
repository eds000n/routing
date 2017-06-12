package projects.ERA.nodes.timers;


import sinalgo.nodes.timers.Timer;


public class EndERATimer extends Timer {
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		sinalgo.tools.Tools.exit();
	}
}

