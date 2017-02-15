#include "Decoder.h"
#include "SimplerDecoder.h"
#include "MTRand.h"
#include "BRKGA.h"
#include "Population.h"
#include <cstdio>

// ./sensores population generations objtype filename [f] [k]
int main(int argc, char* argv[]) {
  if (argc == 1){
    std::cout<<"./sensores population generations objtype filename [f] [k]"<<endl;
    std::cout<<"objtype can be 1 (without battery), 2 (with batter) and 3 (with normalized battery)"<<endl;
    return 0;
  }
  const unsigned p = atoi(argv[1]);
  const unsigned MAX_GENS = atoi(argv[2]);
  const unsigned objtype = atoi(argv[3]);//indicates type of problem: 1 without battery, 2 with battery, 3 with normalized battery
  char* filename = argv[4];
  double f=1, k=1;
  //SimplerDecoder dec
  if ( objtype == 2 ){
    f = atof(argv[5]);
    k = atof(argv[6]);
  }else if (objtype == 3){
    f = atof(argv[5]);
    k = atof(argv[6]);
  }
  SimplerDecoder decoder(filename, objtype, f, k);     // initialize the decoder
  const unsigned n = countEdges(decoder.graph);   // size of chromosomes
  
  const double pe = 0.10;   // fraction of population to be the elite-set
  const double pm = 0.10;   // fraction of population to be replaced by mutants
  const double rhoe = 0.60; // probability that offspring inherit an allele from elite parent
  const unsigned K = 3;   // number of independent populations
  // const unsigned MAXT = 4;  // number of threads for parallel decoding
  const unsigned MAXT = 1;  // number of threads for parallel decoding

  const long unsigned rngSeed = 0;  // seed to the random number generator
  MTRand rng(rngSeed);        // initialize the random number generator

  
  // initialize the BRKGA-based heuristic
  BRKGA< SimplerDecoder, MTRand > algorithm(n, p, pe, pm, rhoe, decoder, rng, K, MAXT);

  algorithm.inject_solution(decoder.initial_tree);


  unsigned generation = 0;    // current generation
  
  const unsigned X_INTVL = 100; // exchange best individuals at every 100 generations
  const unsigned X_NUMBER = p / 500;  // exchange top 2 best
  double last = -1;
  int rep = 0;
  do {
    //std::cerr << "Generation " << generation+1 << " of " << MAX_GENS << ": " << algorithm.getBestFitness() << "\n";
    std::cout << "Generation " << generation+1 << " of " << MAX_GENS << ": " << algorithm.getBestFitness() << "\n";
    if(algorithm.getBestFitness() == last)
      rep++;
    else {
      decoder.decode(algorithm.getBestChromosome(), true);
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
