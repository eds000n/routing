#ifndef REDUCTIONS_H
#define REDUCTIONS_H

#include <lemon/list_graph.h>
#include "SimplerDecoder.h"

using namespace lemon;
void non_terminal_degree_one( ListGraph::Node );
void non_terminal_degree_two( ListGraph::Node );
void terminal_degree_one();
void terminal_degree_two();
void minimum_terminal_edge();
void degree_test();


#endif
