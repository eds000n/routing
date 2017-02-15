package projects.SPT.nodes.timers;


import sinalgo.nodes.timers.Timer;


public class EndSPTTimer extends Timer {
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		sinalgo.tools.Tools.exit();
	}
}

