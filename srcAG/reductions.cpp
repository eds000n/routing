#include "reductions.h"

void non_terminal_degree_one(ListGraph::Node n){
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
	if ( degree == 1 && !is_terminal[n] ){
		graph.erase(n);
	}


}

void non_terminal_degree_two( ListGraph::Node n ){
	int degree = countIncEdges(graph, n);
	if ( degree == 2 && !is_terminal[n] ){
		int c_jk = 0;
		ListGraph::Node n1;
		ListGraph::Node n2;
		int c = 0;
		for ( ListGraph::IncEdgeIt e(graph, n); e!=INVALID; ++e ){
			c_jk += weights[e];
			if ( c == 0 ){
				if (  n == g.u(e) )
					n1 = g.v(e);
				else
					n1 =  g.u(e);
						
			}
			else if ( c == 1){
				if ( n == g.u(e) )
					n2 = g.v(e);
				else
					n2 = g.u(e);
			}
			c++;
		}
		g.erase(n);
		g.addEgde(n1,n2);

	}
}

void terminal_degree_one(){
}

void terminal_degree_two(){
}

void minimum_terminal_edge(){
}

void degree_test(){
	/*void non_terminal_degree_one( n);
	void non_terminal_degree_two( n );
	void terminal_degree_one();
	void terminal_degree_two();
	void minimum_terminal_edge();*/

}
