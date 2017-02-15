package projects.Infra.nodes.timers;


import sinalgo.nodes.timers.Timer;


public class EndTimer extends Timer {
	@Override
	public void fire() {
		// TODO Auto-generated method stub
		sinalgo.tools.Tools.exit();
	}
}

