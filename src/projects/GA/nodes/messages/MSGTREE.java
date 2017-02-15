package projects.GA.nodes.messages;

import sinalgo.nodes.messages.Message;

public class MSGTREE extends Message {
	private int disttree;
	private int nexthop;
	private int Sentnode;
	private String dest;
	
	
	
	public MSGTREE(int disttree, int nexthop, int sentnode) {
		super();
		this.disttree = disttree;
		this.nexthop = nexthop;
		this.Sentnode = sentnode;
	}




	public String getDest() {
		return dest;
	}




	public void setDest(String dest) {
		this.dest = dest;
	}




	public MSGTREE(int disttree, int nexthop, int sentnode, String dest) {
		super();
		this.disttree = disttree;
		this.nexthop = nexthop;
		Sentnode = sentnode;
		this.dest = dest;
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
