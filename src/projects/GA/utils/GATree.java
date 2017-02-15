package projects.GA.utils;

import java.util.ArrayList;

public class GATree {
	public int root;
	private ArrayList<GATree> trees;
	
	public GATree(int root){
		this.root = root;
		trees = new ArrayList<GATree>();
	}
	
	/**
	 * Adds a tree
	 * @param t parent tree to which the new tree will be added
	 * @param n root of the new tree
	 * @return the reference to the new added tree
	 */
	public GATree addTree(int n){
		GATree nt = new GATree(n);
		trees.add(nt);
		return nt;
	}
	
	public ArrayList<GATree> getTrees() {
		return trees;
	}
	
	@Override
	public String toString(){
		return "{ root: " + root + ", size: " + trees.size() + " }";
	}
}

