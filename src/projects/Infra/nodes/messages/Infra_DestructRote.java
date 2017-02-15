package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class Infra_DestructRote extends Message {
	 public int NextHop;
	 public int EventNum;

	 
	 
	public Infra_DestructRote(int nextHop, int eventNum) {
		super();
		NextHop = nextHop;
		EventNum = eventNum;
	}



	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
