/*
 Copyright (c) 2007, Distributed Computing Group (DCG)
                    ETH Zurich
                    Switzerland
                    dcg.ethz.ch

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

 - Neither the name 'Sinalgo' nor the names of its contributors may be
   used to endorse or promote products derived from this software
   without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package projects.HCCRFD;


    
import java.awt.Rectangle;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import projects.HCCRFD.nodes.nodeImplementations.HCCRFDNode;
import projects.SPT.nodes.nodeImplementations.SPTNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.io.eps.ExportException;
import sinalgo.io.eps.Exporter;
import sinalgo.nodes.Node;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;
import sinalgo.tools.statistics.Distribution;


/**
 * This class holds customized global state and methods for the framework. 
 * The only mandatory method to overwrite is 
 * <code>hasTerminated</code>
 * <br>
 * Optional methods to override are
 * <ul>
 * <li><code>customPaint</code></li>
 * <li><code>handleEmptyEventQueue</code></li>
 * <li><code>onExit</code></li>
 * <li><code>preRun</code></li>
 * <li><code>preRound</code></li>
 * <li><code>postRound</code></li>
 * <li><code>checkProjectRequirements</code></li>
 * </ul>
 * @see sinalgo.runtime.AbstractCustomGlobal for more details.
 * <br>
 * In addition, this class also provides the possibility to extend the framework with
 * custom methods that can be called either through the menu or via a button that is
 * added to the GUI. 
 */
public class CustomGlobal extends AbstractCustomGlobal{
	
	public static double xPosition;
	
	public static double yPosition;
	
	public static Map<Node, LinkedList<Integer>> mapNodes = new HashMap<Node, LinkedList<Integer>>();

	public static List<String> arestas = new ArrayList<String>();
	
	public static int overheadsClusters = 0;
	
	public static double [] arrayTime = new double[4];

	public static boolean available = true;
	
	public static List<String> listTree = new ArrayList<String>();
	
	//public static int numTrees = 0;

	public static boolean computedOptimalTree = false; //indicates if the sink has computed an optimal tree
	/* (non-Javadoc)
	 * @see runtime.AbstractCustomGlobal#hasTerminated()
	 */
	public boolean hasTerminated() {
		return false;
	}

	/**
	 * An example of a method that will be available through the menu of the GUI.
	 */
	@AbstractCustomGlobal.GlobalMethod(menuText="Echo")
	public void echo() {
		// Query the user for an input
		String answer = JOptionPane.showInputDialog(null, "This is an example.\nType in any text to echo.");
		// Show an information message 
		JOptionPane.showMessageDialog(null, "You typed '" + answer + "'", "Example Echo", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * An example to add a button to the user interface. In this sample, the button is labeled
	 * with a text 'GO'. Alternatively, you can specify an icon that is shown on the button. See
	 * AbstractCustomGlobal.CustomButton for more details.   
	 */
	@AbstractCustomGlobal.CustomButton(buttonText="GO", toolTipText="A sample button")
	public void sampleButton() {
		JOptionPane.showMessageDialog(null, "You Pressed the 'GO' button.");
	}
	
	@Override
	public void onExit() {
		// TODO Auto-generated method stub
//		double numNodes = 0;
//		try {
//			BufferedWriter bwresult = new BufferedWriter( new FileWriter( "logEdson.txt", true ) );
////			int sim = Configuration.getIntegerParameter( "Params/sim" );
////			nInstancia = Configuration.getDoubleParameter( "Instancia" );
////			density = Configuration.getDoubleParameter( "Density" );
////			numNodes = Configuration.getDoubleParameter( "Params/NumNodes" );
//			
//			bwresult.write(SPTNode.DropRate + " mapNodes: " + this.mapNodes.size() + " listTree: " + this.listTree.size() + 
//					" listOptimized: " + this.treeOptmized.size());
//			bwresult.newLine();
//			bwresult.flush();
//			bwresult.close();
//			
//		} catch ( IOException e ) {
//			e.printStackTrace();
////		} catch (CorruptConfigurationEntryException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
//		}
		
		
		//double Rate =  (double)SPTNode.Detects / SPTNode.DataRate;
		double Rate = (double) HCCRFDNode.Notifications/HCCRFDNode.SimulationTime;
		NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en_US"));
		DecimalFormat rateFormat = (DecimalFormat) nf;
		rateFormat.applyPattern("0.00");
		
		double NotificationRate = (double)HCCRFDNode.Notifications/HCCRFDNode.EventsTimes;
		//NumberFormat nformat = NumberFormat.getCurrencyInstance(new Locale("en_US"));
		DecimalFormat notificationrateFormat = (DecimalFormat) nf;
		notificationrateFormat.applyPattern("0.00");
		
		//double Rate =  (double) 60 / SPTNode.DataRate;

		int upnodes = 0, connectednodes = 0;
		int disconnectedterminals = 0;
		for ( int i : HCCRFDNode.UpNodes )
			upnodes += i;
		for ( int i=0; i<HCCRFDNode.DisconnectedNodes.size(); i++ ){
			connectednodes += HCCRFDNode.DisconnectedNodes.get(i);
			if ( HCCRFDNode.DisconnectedNodes.get(i) == 0 && HCCRFDNode.terminals.contains(i) )
				disconnectedterminals++;
		}
		
//		if ( HCCRFDNode.Edges <= 1 ){
//			HCCRFDNode.Edges = 0;
//			for ( Node n : Tools.getNodeList() ){
//				if ( ((HCCRFDNode)n).have_retransmitted == true )
//					HCCRFDNode.Edges++;
//			}
//			HCCRFDNode.Edges += HCCRFDNode.terminals.size();
//		}
		
		
		if(HCCRFDNode.Edges>1){
			HCCRFDNode.HCCRFD.logln( 
				HCCRFDNode.EventsTimes+ "\t"
				+ HCCRFDNode.EventsAmount +"\t" //Numero de eventos (x)			     
				+ Tools.getNodeList().size()+"\t"
				+ HCCRFDNode.Recivers+"\t"
				+ HCCRFDNode.DataRate+"\t"
				+ HCCRFDNode.Overheads+"\t"
				+ HCCRFDNode.DataPackets+"\t"
				+ HCCRFDNode.Notifications+"\t"
				+ (HCCRFDNode.DataPackets- HCCRFDNode.Notifications)+"\t"
				+ HCCRFDNode.Edges / HCCRFDNode.n_rnd +"\t"			//Average tree size
				+ HCCRFDNode.EventSize + "\t"
				//+ 0+"\t"
				+ HCCRFDNode.Density+"\t"
				+ notificationrateFormat.format(NotificationRate)+"\t"
				+ ((HCCRFDNode.Overheads+HCCRFDNode.DataPackets))+"\t"
				+ "HCCRFD" + "\t"
				+ HCCRFDNode.n_rnd + "\t"
//				+ HCCRFDNode.p	+ "\t"
				//+ " TreeOptimized size " 
				//+ this.treeOptimized.size() + "\t"
//				+ "listTree size " + this.listTree.size() + "\t"
				//+ "numTrees " 
				+ upnodes + "\t"			//Number of up nodes (alive)
				+ connectednodes + "\t"			//Number of connected nodes (might be alive or dead)
				+ disconnectedterminals + "\t"			//Number of disconnected terminals (might be alive or dead) 
				+ Distribution.getSeed()
			);
			
			Iterator<Node> it = Tools.getNodeList().iterator();
			Node next;
			
			while(it.hasNext()){
				next = it.next();
						
				HCCRFDNode.Energy.logln(
					next.ID+"\t"+
					next.getPosition().xCoord+"\t"
					+next.getPosition().yCoord+"\t"
					+((HCCRFDNode)next).getBattery().getEnergy()
				);
			}
		}
		/*if(HCCRFDNode.Edges>1){
			try {
				System.out.println("LOGGING!");
				BufferedWriter bwresult = new BufferedWriter( new FileWriter( "logEdson.txt", true ) );
				String wl = HCCRFDNode.EventsTimes+ "\t"
						+ HCCRFDNode.EventsAmount +"\t" //Numero de eventos (x)			     
						+ Tools.getNodeList().size()+"\t"
						+ HCCRFDNode.Recivers+"\t"
						+ HCCRFDNode.DataRate+"\t"
						+ HCCRFDNode.Overheads+"\t"
						+ HCCRFDNode.DataPackets+"\t"
						+ HCCRFDNode.Notifications+"\t"
						+ (HCCRFDNode.DataPackets- HCCRFDNode.Notifications)+"\t"
						+ HCCRFDNode.Edges+"\t"
						+ 0+"\t"
						+ HCCRFDNode.Density+"\t"
						+ notificationrateFormat.format(NotificationRate)+"\t"
						+ ((HCCRFDNode.Overheads+HCCRFDNode.DataPackets))+"\t"
						+ "SPT" + "\t"
						//+ " TreeOptimized size " 
						+ this.treeOptimized.size() + "\t"
//						+ "listTree size " + this.listTree.size() + "\t"
						//+ "numTrees " 
						+ CustomGlobal.numTrees + "\t"
						+ sinalgo.configuration.Configuration.getIntegerParameter( "Population" ) + "\t"
						+ sinalgo.configuration.Configuration.getIntegerParameter( "Generations" );
				
				
				HCCRFDNode.HCCRFD.logln(wl);
				
				bwresult.write(wl);
				bwresult.newLine();
				
				for(int i=0; i<this.treeOptimized.size(); i++){
					bwresult.write("== "+this.treeOptimized.get(i));
					bwresult.newLine();
				}
				
				bwresult.flush();
				bwresult.close();

				Iterator<Node> it = Tools.getNodeList().iterator();
				Node next;

				while(it.hasNext()){
					next = it.next();

					HCCRFDNode.Energy.logln(
							next.getPosition().xCoord+"\t"
									+next.getPosition().yCoord
									//					+"\t"
									//					+((SPTNode)next).getBateria().getEnergy()
							);
					BufferedWriter bwresultp = new BufferedWriter( new FileWriter( "logEdson.txt", true ) );
					bwresultp.write(next.getPosition().xCoord+"\t"
							+next.getPosition().yCoord);
					bwresultp.newLine();
					bwresultp.flush();
					bwresultp.close();
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
	}
	
}
