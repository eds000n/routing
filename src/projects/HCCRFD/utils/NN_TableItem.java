package projects.HCCRFD.utils;

public class NN_TableItem {
	int nn;			//NeighborNode
	int hc;			//HopCount between NN and the sink
	float re_nn;	//Remaining Energy of NN
	float ds;		//Distance from SN (SourceNode) to the neighbor node
	float dn;		//Distance from NN to the sink
	
	/**
	 * Constructor
	 * @param nn neighbor node ID
	 * @param hc hop count between neighbor node and the sink
	 * @param re_nn remaining energy of neighbor node
	 * @param ds euclidean distance between source node and the sink 
	 * @param dn euclidean distance between neighbor node and the sink
	 */
	public NN_TableItem(int nn, int hc, float re_nn, float ds, float dn){
		this.nn = nn;
		this.hc = hc;
		this.re_nn = re_nn;
		this.ds = ds;
		this.dn = dn;
	}

	public int getNn() {
		return nn;
	}

	public void setNn(int nn) {
		this.nn = nn;
	}

	public int getHc() {
		return hc;
	}

	public void setHc(int hc) {
		this.hc = hc;
	}

	public float getRe_nn() {
		return re_nn;
	}

	public void setRe_nn(float re_nn) {
		this.re_nn = re_nn;
	}

	public float getDs() {
		return ds;
	}

	public void setDs(float ds) {
		this.ds = ds;
	}

	public float getDn() {
		return dn;
	}

	public void setDn(float dn) {
		this.dn = dn;
	}
}
