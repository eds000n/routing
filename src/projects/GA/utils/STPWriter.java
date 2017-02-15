package projects.GA.utils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import projects.GA.nodes.nodeImplementations.GANode;
import sinalgo.tools.Tools;

public class STPWriter {
	int obj; //1: normal STP, 2: node-weighted STP, 3: normalized node-weightd
	String filename;
	ArrayList<Double> nodesWeights;
	ArrayList<STPEdge> edges;
	ArrayList<Integer> terminals;
	
	public STPWriter(String filename, int obj){
		this.filename = filename;
		this.obj = obj;
	}
	
	/**
	 * method to pass from the brkga input to the STP standard input
	 * @param nodes 1-indexed
	 * @param edges 1-indexed
	 */
	public void setInput(HashMap<Integer, Vertex> nodes, List<String> edges) {
		this.nodesWeights = new ArrayList<Double>(Collections.nCopies(nodes.size(), 0.));
		this.edges = new ArrayList<STPEdge>();
		this.terminals = new ArrayList<Integer>();
//		for (Vertex v : nodes){
		for (Integer key : nodes.keySet()){
			Vertex v = nodes.get(key);
			if ( GANode.UpNodes.get(v.ID-1)==1 ){
				//nodesWeights.set(v.ID - 1, (double) ((GANode)Tools.getNodeByID(v.ID)).getBattery().getTotalSpentEnergy());
				nodesWeights.set(v.ID - 1, v.battery);
				if (v.terminal==1)
					this.terminals.add(v.ID);
			}
			
		}
		
		for(String e : edges){
			String uv[] = e.split("-");
			String v[] = uv[1].split(" ");
			this.edges.add(new STPEdge(Integer.parseInt(uv[0]), Integer.parseInt(v[0]), 1.0));
		}
	}
	
	public void write() throws IOException{
		FileWriter file = new FileWriter( this.filename );
		PrintWriter printFile = new PrintWriter( file );
		writeComment(printFile);
		writeGraph(printFile);
		writeTerminals(printFile);
		if (this.obj==2 || this.obj==3){
			writeNodeWeights(printFile);
		}
		
		printFile.println("EOF");
		printFile.close();
		file.close();
	}

	private void writeComment(PrintWriter pf){
		pf.println("33D32945 STP File, STP Format Version 1.0");
		pf.println("SECTION Comment");
		if (this.obj == 1)
			pf.println("Name    \"Routing at WSNs\"");
		else if (this.obj == 2)
			pf.println("Name    \"Routing at WSNs considering energy usage\"");
		else if (this.obj == 3)
			pf.println("Name    \"Routing at WSNs normalized node-weighted\"");
		pf.println("Creator \"Edson Ticona Zegarra\"");
		pf.println("Remark  \"Random instances considering nodes as sensors\"");   
		pf.println("END");
		pf.println();
	}
	
	private void writeGraph(PrintWriter pf) {
		pf.println("SECTION Graph");
		pf.println("Nodes " + nodesWeights.size());
		pf.println("Edges " + edges.size());
		for(STPEdge e: edges)
			pf.println("E " + e.u + " " + e.v + " " + e.w);
		pf.println("END");
		pf.println();
	}

	private void writeTerminals(PrintWriter pf) {
		pf.println("SECTION Terminals");
		pf.println("Terminals " + terminals.size());
		for(Integer t: terminals)
			pf.println("T "+ t);
		pf.println("END");
		pf.println();
	}
	
	private void writeNodeWeights(PrintWriter pf) {
		pf.println("SECTION NodeWeights");
		for(Double w: nodesWeights)
			pf.println("NW " + w);
		pf.println("END");
		pf.println();
	}


}
