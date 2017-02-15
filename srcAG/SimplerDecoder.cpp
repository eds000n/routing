#include "SimplerDecoder.h"
#include <lemon/unionfind.h>
#include <lemon/bfs.h>
#include <lemon/list_graph.h>

// read the edges from the file
// an edge is given by u-v weight
//void SimplerDecoder::read_edges(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int edges) {
void SimplerDecoder::read_edges(ifstream& file, std::unordered_map<int,ListGraph::Node>& labels, int edges) {
	for(int i = 0; i < edges; i++) {
	  int u, v, in_old_tree;
	  double w;
	  char dash;
	  file >> u >> dash >> v >> w >> in_old_tree;
	  //SmartGraph::Edge e = graph.addEdge(labels[u], labels[v]);
	  ListGraph::Edge e = graph.addEdge(labels[u], labels[v]);
	  (*weights)[e] = w;
	  (*edge_chromosome)[e] = i;
	  if(in_old_tree)
	 		initial_tree.push_back(0);
	 	else
	 		initial_tree.push_back(1);
	}
}

// read the nodes from the file
// a node is given by id terminal x y battery
// the battery value is the spent energy by the node
//void SimplerDecoder::read_nodes(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int nodes) {
void SimplerDecoder::read_nodes(ifstream& file, std::unordered_map<int,ListGraph::Node>& labels, int nodes) {
	double w_max = 0;
	for(int i = 0; i < nodes; i++) {
	  int id, terminal;
	  double x, y, battery;
	  file >> id >> terminal >> x >> y >> battery;
	  //SmartGraph::Node node = graph.addNode();
	  ListGraph::Node node = graph.addNode();
	  labels[id] = node;
	  (*original_id)[node] = id;
	  if(terminal){
	    terminals.push_back(node);
	    (*is_terminal)[node] = true;
	  }
	  //
	  (*node_weights)[node] = battery;
	  if (w_max < battery) 
		  w_max = battery;
	  //
	}

	//cout<<"DEBUG"<<endl;
	if ( objtype == 3 && w_max > 0 ){
		//for (SmartGraph::NodeIt n(graph); n != INVALID; ++n) {
		for (ListGraph::NodeIt n(graph); n != INVALID; ++n) {
		 //cout<<(*node_weights)[n]<<";";
			 (*node_weights)[n] /=w_max ;
		 //cout<<(*node_weights)[n]<<"  ";
		}
		//cout<<endl;
	}
	//cout<<"DEBUG"<<endl;
}

// verifies if the instance has terminals and is conex
void SimplerDecoder::sanity_check() {
	//Bfs<SmartGraph> bfs(graph);
	Bfs<ListGraph> bfs(graph);
	if(terminals.size() == 0) {
		cerr << "instância sem terminais" << endl;
		abort();
	} else {
		bfs.run(terminals[0]);
		//for (SmartGraph::NodeIt n(graph); n != INVALID; ++n) {
		for (ListGraph::NodeIt n(graph); n != INVALID; ++n) {
	  	if ((*is_terminal)[n] && !bfs.reached(n)) {
	   		cerr << "grafo desconexo" << endl;
	   		cerr << "não existe caminho de " << (*original_id)[n] << " para " << (*original_id)[terminals[0]] << endl;
	    	abort();
	  	}
		}
	}
}
 
// creates a decoder from an instance given by a file
/*SimplerDecoder::SimplerDecoder(char *filename, int objtype) {
	this->objtype = objtype;
	original_id = new SmartGraph::NodeMap<int>(graph);
 	weights = new SmartGraph::EdgeMap<double>(graph);
	is_terminal = new SmartGraph::NodeMap<bool>(graph, false);
	//
	node_weights = new SmartGraph::NodeMap<double>(graph);
	//
	edge_chromosome = new SmartGraph::EdgeMap<int>(graph);
	ifstream file;
	file.open(filename);
	file >> nodes >> edges;
	std::unordered_map<int,SmartGraph::Node> labels;
	read_nodes(file, labels, nodes);
	read_edges(file, labels, edges);
	file.close();
	// sanity_check();
}*/

// creates a decoder from an instance given by a file
SimplerDecoder::SimplerDecoder(char *filename, int objtype, double f, double k) {
	this->objtype = objtype;	//If normalizing, it is done finishing the input of the graph, on read_nodes
	this->c_factor = f;
	this->k_pow = k;
	original_id = new ListGraph::NodeMap<int>(graph);
 	weights = new ListGraph::EdgeMap<double>(graph);
	is_terminal = new ListGraph::NodeMap<bool>(graph, false);
	node_weights = new ListGraph::NodeMap<double>(graph);
	edge_chromosome = new ListGraph::EdgeMap<int>(graph);
	/*original_id = new SmartGraph::NodeMap<int>(graph);
 	weights = new SmartGraph::EdgeMap<double>(graph);
	is_terminal = new SmartGraph::NodeMap<bool>(graph, false);
	//
	node_weights = new SmartGraph::NodeMap<double>(graph);
	//
	edge_chromosome = new SmartGraph::EdgeMap<int>(graph);*/
	ifstream file;
	file.open(filename);
	file >> nodes >> edges;
	//std::unordered_map<int,SmartGraph::Node> labels;
	std::unordered_map<int,ListGraph::Node> labels;
	read_nodes(file, labels, nodes);
	read_edges(file, labels, edges);
	file.close();
	// sanity_check();
}

// destroys the decoder
SimplerDecoder::~SimplerDecoder() {
	delete original_id;
	delete weights;
	delete is_terminal;
	//
	delete node_weights;
	//
}

//double SimplerDecoder::compute_value_rec(SmartGraph::Node n, SmartGraph::NodeMap<bool> &visited, SmartGraph::EdgeMap<bool> &in_tree, bool &has_terminal, bool output) const {
double SimplerDecoder::compute_value_rec(ListGraph::Node n, ListGraph::NodeMap<bool> &visited, ListGraph::EdgeMap<bool> &in_tree, bool &has_terminal, bool output) const {
	double value = 0, aux;
	double nodeval = 0;
	visited[n] = true;
	has_terminal = (*is_terminal)[n];
	//for (SmartGraph::IncEdgeIt e(graph, n); e != INVALID; ++e)	{
	for (ListGraph::IncEdgeIt e(graph, n); e != INVALID; ++e)	{
		//SmartGraph::Node v = graph.u(e) != n ? graph.u(e) : graph.v(e);
		ListGraph::Node v = graph.u(e) != n ? graph.u(e) : graph.v(e);
		if (visited[v] || in_tree[e] == false) continue;
		bool child_has_terminal = false;
		aux = compute_value_rec(v, visited, in_tree, child_has_terminal, output);
		has_terminal = has_terminal || child_has_terminal;
		if (child_has_terminal) {
			value += (*weights)[e] + aux;
			if (output) {
				cout << (*original_id)[graph.u(e)] << " " << (*original_id)[graph.v(e)] << endl;	
			}
			//
			double wtmp = c_factor*pow( (*node_weights)[v], k_pow );
			nodeval += wtmp;
			//nodeval += (*node_weights)[v];
			//
		}
	}
	//return value;
	//cout<<"Nodeval "<<nodeval<<endl;
	if (objtype==1)
		return value;
	else if (objtype==2)
		return value + nodeval;
	else if (objtype==3)
		return value + nodeval;
	return 0;
}

//double SimplerDecoder::compute_value(SmartGraph::EdgeMap<bool> &in_tree, bool output) const {
double SimplerDecoder::compute_value(ListGraph::EdgeMap<bool> &in_tree, bool output) const {
	bool has_terminal;
	ListGraph::NodeMap<bool> visited(graph, false);
	//SmartGraph::NodeMap<bool> visited(graph, false);
	//for (SmartGraph::NodeIt n(graph); n != INVALID; ++n)
	for (ListGraph::NodeIt n(graph); n != INVALID; ++n)
		if((*is_terminal)[n]){
			//return compute_value_rec(n, visited, in_tree, has_terminal, output);
			double res = compute_value_rec(n, visited, in_tree, has_terminal, output);
			//cout<<"obj "<<res<<endl;
			return res;
		}
	return 0;
}

double SimplerDecoder::decode(const std::vector< double >& chromosome, bool output) const {	
	// ordena o vetor de arestas por peso vezes o chromosome
	std::vector<ListGraph::Edge> edges;
	//std::vector<SmartGraph::Edge> edges;
	//for (SmartGraph::EdgeIt e(graph); e != INVALID; ++e) {
	for (ListGraph::EdgeIt e(graph); e != INVALID; ++e) {
		edges.push_back(e);
	}
	/*std::sort(edges.begin(), edges.end(),
	          [&chromosome, this] ( const SmartGraph::Edge& first, const SmartGraph::Edge& second ) {
	  return chromosome[(*edge_chromosome)[first]]*(*weights)[first] < chromosome[(*edge_chromosome)[second]]*(*weights)[second] ; 
	}  ) ;*/
	std::sort(edges.begin(), edges.end(),
	          [&chromosome, this] ( const ListGraph::Edge& first, const ListGraph::Edge& second ) {
	  return chromosome[(*edge_chromosome)[first]]*(*weights)[first] < chromosome[(*edge_chromosome)[second]]*(*weights)[second] ; 
	}  ) ;

	// MST algoritmo (Kruskal, mas poderia ser Prim)
	ListGraph::NodeMap<int> map(graph);
	UnionFind<ListGraph::NodeMap<int>>	uf(map);
	std::vector<ListGraph::Node> representative(countNodes(graph));
	ListGraph::NodeMap<bool> has_terminal(graph, false);
	/*SmartGraph::NodeMap<int> map(graph);
	UnionFind<SmartGraph::NodeMap<int>>	uf(map);
	std::vector<SmartGraph::Node> representative(countNodes(graph));
	SmartGraph::NodeMap<bool> has_terminal(graph, false);*/
	//for (SmartGraph::NodeIt n(graph); n != INVALID; ++n) {
	for (ListGraph::NodeIt n(graph); n != INVALID; ++n) {
 		 representative[uf.insert(n)] = n;
	}
	//SmartGraph::EdgeMap<bool> in_tree(graph, false);
	ListGraph::EdgeMap<bool> in_tree(graph, false);
	//for (std::vector<SmartGraph::Edge>::iterator e = edges.begin(); e != edges.end(); ++e) {
	for (std::vector<ListGraph::Edge>::iterator e = edges.begin(); e != edges.end(); ++e) {
		ListGraph::Node r1 = representative[uf.find(graph.u(*e))];
		ListGraph::Node r2 = representative[uf.find(graph.v(*e))];
		/*SmartGraph::Node r1 = representative[uf.find(graph.u(*e))];
		SmartGraph::Node r2 = representative[uf.find(graph.v(*e))];*/
		if(uf.join(graph.u(*e), graph.v(*e))) {
			in_tree[*e] = true;
			representative[uf.find(graph.u(*e))] = r1;
		}
	}
	// poda as arestas e devolve o valor, imprimindo se necessário
	return compute_value(in_tree, output);
}

double rsph(){
}
