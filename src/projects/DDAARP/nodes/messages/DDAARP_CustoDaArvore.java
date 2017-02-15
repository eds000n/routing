package projects.DDAARP.nodes.messages;

import sinalgo.nodes.messages.Message;

public class DDAARP_CustoDaArvore extends Message {
	private int disttree;
	private int nexthop;
	private int Sentnode;
	
	
	
	public DDAARP_CustoDaArvore(int disttree, int nexthop, int sentnode) {
		super();
		this.disttree = disttree;
		this.nexthop = nexthop;
		this.Sentnode = sentnode;
	}




	@Override
	public Message clone() {
		// TODO Auto-generated method stub
		return this;
	}




	public int getDisttree() {
		return disttree;
	}




	public void setDisttree(int disttree) {
		this.disttree = disttree;
	}




	public int getNexthop() {
		return nexthop;
	}




	public void setNexthop(int nexthop) {
		this.nexthop = nexthop;
	}




	public int getSentnode() {
		return Sentnode;
	}




	public void setSentnode(int sentnode) {
		Sentnode = sentnode;
	}



}
