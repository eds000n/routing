#include "SimplerDecoder.h"
#include "lemon/graph_to_eps.h"

void SimplerDecoder::non_terminal_degree_one(ListGraph::Node n){
	/*int degree = 0;
	bool degree_one = true;
	for(SmartGraph::IncEdgeIt e(graph, n); e!=INVALID; ++e){
		degree++;
		if ( degree > 1 ){
			degree_one = false;
			break;
		}
	}
	if ( degree_one ){
	}*/

	int degree = countIncEdges(graph, n);
	if ( degree == 1 && !(*is_terminal)[n] ){
		graph.erase(n);
	}
}

void SimplerDecoder::non_terminal_degree_two( ListGraph::Node n ){
	int degree = countIncEdges(graph, n);
	if ( degree == 2 && !(*is_terminal)[n] ){
		int c_jk = 0;
		ListGraph::Node n1;
		ListGraph::Node n2;
		int c = 0;
		for ( ListGraph::IncEdgeIt e(graph, n); e!=INVALID; ++e ){
			c_jk += (*weights)[e];
			if ( c == 0 ){
				if (  n == graph.u(e) )
					n1 = graph.v(e);
				else
					n1 = graph.u(e);
						
			}
			else if ( c == 1 ){
				if ( n == graph.u(e) )
					n2 = graph.v(e);
				else
					n2 = graph.u(e);
			}
			c++;
		}
		graph.erase(n);
		graph.addEdge(n1,n2);

	}
}

void SimplerDecoder::terminal_degree_one( ListGraph::Node n ){
	int degree = countIncEdges(graph, n);
	ListGraph::Node v_i = n;
	if ( degree == 1 && (*is_terminal)[n] ){
		for ( ListGraph::IncEdgeIt e(graph, n); e!=INVALID; ++e ){
			ListGraph::Node v_j = graph.u(e);
			if ( v_j == n )
				v_j = graph.v(e);
			SimplerDecoder::contract(n, v_j);
			contract(n,v_j);
		}
	}
}

void SimplerDecoder::terminal_degree_two(){
}

void SimplerDecoder::minimum_terminal_edge(){
}

///
// contract node v_j into node v_i according to the contraction definition given in
// A Generic Approach to Solving the Steiner Tree Problem and Variants pp. 19
void SimplerDecoder::contract( ListGraph::Node& v_i, ListGraph::Node& v_j){
	for ( ListGraph::IncEdgeIt e(graph, v_j); e!=INVALID; ++e ){
		ListGraph::Node v_k = graph.u(e);
		if ( v_k == v_j )
			v_k = graph.v(e);
		if ( v_k == v_i )
			continue;
		ListGraph::Edge e_ik;


		/// This block searches for the edge (v_i,v_k)
		bool found = false;
		for ( ListGraph::IncEdgeIt e_tmp(graph, v_k); e_tmp!=INVALID; ++e_tmp ){
			if ( (graph.u(e_tmp)==v_i && graph.v(e_tmp)==v_k) || (graph.v(e_tmp)==v_i && graph.u(e_tmp)==v_k) ){
				e_ik = e_tmp;
				found = true;
				break;
			}
		}
		cout<<(*original_id)[v_i]<<" "<<(*original_id)[v_k];
		cout<<" found ?"<<found<<endl;

		if (found && (*weights)[e_ik] > (*weights)[e] ){
			(*weights)[e_ik] = (*weights)[e];
#ifdef DEBUG
			cout<<"found and replacing cost"<<endl;
#endif
		}
		else if ( !found ){
			//e_ik = graph.addEdge(v_i, v_k);
			graph.addEdge(v_i, v_k);
			//(*weights)[e_ik] = (*weights)[e];
#ifdef DEBUG
			cout<<"not found and adding edge with its cost"<<endl;
#endif
		}

		/*if ( e_ik==INVALID ){
			e_ik = graph.addEdge(v_i, v_k);
			(*weights)[e_ik] = (*weights)[e];
		}else if ( e_ik!=INVALID && (*weights)[e_ik] > (*weights)[e] ){
			(*weights)[e_ik] = (*weights)[e];
		}*/
			
	}
	int pos = -1;
	for ( int i = 0; i<terminals.size() ; i++ ) 
		if ( terminals[i] == v_j ){
			terminals.push_back(v_i);
			pos = i;
			break;
		}
#ifdef DEBUG
	cout<<"erase"<<endl;
	//graph.erase(v_j);
	cout<<"erased"<<endl;
#endif
	if ( pos != -1 )
		terminals.erase(terminals.begin()+pos);
}

void SimplerDecoder::degree_test(){
#ifdef DEBUG
	std::stringstream ss;
	draw_graph("input_0.eps", "initial input");
#endif
	while(1){
		int n0 = countNodes(graph);
		for ( ListGraph::NodeIt n(graph); n!=INVALID; ++n){
			non_terminal_degree_one( n );
			non_terminal_degree_two( n );
			//terminal_degree_one( n );FIXME
			//terminal_degree_two();
			//minimum_terminal_edge();
		}
		if ( n0 == countNodes(graph) )
			break;
	}
#ifdef DEBUG
	std::cout<< "nodes " << countNodes(graph) << " edges: " << countEdges(graph) << std::endl;
#endif
	draw_graph("input_wdt.eps", "after DT reduction");
}

void SimplerDecoder::draw_graph(const char* file_name, const char* title) const{
#ifdef DEBUG	
	std::cout<< "nodes " << countNodes(graph) << " edges: " << countEdges(graph) << std::endl;
#endif
	graphToEps(graph, file_name)
	  .scaleToA4()
	  .coords(*coords)
	  .nodeShapes(*shapes)
	  .nodeSizes(*sizes)
	  .nodeTexts(*original_id)
	  .nodeTextSize(.025)
	  .distantColorNodeTexts()
	  .nodeColors(composeMap(palette,*colors))
	  .edgeWidths(*edge_widths)
	  //.edgeColors(composeMap(palette,*edge_colors))
	  .title(title)
	  .run();
}

void SimplerDecoder::draw_solution(std::vector<ListGraph::Edge> &sol, int color, std::string fname, int gen) const{
	ListGraph::EdgeMap<int> e_colors(graph, 0);
	ListGraph::EdgeMap<double> e_widths(graph, 0.1);
	for (std::vector<ListGraph::Edge>::iterator it = sol.begin(); it!=sol.end(); ++it){
		//(*edge_colors)[*it] = color;
		e_colors[*it] = color;
		e_widths[*it] = 1;
		//if (color == 1)
		//	e_widths = 1; //(*edge_widths)[*it] = 1;
		//else
		//	e_widths = 0.1(*edge_widths)[*it] = 0.1;

		//cout << graph.id(graph.u(*it)) << "," << graph.id(graph.v(*it)) << " ";
	}
	//cout << std::endl;
	std::stringstream ss;
	ss << fname << gen << ".eps";
	graphToEps(graph, ss.str()) 
	  //.preScale(false)
	  //.scale(20)
	  .scaleToA4()
	  .coords(*coords)
	  .nodeShapes(*shapes)
	  .nodeSizes(*sizes)
	  .nodeTexts(*original_id)
	  .nodeTextSize(.025) //.01
	  .distantColorNodeTexts()
	  .nodeColors(composeMap(palette, *colors))
	  .edgeWidths(e_widths)
	  .edgeColors(composeMap(palette, e_colors))
	  .title("titulo")
	  .run();
}
