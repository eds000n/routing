package projects.Infra.nodes.messages;

import sinalgo.nodes.messages.Message;

public class InfraWantRelay extends Message {
	private int Source;
	private int HopToSink;
	
	
	public InfraWantRelay(int source, int hopToSink) {
		super();
		Source = source;
		HopToSink = hopToSink;
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
