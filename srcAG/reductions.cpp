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
					n1 =  graph.u(e);
						
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

void SimplerDecoder::terminal_degree_one(){
}

void SimplerDecoder::terminal_degree_two(){
}

void SimplerDecoder::minimum_terminal_edge(){
}

void SimplerDecoder::degree_test(){
	//std::stringstream ss;
	//draw_graph("input_0.eps", "initial input");
	for ( ListGraph::NodeIt n(graph); n!=INVALID; ++n){
		non_terminal_degree_one( n );
		//non_terminal_degree_two( n );
		terminal_degree_one();
		terminal_degree_two();
		minimum_terminal_edge();
	}
	//std::cout<< "nodes " << countNodes(graph) << " edges: " << countEdges(graph) << std::endl;
	//draw_graph("input_wdt.eps", "after DT reduction");
}

void SimplerDecoder::draw_graph(const char* file_name, const char* title) const{
	std::cout<< "nodes " << countNodes(graph) << " edges: " << countEdges(graph) << std::endl;
	graphToEps(graph, file_name)
	  .coords(*coords)
	  .nodeShapes(*shapes)
	  .nodeSizes(*sizes)
	  .nodeTexts(*original_id)
	  .nodeTextSize(.01)
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
