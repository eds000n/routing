package projects.DDAARP.nodes.timers;


import sinalgo.nodes.timers.Timer;


public class EndLeoTimer extends Timer {
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		sinalgo.tools.Tools.exit();
	}
}

