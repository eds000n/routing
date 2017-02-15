package projects.GA.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import projects.GA.nodes.nodeImplementations.GANode;
import sinalgo.nodes.Node;

public class PrintResult {

	public static Map<Node, LinkedList<Integer>> mapNodes = new HashMap<Node, LinkedList<Integer>>();

	public static List<String> arestas = new ArrayList<String>();

	public static void print() {
		int peso = 1;
		inputAresta();
		try {
			FileWriter arq = new FileWriter( "srcAG/log.dat" );
			PrintWriter gravarArq = new PrintWriter( arq );

			gravarArq.print( mapNodes.keySet().size() + " " + arestas.size() );
			gravarArq.println();

			for ( Entry<Node, LinkedList<Integer>> item : mapNodes.entrySet() ) {

				GANode node = (GANode) item.getKey();
				gravarArq.print( node.ID + " " + node.generateEvent + " " + node.getPosition().xCoord + " " + node.getPosition().yCoord + " " + node.getBattery().getEnergy() );
				gravarArq.println();

			}

			gravarArq.println();
			for ( String a : arestas ) {
				gravarArq.print( a + " " + peso );
				gravarArq.println();
			}
			arq.close();

		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}

	private static void inputAresta() {
		for ( Entry<Node, LinkedList<Integer>> item : mapNodes.entrySet() ) {

			Node node = item.getKey();

			for ( Integer v : item.getValue() ) {
				if ( v != node.ID ) {

					String AB = String.valueOf( node.ID ) + "-" + String.valueOf( v );
					String BA = String.valueOf( v ) + "-" + String.valueOf( node.ID );

					if ( !arestas.contains( AB ) && !arestas.contains( BA ) ) {
						arestas.add( AB );
					}

				}
			}

		}
	}

}
