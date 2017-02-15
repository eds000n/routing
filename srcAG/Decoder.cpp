#include "Decoder.h"
#include <lemon/dijkstra.h>
#include <lemon/kruskal.h>
#include <lemon/unionfind.h>
#include <lemon/adaptors.h>
#include <lemon/bfs.h>
#include <lemon/radix_heap.h>

// read the edges from the file
// an edge is given by u-v weight
void Decoder::read_edges(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int edges) {
	for(int i = 0; i < edges; i++) {
	  int u, v;
	  double w;
	  char dash;
	  file >> u >> dash >> v >> w;
	  SmartGraph::Edge e = graph.addEdge(labels[u], labels[v]);
	  (*weights)[e] = w;
	}
}

// read the nodes from the file
// a node is given by id terminal x y battery
void Decoder::read_nodes(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int nodes) {
	for(int i = 0; i < nodes; i++) {
	  int id, terminal;
	  double x, y, battery;
	  file >> id >> terminal >> x >> y >> battery;
	  SmartGraph::Node node = graph.addNode();
	  labels[id] = node;
	  (*original_id)[node] = id;
	  if(terminal){
	    terminals.push_back(node);
	    (*is_terminal)[node] = true;
	  }
	}
}

// create a map from the graph to a complete graph on the terminals set
void Decoder::map_to_complete() {
	complete.resize(terminals.size());
	tk_map = new SmartGraph::NodeMap<FullGraph::Node>(graph);
	kt_map = new FullGraph::NodeMap<SmartGraph::Node>(complete);
	int j = 0;
	for (FullGraph::NodeIt v(complete); v != INVALID; ++v, ++j){
		(*tk_map)[terminals[j]] = v;
		(*kt_map)[v] = terminals[j];
	}
}

// verifies if the instance has terminals and is conex
void Decoder::sanity_check() {
	Bfs<SmartGraph> bfs(graph);
	if(terminals.size() == 0) {
		cerr << "instância sem terminais" << endl;
		abort();
	} else {
		bfs.run(terminals[0]);
		for (SmartGraph::NodeIt n(graph); n != INVALID; ++n) {
	  	if ((*is_terminal)[n] && !bfs.reached(n)) {
	   		cerr << "grafo desconexo" << endl;
	   		cerr << "não existe caminho de " << (*original_id)[n] << " para " << (*original_id)[terminals[0]] << endl;
	    	abort();
	  	}
		}
	}
}
 
// creates a decoder from an instance given by a file
Decoder::Decoder(char *filename) {
	original_id = new SmartGraph::NodeMap<int>(graph);
 	weights = new SmartGraph::EdgeMap<double>(graph);
	is_terminal = new SmartGraph::NodeMap<bool>(graph, false);
	ifstream file;
	file.open(filename);
	int nodes, edges;
	file >> nodes >> edges;
	std::unordered_map<int,SmartGraph::Node> labels;
	read_nodes(file, labels, nodes);
	read_edges(file, labels, edges);
	file.close();
	map_to_complete();
	// sanity_check();
}

// destroys the decoder
Decoder::~Decoder() {
	delete original_id;
	delete weights;
	delete is_terminal;
	delete tk_map;
	delete kt_map;
}

// compute the distances between every pair of terminals
void Decoder::compute_distances(SmartGraph::EdgeMap<int>& modified,
                     FullGraph::EdgeMap<double>& k_dist, 
                     SmartGraph::NodeMap<SmartGraph::NodeMap<SmartGraph::Arc> *>& trees) const {
	SmartGraph::NodeMap<SmartGraph::Arc> *pred;
	SmartGraph::NodeMap<int> dist(graph);
	// se quiser usar RadixHead descomente abaixo
	// typedef RadixHeap<SmartGraph::NodeMap<int>> NodeHeap;
  // typename Dijkstra<SmartGraph, SmartGraph::EdgeMap<int>>::template SetStandardHeap<NodeHeap>::Create dijkstra(graph, modified);
  Dijkstra<SmartGraph, SmartGraph::EdgeMap<int>> dijkstra(graph, modified);
	// dá para fazer até terminals.size()-1, mas tem que ajeitar algumas coisas
	for (int i = 0; i < terminals.size(); i++){
		SmartGraph::Node s = terminals[i];
		pred = new SmartGraph::NodeMap<SmartGraph::Arc>(graph);
		// colocar condicao de parada ter visitado todos os outros vértices
		dijkstra.predMap(*pred);	
		dijkstra.distMap(dist);
		dijkstra.init();
		dijkstra.addSource(s);
		dijkstra.start();
		trees[s] = pred;
		for (FullGraph::IncEdgeIt e(complete, (*tk_map)[s]); e != INVALID; ++e) {
			FullGraph::Node u = complete.u(e);
			FullGraph::Node v = complete.v(e);
			double d = (s == (*kt_map)[u] ? dist[(*kt_map)[v]] : dist[(*kt_map)[u]]);
			k_dist[complete.arc(u,v)] = d;
		}
  }	
}

void Decoder::retrive_paths(FullGraph::EdgeMap<bool>& tree_map,
                          SmartGraph::NodeMap<SmartGraph::NodeMap<SmartGraph::Arc> *>& trees, 
                          SmartGraph::EdgeMap<bool>& filter) const
{
	int j;
	for (FullGraph::EdgeIt e(complete); e != INVALID; ++e, ++j){
		if(tree_map[e]) {
			SmartGraph::Node s = (*kt_map)[complete.u(e)];
			SmartGraph::Node t = (*kt_map)[complete.v(e)];
			SmartGraph::Node prev = t;
			while(prev != s) {
				SmartGraph::Edge edge = (*trees[s])[prev];
				filter[edge] = true;
				prev = graph.u(edge) == prev ? graph.v(edge) : graph.u(edge);
			}
		}
	}	
}

void Decoder::remove_steiner_leaf(SmartGraph::EdgeMap<bool>& tree_map, SmartGraph::Node& v) const {
	cerr << "folha de steiner" << endl;
}

double Decoder::decode(const std::vector< double >& chromosome, bool output) const {
	// criar um novo map com os pesos modificados
	SmartGraph::EdgeMap<int> modified(graph);
	int j = 0;
	for (SmartGraph::EdgeIt e(graph); e != INVALID; ++e, ++j){
		modified[e] = 1000*(*weights)[e]*chromosome[j];
	}	
	
	// rodar djikstra para k terminais
	FullGraph::EdgeMap<double> k_dist(complete);
	SmartGraph::NodeMap<SmartGraph::NodeMap<SmartGraph::Arc> *> trees (graph);
	compute_distances(modified, k_dist, trees);

	// rodar MST
  FullGraph::EdgeMap<bool> tree_map(complete);
  kruskal(complete, k_dist, tree_map);

	// recuperar os caminhos originais
	SmartGraph::EdgeMap<bool> filter(graph, false);
	retrive_paths(tree_map, trees, filter);

	//encontra uma árvore com os terminais
	FilterEdges<const SmartGraph> subgraph(graph, filter);
	SmartGraph::EdgeMap<bool> tree_map2(graph);
	std::vector<SmartGraph::Edge> tree;
	double k = kruskal(subgraph, *weights, tree_map2);

	// remove folhas steiner
	// percorer os vértices não-terminais, se tiver grau 1 remove recursivamente
	// FilterEdges<const SmartGraph> final_tree(graph, tree_map2);
	// for (SmartGraph::NodeIt v(graph); v != INVALID; ++v){
	// 	if (!(*is_terminal)[v] && countIncEdges(final_tree, v) == 1)
	// 		remove_steiner_leaf(tree_map2, v);			
	// }

	// Imprimi
	if (output)
		for (SmartGraph::EdgeIt e(graph); e != INVALID; ++e)
			if (tree_map2[e])
				cout << (*original_id)[graph.u(e)] << " " << (*original_id)[graph.v(e)] << endl;

	// libera a memória
	for (int i = 0; i < terminals.size(); i++){
		SmartGraph::Node s = terminals[i];
		delete trees[s];
	}

	return k;
}
