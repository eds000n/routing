package projects.GA.nodes.messages;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class MCIMessage extends Message {
	public int HopToSink;
	public int SenderID;
	//private int densidadeEstimada;
	//private int numberOfNodes;
	//private Map<Node, LinkedList<Integer>> mapNeibohrs = new HashMap<Node, LinkedList<Integer>>();

	
	public MCIMessage(int hopToSink, int senderID) {
		HopToSink = hopToSink;
		SenderID = senderID;
	}
	
	/*public MCIMessage(int hopToSink, int senderID, Map<Node, LinkedList<Integer>> mapNeibohrs) {
		HopToSink = hopToSink;
		SenderID = senderID;
		this.mapNeibohrs = mapNeibohrs;
	}*/

	/*public MCIMessage(int hopToSink, int senderID, int numberOfNodes) {
		this.HopToSink = hopToSink;
		this.SenderID = senderID;
		this.numberOfNodes = numberOfNodes;
	}*/

	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		//return new MCIMessage(this.HopToSink,this.SenderID, this.numberOfNodes);
		return new MCIMessage(this.HopToSink,this.SenderID);
	}


	public int getHopToSink() {
		return HopToSink;
	}


	public void setHopToSink(int hopToSink) {
		HopToSink = hopToSink;
	}


	public int getSenderID() {
		return SenderID;
	}


	public void setSenderID(int senderID) {
		SenderID = senderID;
	}

	/*public Map<Node, LinkedList<Integer>> getMapNeibohrs() {
		return mapNeibohrs;
	}

	public void setMapNeibohrs(Map<Node, LinkedList<Integer>> mapNeibohrs) {
		this.mapNeibohrs = mapNeibohrs;
	}

	public int getDensidadeEstimada() {
		return densidadeEstimada;
	}

	public void setDensidadeEstimada(int densidadeEstimada) {
		this.densidadeEstimada = densidadeEstimada;
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public void setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
	}*/
	
	

}
