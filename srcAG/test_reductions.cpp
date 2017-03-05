#include "SimplerDecoder.h"

int main(int argc, char* argv[]){
	if ( argc == 1 ){
		std::cout<<"./test_reductions objtype filename [f] [k]"<<std::endl;
		return 0;
	}
	const unsigned objtype = atoi(argv[1]);
	char* filename = argv[2];
	double f=1, k=1;
	if ( objtype == 2  || objtype == 3){
		f = atoi(argv[3]);
		k = atoi(argv[4]);
	}
	SimplerDecoder decoder(filename, objtype, f, k);

	decoder.draw_graph("input_0.eps", "initial graph");
	//decoder.degree_test();
	decoder.draw_graph("input_wdt.eps", "with degree test");
}
