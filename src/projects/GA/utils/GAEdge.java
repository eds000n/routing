package projects.GA.utils;

public class GAEdge {	//Edge of the graph. It's always guaranteed that u<v so that the sorting is easier.
	private int u,v;

	public GAEdge(int u, int v){
		if (u<v){
			this.u = u;
			this.v = v;
		}else{
			this.u = v;
			this.v = u;
		}
		
	}
	
	public boolean hasNode(int n){
		if (this.u==n || this.v==n)
			return true;
		else
			return false;
	}
	
	public int getPairNode(int n){
		if (hasNode(n)){
			if (n==u)
				return v;
			else if (n==v)
				return u;
		}
		return -1;
	}
	
	public int getU() {
		return u;
	}

	public int getV() {
		return v;
	}

	@Override
	public String toString() {
		return "GAEdge [u=" + u + ", v=" + v + "]";
	}
}
