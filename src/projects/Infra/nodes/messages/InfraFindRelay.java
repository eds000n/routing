package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraFindRelay extends Message {
	private int source;
	
	

	public InfraFindRelay(int source) {
		super();
		this.source = source;
	}



	public int getSource() {
		return source;
	}



	public void setSource(int source) {
		this.source = source;
	}



	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
