package projects.GA.utils;

import java.util.ArrayList;
import java.util.List;

public class GAConcurrentOptimizedTree {
	private static int numTress=0;
	private static List<String> treeOptimized;

	public synchronized static int getNumTress() {
		return numTress;
	}

	public synchronized static void setNumTress(int numTress) {
		GAConcurrentOptimizedTree.numTress = numTress;
	}
	
	public synchronized static void augmentNumTrees(){
		numTress++;
	}
	
	public GAConcurrentOptimizedTree (){
		treeOptimized = new ArrayList<String>();
	}
	
	public synchronized static void getTreeOptimized(List<String> tree) {
		tree.addAll(treeOptimized);
	}

	public synchronized  static void setTreeOptimized(List<String> tree) {
		treeOptimized.clear();
		treeOptimized.addAll(tree);
	}
	
	
}
