package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;


public class InfraRelayResponde extends Message {
	private int Source;
	private int ID;
	private int HopToSink;
	
	
	
	public InfraRelayResponde(int id, int source, int hopToSink) {
		super();
		ID = id;
		Source = source;
		HopToSink = hopToSink;
	}


	public int getID() {
		return ID;
	}



	public void setID(int id) {
		ID = id;
	}



	public int getSource() {
		return Source;
	}



	public void setSource(int source) {
		Source = source;
	}



	public int getHopToSink() {
		return HopToSink;
	}



	public void setHopToSink(int hopToSink) {
		HopToSink = hopToSink;
	}



	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}

}
