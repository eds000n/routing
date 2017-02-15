package projects.DAARPMSWIM.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DAARPMSWIM_DestructRote extends Message {
    public int NextHop;
    public int EventNum;
    
    
	public DAARPMSWIM_DestructRote(int nextHop, int eventNum) {
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

	
