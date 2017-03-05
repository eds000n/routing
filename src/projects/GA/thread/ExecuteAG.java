package projects.GA.thread;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import projects.GA.CustomGlobal;
import projects.GA.nodes.messages.SetRouteMessage;
import projects.GA.nodes.nodeImplementations.GANode;
import projects.GA.nodes.timers.MessageTimer;
import projects.GA.nodes.timers.SetRouteTimer;
import projects.GA.utils.GAConcurrentOptimizedTree;
import projects.GA.utils.GAEdge;
import projects.GA.utils.GATree;
import sinalgo.models.EnergyModel.EnergyMode;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

public class ExecuteAG implements Runnable{
	public static List<String> listTree = new ArrayList<String>();
	public static int nGenerations = 0;
//	GANode n;
	private static Process pr = null;				//ONLY process running the algorithm 
	private Runtime rt = null;
	private boolean executing = true;
	
	public ExecuteAG(){
//		this.n = n;
		
		this.rt = Runtime.getRuntime();
		
	}
	
	private String getCmd(){
		try{
			int p = sinalgo.configuration.Configuration.getIntegerParameter( "Population" )*Tools.getNodeList().size();
	        int g = sinalgo.configuration.Configuration.getIntegerParameter( "Generations" );
	        String GAexec = "srcAG/sensores";
	        String GAargs ;//= " " + p + " " + g +" 1 srcAG/log.dat 1 1";
	        if ( GANode.objFunction==1 ){
	        	GAargs = " " + p + " " + g +" 1 srcAG/log" + GANode.numSPTFiles + ".dat";
	        }else if ( GANode.objFunction==2 ){
	        	GAargs = " " + p + " " + g +" 2 srcAG/log" + GANode.numSPTFiles + ".dat " + GANode.fFactor + " " + GANode.kFactor;
	        }else{
	        	GAargs = " " + p + " " + g +" 3 srcAG/log" + GANode.numSPTFiles + ".dat " + GANode.fFactor + " " + GANode.kFactor;
	        }
	        File f = new File(GAexec);
	        if (!f.exists() || !f.canExecute()){
	        	Tools.stopSimulation();
	        	Tools.fatalError("sensores program not found, compile it first and check its execute permission");
	        	Tools.exit();
	        }
	        return GAexec + GAargs;
		}catch(Exception e){
			System.out.println(e.toString());
            e.printStackTrace();
		}
		return "";
	}
	
	public void SendSetRouteMessage(int n, double obj){

		ArrayList<GAEdge> edges = new ArrayList<GAEdge>();
		
		/////////////////////////////////////////////////////////////
		/*synchronized (CustomGlobal.treeOptimized) {
			for(String s: CustomGlobal.treeOptimized){
				String[] uv = s.split(" ");
				edges.add( new GAEdge( Integer.parseInt(uv[0]), Integer.parseInt(uv[1]) ) );
			}
		}*/
		List<String> listtree = new ArrayList<String>();
		CustomGlobal.treeOptimized.getTreeOptimized(listtree);
		GANode.debugMsg("DEBUG treeSize: " + listtree.size() + " objVal: " + obj + " generation: " + this.nGenerations + " terminals: (0-index) " + GANode.terminals + " edges:"  );
		for(String s: listtree){
			String[] uv = s.split(" ");
			edges.add( new GAEdge( Integer.parseInt(uv[0]), Integer.parseInt(uv[1]) ) );
			System.out.println("\t"+s);
		}
		
		//Creating the tree contained in the message
		GATree tree = new GATree(1);
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.add(1);
		while(!queue.isEmpty()){
			int u = queue.removeFirst();
			while ( true ){
				int v = searchEdge(edges, u);
				if (v==-1)
					break;
				GATree t = searchTree(tree, u);
				t.addTree(v);
				queue.add(v);
			}
		}
		
		//System.out.println("DEBUG Created tree: " + tree.toString());
		for(GATree t : tree.getTrees()){
			//SetRouteMessage setRouteMessage = new SetRouteMessage(1, 0, CustomGlobal.numTrees);
			int numTrees = GAConcurrentOptimizedTree.getNumTress();
			SetRouteMessage setRouteMessage = new SetRouteMessage(1, 0, numTrees);
			setRouteMessage.addTree(t);
			SetRouteTimer timer = new SetRouteTimer(Tools.getNodeByID(1), setRouteMessage);
			timer.startRelative(0.0001+numTrees/10.0, Tools.getNodeByID(1));
			((GANode)Tools.getNodeByID(1)).getBattery().spend(EnergyMode.SEND);
			GANode.debugMsg("SetRouteTimer time: " + timer.getFireTime(), 2);
		}
		
	}
	
	public GATree searchTree(GATree parent, int n){
		if (parent.root == n){
			return parent;
		}else{
			for(GATree son: parent.getTrees()){
				GATree tmptree = searchTree(son, n);
				if ( tmptree != null)
					return tmptree;
			}
		}
		return null;
	}
	
	public int searchEdge(ArrayList<GAEdge> edges, int n){
		for(GAEdge edge: edges){
			int r = edge.getPairNode(n);
			if (r>0){
				edges.remove(edge);
				return r;
			}
		}
		return -1;
	}
	
	public void attachExitDetector(){
	    try{
        	ProcessExitDetector processExitDetector = new ProcessExitDetector(pr);
            processExitDetector.addProcessListener(new ProcessListener() {
                public void processFinished(Process process) {
                	GANode.debugMsg("The subprocess has finished.");
                    if (process.exitValue()==0){
                    	Tools.appendToOutput("Process finished correctly\n");
                    }else{
                    	Tools.appendToOutput("Process finished wrong! see stderr\n");
                    	BufferedReader sdterr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    	String l = null;
                    	try {
							while((l=sdterr.readLine())!=null){
								System.out.println(l);
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
                    }
                    
                }
            });
            processExitDetector.start();
        }catch(IllegalArgumentException e){
        	System.out.println(e.getStackTrace());
        	Tools.appendToOutput(e.getMessage()+ "\n");
        }
	}

	/*public void restartProcess(){
//		assert (this.pr != null);
		if ( this.pr != null ){
			if ( pr.isAlive() )
				pr.destroy();
			else{
				this.run();
			}
		}

	}*/

	@Override
	public void run() {
		while (true){
			if ( isExecuting() ){
				try {

					//            Process pr = rt.exec("srcAG/sensores 10*n 1000 srcAG/log.dat");
					pr = rt.exec(getCmd());
					GANode.debugMsg("Thread " + Thread.currentThread().getId() + " executing genetic algorithm by: " + getCmd());
					attachExitDetector();

					BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
					BufferedReader error = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
					//            BufferedReader input = new BufferedReader(new FileReader("teste.txt"));
					boolean flag = false;
					listTree = new ArrayList<String>();
					String line=null;
					//System.out.println("================");

					//            while((line=input.readLine()) != null) {
					//            	listTree.add( line );
					//                System.out.println("** "+line);
					//                CustomGlobal.treeOptimized.add( line );
					//                if (line.equals("-")){
					//                	flag = false;
					//                	CustomGlobal.treeOptimized.clear();
					//                	CustomGlobal.numTrees++;
					//                }else
					//                	flag = true;
					//                
					//                if (flag)
					//                	listTree.clear();
					//            }

					/////////////////////////////////////////////###########################
					/*synchronized (CustomGlobal.treeOptimized) {
            	CustomGlobal.treeOptimized.clear();
            	String ll = null;
                while((line=input.readLine()) != null) {
                	if (ll!=null && ll.equals("-")){
                		Tools.appendToOutput("ExecuteAG.run(): treeOptimized size: " + CustomGlobal.treeOptimized.size() + "\n");
                		CustomGlobal.treeOptimized.clear();
                	}
                	if(!line.equals("-")){
                		listTree.add( line );
                		CustomGlobal.treeOptimized.add( line );
                		CustomGlobal.computedOptimalTree = true;
                	}else{
                		CustomGlobal.numTrees++;
                		SendSetRouteMessage(CustomGlobal.numTrees);
                		GANode.Edges = CustomGlobal.treeOptimized.size() ;
                	}

                    System.out.println("** "+line);

                    ll = line;

                }
                System.out.println("run() THREAD FINISHED: current time ()"+Global.currentTime);
                synchronized (this.n) {
                	this.n.notifyAll();
				}
                System.out.println("================");
                Tools.appendToOutput("ExecuteAG.run(): treeOptimized size: " + CustomGlobal.treeOptimized.size() + "\n");
                Tools.appendToOutput("ExecuteAG.run(): number of Optimized trees: " + CustomGlobal.numTrees + "\n");
			}*/
					List<String> listtree = new ArrayList<String>();

					String ll = null;
					//    		Pattern pat = Pattern.compile("Generation (\\d+) .*");
					Pattern pat = Pattern.compile("Generation (\\d+) of (\\d+): (\\d+.?\\d*)");
					double obj = 0; 
					while((line=input.readLine()) != null) {
						Matcher mat = pat.matcher(line);
						if (mat.matches()){
							this.nGenerations++;
							obj = Double.parseDouble(mat.group(3));
							//            		obj = mat.groupCount();
							//            		System.out.println("  => NGenerations " + this.nGenerations);
						}
						else{
							if (ll!=null && ll.equals("-")){
								Tools.appendToOutput("ExecuteAG.run(): treeOptimized size: " + listtree.size() + "\n");
								//CustomGlobal.treeOptimized.setTreeOptimized(listtree);
								listtree.clear();
							}
							if(!line.equals("-")){
								listTree.add( line );
								listtree.add( line );
								CustomGlobal.computedOptimalTree = true;
							}else{
								CustomGlobal.treeOptimized.setTreeOptimized(listtree);
								GAConcurrentOptimizedTree.augmentNumTrees();
								GANode.debugMsg("Sending SetRouteMessage #"+ GAConcurrentOptimizedTree.getNumTress());
								SendSetRouteMessage(GAConcurrentOptimizedTree.getNumTress(), obj);
								GANode.Edges = listtree.size() ;
								//CustomGlobal.numTrees++;

							}
							ll = line;
						}

					}
					GANode.debugMsg("run() THREAD FINISHED: current time ()"+Global.currentTime);
					//            synchronized (this.n) {
					//            synchronized (Tools.getNodeByID(1)) {
					//            	this.n.notifyAll();
					//            	Tools.getNodeByID(1).notifyAll();
					//			}


					//            String le = null;
					//            while((le=error.readLine()) != null ){
					//            	nGenerations++;
					//            }


					CustomGlobal.treeOptimized.setTreeOptimized(listtree);
					System.out.println("================");
					Tools.appendToOutput("ExecuteAG.run(): treeOptimized size: " + listtree.size() + "\n");
					Tools.appendToOutput("ExecuteAG.run(): number of Optimized trees: " + GAConcurrentOptimizedTree.getNumTress() + "\n");
					/////////////////////////////////////////////###########################


					line = null;
					CustomGlobal.listTree = listTree;
					//CustomGlobal.numTrees++;

					//***int exitVal = pr.waitFor();
					//***System.out.println("Exited with error code "+exitVal);
					CustomGlobal.available = true;

				} catch(Exception e) {
					System.out.println(e.toString());
					e.printStackTrace();
				}
				setExecuting(true);
			}
		}
	}

	public synchronized boolean isExecuting() {
		return executing;
	}

	public synchronized void setExecuting(boolean execute) {
		this.executing = execute;
	}

}
