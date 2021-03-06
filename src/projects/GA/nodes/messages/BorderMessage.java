package projects.GA.nodes.messages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import projects.GA.utils.GAEdge;
import sinalgo.nodes.messages.Message;

/**
 * Message that is generated by border nodes (those that do not have nextHop higher)
 * This message is sent to the SINK so that it knows the whole network.
 * @author edson
 *
 */
public class BorderMessage extends Message{
	private int SenderID;
//	private ArrayList<Integer> node1;	// List of edges, in the format e:(node1,node2) 1-indexed
//	private ArrayList<Integer> node2;	// Assuming that node1[i] < node2[i] for i=1, ..., size(node1)
//	private ArrayList<GAEdge> nodes;
	
//	private HashSet<GAEdge> nodes;

	private int nodes;		// List of edges in a binary matrix, assuming a 32 bits integer.
							// The edge (u,v) is represented as one bit in the position (1<<16*v)<<u  

	private int maxbits = 8;	// 2^8=256
	
	private int n[][] = new int[128][128];		//An array of 128x128 is enough for 4096 nodes in the network
	public BorderMessage (int SenderID){
		this.setSenderID(SenderID);
//		node1 = new ArrayList<Integer>();
//		node2 = new ArrayList<Integer>();
		
//		nodes = new ArrayList<>();
		
//		nodes = new HashSet<GAEdge>();
		nodes = 0;
	}
	
	public void addEdge(Integer n1, Integer n2){
//		if ( n1 < n2 ){
//			this.node1.add(n1);
//			this.node2.add(n2);
//		}else{
//			this.node1.add(n2);
//			this.node2.add(n1);
//		}
		
//		GAEdge e = new GAEdge(n1, n2);
//		if ( java.util.Collections.binarySearch(this.nodes, e, new Comparator<GAEdge>() {
//
//			@Override
//			public int compare(GAEdge e1, GAEdge e2) {
//				if ( e1.getU() == e2.getU() ){
//					if (e1.getV()<=e2.getV())
//						return -1;
//					else
//						return 1;
//				}else if (e1.getU()< e2.getU())
//					return -1;
//				else
//					return 1;
//			}
//		}) < 0 ){
////		if ( !this.nodes.contains(e) ){
//			this.nodes.add(e);
//			this.nodes.sort(new Comparator<GAEdge>() {
//
//				@Override
//				public int compare(GAEdge e1, GAEdge e2) {
//					if ( e1.getU() == e2.getU() ){
//						if (e1.getV()<=e2.getV())
//							return -1;
//						else
//							return 1;
//					}else if (e1.getU()< e2.getU())
//						return -1;
//					else
//						return 1;
//				}
//			});
//		}
		
//		GAEdge e = new GAEdge(n1, n2);
//		this.nodes.add(e);
		
		this.nodes |= setBits(n1, n2);
	}
	
	private int setBits(int col, int row){
		int r = (1<<(maxbits*row))<<col;
		return r;
	}
	
	private int getBits(int col, int row){
		return (this.nodes>>(maxbits*row))>>col & 1;
	}
	
	public void addEdges(ArrayList<Integer> n1, ArrayList<Integer> n2){ 
		for(int i=0; i<n1.size(); i++){
			addEdge(n1.get(i), n2.get(i));
		}
		
//		printTable();
		
		
	}
	
	public int getSenderID() {
		return SenderID;
	}
	
	public void setSenderID(int senderID) {
		SenderID = senderID;
	}
	
	public void printTable(){
		for(int i=0; i<(1<<maxbits); i++){
			int tmp = this.nodes >> (maxbits*i);
			System.out.print(i + ": ");
			for(int j=i+1; j<(1<<maxbits); j++){
				System.out.print(tmp>>j & 1);
			}
			System.out.println();
		}
	}
	
	public ArrayList<Integer> getNode1(){
		ArrayList<Integer> node1 = new ArrayList<>();
//		for(GAEdge e : this.nodes){
//			node1.add(e.getU());
//		}
		
//		for(int i=0; i<(1<<maxbits); i++){
//			for(int j=i+1; j<(1<<maxbits); j++){
//				if ( getBits(j,i) == 1){
//					node1.add(i+1);
//				}
//			}
//		}
		
		return node1;
//		return this.node1;
	}
	
	public ArrayList<Integer> getNode2(){
		ArrayList<Integer> node2 = new ArrayList<>();
//		for(GAEdge e : this.nodes){
//			node2.add(e.getV());
//		}
		
//		for(int i=0; i<(1<<maxbits); i++){
//			for(int j=i+1; j<(1<<maxbits); j++){
//				if ( getBits(j,i) == 1){
//					node2.add(j+1);
//				}
//			}
//		}
		
		return node2;
//		return this.node2;
	}
	
	@Override
	public Message clone() {
		BorderMessage msg = new BorderMessage(SenderID);
//		msg.node1.addAll(node1);
//		msg.node2.addAll(node2);
		
//		msg.nodes.addAll(nodes);
		
		msg.nodes = nodes;
		return msg;
	}

	@Override
	public String toString() {
		return "BorderMessage [SenderID=" + SenderID + ", nodes=" + nodes + "]";
	}
	
}
