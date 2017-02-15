#ifndef SIMPLER_DECODER_H
#define SIMPLER_DECODER_H

#include <list>
#include <vector>
#include <algorithm>
#include <fstream>
#include <iostream>
#include <lemon/smart_graph.h>
#include <lemon/full_graph.h>
#include <lemon/list_graph.h>
#include <unordered_map>
#include <lemon/adaptors.h>

using namespace lemon;
using namespace std;

class SimplerDecoder {
public:
  ListGraph graph;
  ListGraph::NodeMap<int> *original_id;
  ListGraph::EdgeMap<double> *weights;
  ListGraph::NodeMap<bool> *is_terminal;
  ListGraph::NodeMap<double> *node_weights;
  std::vector<ListGraph::Node> terminals;
  ListGraph::EdgeMap<int> *edge_chromosome;
  /*SmartGraph graph;
  SmartGraph::NodeMap<int> *original_id;
  SmartGraph::EdgeMap<double> *weights;
  SmartGraph::NodeMap<bool> *is_terminal;
  SmartGraph::NodeMap<double> *node_weights;
  std::vector<SmartGraph::Node> terminals;
  SmartGraph::EdgeMap<int> *edge_chromosome;*/
  int nodes, edges;
  int objtype;
  double c_factor, k_pow;
  std::vector<double> initial_tree;

	// Decoder(Instance* instance);
  //SimplerDecoder(char *filename, int objtype);
  SimplerDecoder(char *filename, int objtype, double c_factor = 1.0, double k_pow = 1.0);
	~SimplerDecoder();

	// double decode(const std::vector< double >& chromosome) const;
  double decode(const std::vector< double >& chromosome, bool output = false) const;
  
private:
  void read_nodes(ifstream& file, std::unordered_map<int,ListGraph::Node>& labels, int nodes);
  void read_edges(ifstream& file, std::unordered_map<int,ListGraph::Node>& labels, int edges);
  /*void read_nodes(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int nodes);
  void read_edges(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int edges);*/
  // void compute_distances(SmartGraph::EdgeMap<int>& modified,
  //                    FullGraph::EdgeMap<double>& k_dist, 
  //                    SmartGraph::NodeMap<SmartGraph::NodeMap<SmartGraph::Arc> *>& trees) const;
  // void retrive_paths(FullGraph::EdgeMap<bool>& tree_map,
  //                         SmartGraph::NodeMap<SmartGraph::NodeMap<SmartGraph::Arc> *>& trees, 
  //                         SmartGraph::EdgeMap<bool>& filter) const;

  // void remove_steiner_leaf(SmartGraph::EdgeMap<bool>& tree_map, SmartGraph::Node& v) const;
  // void map_to_complete();
  void sanity_check();
  // SmartGraph::NodeMap<FullGraph::Node> *tk_map;
  // FullGraph::NodeMap<SmartGraph::Node> *kt_map;

  double compute_value(ListGraph::EdgeMap<bool> &in_tree, bool output) const;
  double compute_value_rec(ListGraph::Node n, ListGraph::NodeMap<bool> &visited, ListGraph::EdgeMap<bool> &in_tree, bool &has_terminal, bool output) const;
  /*double compute_value(SmartGraph::EdgeMap<bool> &in_tree, bool output) const;
  double compute_value_rec(SmartGraph::Node n, SmartGraph::NodeMap<bool> &visited, SmartGraph::EdgeMap<bool> &in_tree, bool &has_terminal, bool output) const;*/
};

#endif
