#ifndef DECODER_H
#define DECODER_H

#include <list>
#include <vector>
#include <algorithm>
// #include "Instance.h"
#include <fstream>
#include <iostream>
#include <lemon/smart_graph.h>
#include <lemon/full_graph.h>
#include <lemon/list_graph.h>
#include <unordered_map>
#include <lemon/adaptors.h>

using namespace lemon;
using namespace std;

class Decoder {
public:
  // Instance* instance;
  FullGraph complete;
  SmartGraph graph;
  SmartGraph::NodeMap<int> *original_id;
  SmartGraph::EdgeMap<double> *weights;
  SmartGraph::NodeMap<bool> *is_terminal;
  std::vector<SmartGraph::Node> terminals;

	// Decoder(Instance* instance);
  Decoder(char *filename);
	~Decoder();

	// double decode(const std::vector< double >& chromosome) const;
  double decode(const std::vector< double >& chromosome, bool output = false) const;
  
private:
  void read_nodes(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int nodes);
  void read_edges(ifstream& file, std::unordered_map<int,SmartGraph::Node>& labels, int edges);
  void compute_distances(SmartGraph::EdgeMap<int>& modified,
                     FullGraph::EdgeMap<double>& k_dist, 
                     SmartGraph::NodeMap<SmartGraph::NodeMap<SmartGraph::Arc> *>& trees) const;
  void retrive_paths(FullGraph::EdgeMap<bool>& tree_map,
                          SmartGraph::NodeMap<SmartGraph::NodeMap<SmartGraph::Arc> *>& trees, 
                          SmartGraph::EdgeMap<bool>& filter) const;

  void remove_steiner_leaf(SmartGraph::EdgeMap<bool>& tree_map, SmartGraph::Node& v) const;
  void map_to_complete();
  void sanity_check();
  SmartGraph::NodeMap<FullGraph::Node> *tk_map;
  FullGraph::NodeMap<SmartGraph::Node> *kt_map;
};

#endif
