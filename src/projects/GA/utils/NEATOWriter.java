package projects.GA.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import projects.GA.nodes.nodeImplementations.GANode;

public class NEATOWriter {
	String filename;
	ArrayList<Vertex> nodes;
	ArrayList<STPEdge> edges;
	float maxBattery;
	
	public NEATOWriter(String filename, float maxBattery){
		this.filename = filename;
		this.maxBattery = maxBattery;
	}
	
	public void write() throws IOException{
		FileWriter file = new FileWriter( this.filename );
		PrintWriter printFile = new PrintWriter( file );
		printFile.println("graph G{");
		printFile.println("overlap = true");
//		printFile.println("overlap = scale");
//		printFile.println("overlap = false");
		for(Vertex n:nodes){
			String nodestr = "v" + n.ID + " [fixedsize=true, height=6, width=8, fontsize=98, ";
			
			
			if (n.ID==1) //root
				nodestr +="style=filled, color=blue4, ";
			else{
				if (n.terminal==1)
					nodestr +="shape=box, style=filled, color=firebrick4, fontcolor=white, ";
				else{
					nodestr +="shape=circle, ";
				
					int color = (int) (n.battery * 256 / this.maxBattery);
					if (color > 256){
						color = 256;
						System.out.println("NeatoWriter.write(): warning, battery depleted: " + n.ID + "(" + n.battery+ ")");
					}
						
					nodestr += "style=filled, color=\"#" + 
							String.format("%02x", 256-color) + 
							String.format("%02x", 256-color) + 
							String.format("%02x", 256-color) + 
							"ff\", fontcolor=\"#"	+ 
							String.format("%02x", color) + 
							String.format("%02x", color) + 
							String.format("%02x", color) + "ff\", ";
				}
			}
				
			
			nodestr += "label=\""+ String.format("%4.2f", n.battery) +"/"+n.ID+"\", " ;
			
			nodestr += "pos=\"" + n.x+ "," + n.y +"!\"";
			nodestr +="];";
				
			printFile.println(nodestr);
		}
		
		for (STPEdge edge:edges){
			String edgestr = "v"+edge.u + " -- v" + edge.v + ";";
			printFile.println(edgestr);
		}
		
		printFile.println("}");
		printFile.close();
		file.close();
	}

	public void setInput(HashMap<Integer, Vertex> nodes, List<String> edges) {
		this.nodes = new ArrayList<>();
		this.edges = new ArrayList<>();
//		this.nodes.addAll(nodes);
//		for( Vertex v : nodes){
		for( Integer key : nodes.keySet()){
			Vertex v = nodes.get(key);
			if ( GANode.UpNodes.get(v.ID-1)==1 ){
				this.nodes.add(v);
			}
		}
		for(String e : edges){
			String uv[] = e.split("-");
			String v[] = uv[1].split(" ");
			this.edges.add(new STPEdge(Integer.parseInt(uv[0]), Integer.parseInt(v[0]), 1.0));
		}
	}
}
