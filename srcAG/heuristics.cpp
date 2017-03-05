#include "SimplerDecoder.h"
#include <lemon/dijkstra.h>
#include <lemon/fib_heap.h>
#include <lemon/concepts/heap.h>


///\brief Repetitive Shortest Path Heuristic
///
///Repetitive Shortest Path Heuristic 
///\param num number of solutions to return
///\param solutions vector containing the best <it>num</it> solutions
void SimplerDecoder::rsph(int num, std::vector< std::vector<ListGraph::Edge> >& solutions){
	for (std::vector<ListGraph::Node>::iterator it=terminals.begin(); it!=terminals.end(); it++){
		std::set<ListGraph::Edge> sol_set;
		//ListGraph::EdgeMap<double>
		/*concepts::ReadMap<ListGraph::Edge, float> length;
		//concepts::ReadWriteMap<ListGraph::Node, int> heap_cross_ref;
		//Dijkstra<ListGraph, concepts::ReadMap<ListGraph::Edge, float> > d(graph, length);
		//Dijkstra<ListGraph, concepts::ReadMap<ListGraph::Edge, float> >
		Dijkstra<ListGraph, ListGraph::EdgeMap<double> >
			::SetHeap<FibHeap<int, concepts::ReadWriteMap<ListGraph::Node, int> > , 
				  concepts::ReadWriteMap<ListGraph::Node, int> >
			::Create d(graph, *weights);
			//::Create d(graph, length);
		//d.predMap(pred).distMap(dist);//.heap(f_heap).addSource(*it);
		//
		*/
		//concepts::ReadWriteMap<ListGraph::Node, int> heap_cross_ref;
		ListGraph::NodeMap<double> heap_cross_ref(graph, -1); //Heap::State.PRE_HEAP
		//FibHeap<int, concepts::ReadWriteMap<ListGraph::Node, int> > f_heap(heap_cross_ref);
		//ListGraph::NodeMap<ListGraph::Node> pred(graph);
		ListGraph::NodeMap<double> dist(graph,0.0f);
		FibHeap<int, ListGraph::NodeMap<double> > f_heap(heap_cross_ref);
		//Dijkstra<ListGraph, ListGraph::EdgeMap<double> > d(graph, *weights);
		Dijkstra<ListGraph, ListGraph::EdgeMap<double> > 
			::SetHeap< FibHeap<int, ListGraph::NodeMap<double> >, ListGraph::NodeMap<double> >
			//::SetPredMap< ListGraph::NodeMap<ListGraph::Node> >
			::SetDistMap< ListGraph::NodeMap<double> >
			::Create d(graph, *weights);
		d.heap(f_heap, heap_cross_ref);
		//d.predMap(pred).distMap(dist);
		d.distMap(dist);
		d.init();
		d.addSource(*it);
		while (!d.emptyQueue()){
			ListGraph::Node n = d.processNextNode();
			//cout << graph.id(n) << ' ' << d.dist(n) << std::endl;
			if ( (*is_terminal)[n] ){
				while ( 1 ){
					ListGraph::Node tmp_node = d.predNode(n);
					if ( tmp_node == INVALID )
						break;
					for ( ListGraph::IncEdgeIt e(graph, n); e!=INVALID; ++e ){
						if ( tmp_node == graph.u(e) || tmp_node == graph.v(e) ){
							sol_set.insert(e);
							break;
						}
					}
					//if ( opt == 1 ){
					f_heap.set(tmp_node, 0);
					dist[tmp_node] = 0;
					//}
					n = tmp_node;
				}
			}
		}
		/*cout << "DEBUG" << std::endl;
		for( std::vector<ListGraph::Edge>::iterator it2=sol.begin(); it2!=sol.end(); ++it2 )
			cout << graph.id(graph.u(*it2)) << "," << graph.id(graph.v(*it2)) << std::endl;
		cout << "DEBUG" << std::endl;
		for ( std::set<ListGraph::Edge>::iterator it2=sol_set.begin(); it2!=sol_set.end(); ++it2)
			cout << graph.id(graph.u(*it2)) << "," << graph.id(graph.v(*it2)) << " ";
		cout << "DEBUG" << std::endl;
		*/

		std::vector<ListGraph::Edge> sol( sol_set.begin(), sol_set.end() );
		
		draw_solution( sol, 2, "rsph", graph.id(*it) );
		//std::cout << "begining from terminal " << graph.id(*it) << " " << sol.size() << std::endl;
		solutions.push_back(sol);
	}
#ifdef DEBUG
	for ( std::vector< std::vector<ListGraph::Edge> >::iterator it = solutions.begin(); it!=solutions.end(); it++)
		cout << (*it).size() << " " ;
	cout << std::endl;
#endif
	std::sort( solutions.begin(), solutions.end(), [](const std::vector<ListGraph::Edge> first, const std::vector<ListGraph::Edge> second ){
		return first.size() < second.size();
	} );
#ifdef DEBUG
	for ( std::vector< std::vector<ListGraph::Edge> >::iterator it = solutions.begin(); it!=solutions.end(); it++)
		cout << (*it).size() << " " ;
	cout << std::endl;
#endif
	solutions.resize(num);
#ifdef DEBUG
	for ( std::vector< std::vector<ListGraph::Edge> >::iterator it = solutions.begin(); it!=solutions.end(); it++)
		cout << (*it).size() << " " ;
	cout << std::endl;
#endif
}
