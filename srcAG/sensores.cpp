#include "Decoder.h"
#include "SimplerDecoder.h"
#include "MTRand.h"
#include "BRKGA.h"
#include "Population.h"
#include <cstdio>
#include <numeric>

// ./sensores population generations objtype filename [f] [k]
int main(int argc, char* argv[]) {
  if (argc == 1){
    std::cout<<"./sensores population generations objtype filename [processing] [f] [k]"<<endl;
    std::cout<<"objtype can be 1 (without battery), 2 (with batter) and 3 (with normalized battery)"<<endl;
    std::cout<<"processing by default is 1(true)"<<endl<<endl;
    return 0;
  }
  const unsigned p = atoi(argv[1]);
  const unsigned MAX_GENS = atoi(argv[2]);
  const unsigned objtype = atoi(argv[3]);//indicates type of problem: 1 without battery, 2 with battery, 3 with normalized battery
  char* filename = argv[4];
  double f=1, k=1;
  bool preprocessing = true;
  if ( argc >= 6 )
    if ( atoi(argv[5])==0 )
      preprocessing = false;
  //SimplerDecoder dec
  if ( objtype == 2 ){
    f = atof(argv[6]);
    k = atof(argv[7]);
  }else if (objtype == 3){
    f = atof(argv[6]);
    k = atof(argv[7]);
  }

  SimplerDecoder decoder(filename, objtype, f, k);     // initialize the decoder
  const unsigned m = countEdges(decoder.graph);   // size of chromosomes
  
  const double pe = 0.10;   // fraction of population to be the elite-set
  //const double pm = 0.10;   // fraction of population to be replaced by mutants
  const double pm = 0.40;   // fraction of population to be replaced by mutants
  const double rhoe = 0.60; // probability that offspring inherit an allele from elite parent
  const unsigned K = 3;   // number of independent populations
  // const unsigned MAXT = 4;  // number of threads for parallel decoding
  const unsigned MAXT = 1;  // number of threads for parallel decoding

  const long unsigned rngSeed = 0;  // seed to the random number generator
  MTRand rng(rngSeed);        // initialize the random number generator

  
  // initialize the BRKGA-based heuristic
  BRKGA< SimplerDecoder, MTRand > algorithm(m, p, pe, pm, rhoe, decoder, rng, K, MAXT);

  algorithm.inject_solution(decoder.initial_tree);
#ifdef DEBUG
  std::cout << "Initial_tree size " << m-std::accumulate(decoder.initial_tree.begin(), decoder.initial_tree.end(), 0) << std::endl;
#endif

  unsigned generation = 0;    // current generation
  
  const unsigned X_INTVL = 10; // exchange best individuals at every 100 generations
  //const unsigned X_NUMBER = p / 500;  // exchange top 2 best
  const unsigned X_NUMBER = 2;  // exchange top 2 best
  double last = -1;
  int rep = 0;

  if ( preprocessing ){
    std::vector< std::vector<ListGraph::Edge> > sols;
    decoder.draw_graph("input_0.eps", "initial graph");
    decoder.degree_test();
    decoder.draw_graph("input_wdt.eps", "with degree test");
    decoder.rsph(1, sols);

    for ( std::vector< std::vector<ListGraph::Edge> >::iterator it=sols.begin(); it!=sols.end(); ++it) {
      std::vector<double> tmp_sol(m, 1);
      for ( std::vector<ListGraph::Edge>::iterator ii=(*it).begin(); ii!= (*it).end(); ++ii )
        tmp_sol[(*(decoder.edge_chromosome))[*ii]] = 0;
      algorithm.inject_solution(tmp_sol);
    }
  }

  do {
    //std::cerr << "Generation " << generation+1 << " of " << MAX_GENS << ": " << algorithm.getBestFitness() << "\n";
    std::cout << "Generation " << generation+1 << " of " << MAX_GENS << ": " << algorithm.getBestFitness() << "\n";
    if(algorithm.getBestFitness() == last)
      rep++;
    else {
      decoder.decode(algorithm.getBestChromosome(), true, generation);
      cout << "-" << endl;
      rep = 0;
    }
    if(rep == 20)
      break;
    last = algorithm.getBestFitness();
    algorithm.evolve(); // evolve the population for one generation        
    if((++generation) % X_INTVL == 0) {
      algorithm.exchangeElite(X_NUMBER);  // exchange top individuals
    }
  } while (generation < MAX_GENS);
  return 0;
}
