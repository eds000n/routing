# Macros:
#Compile me on agamenon, others doesn't have the required libs
CFLAGS= -O3 -fopenmp -lemon -std=c++11 
#CFLAGS= -O3 -fopenmp -lemon -I /home/edson/include -std=c++0x
#CFLAGS= -O3 -fopenmp -lemon -I /home/edson/include -std=gnu++0x
#CFLAGS= -O3 -fopenmp -lemon -I /home/edson/include -std=c++11
#CFLAGS= -O3 -lemo n
OBJECTS= Decoder.o sensores.o reductions.o heuristics.o SimplerDecoder.o
TEST_OBJECTS = test_reductions.o reductions.o heuristics.o SimplerDecoder.o
#OBJECTS= Decoder.o sensores.o SimplerDecoder.o
CPP = g++
# CPP = clang++

# Targets:
all: sensores test_reductions

test:
	./sensores 100 100 example.dat

sensores: $(OBJECTS)
	$(CPP) $(CFLAGS) $(OBJECTS) -o sensores -lemon

sensores.o: sensores.cpp Decoder.h MTRand.h BRKGA.h SimplerDecoder.h
	$(CPP) $(CFLAGS) -c sensores.cpp

Decoder.o: Decoder.cpp Decoder.h
	$(CPP) $(CFLAGS) -c Decoder.cpp

SimplerDecoder.o: SimplerDecoder.cpp SimplerDecoder.h
	$(CPP) $(CFLAGS) -c SimplerDecoder.cpp

reductions.o: reductions.cpp
	$(CPP) $(CFLAGS) -c reductions.cpp

heuristics.o: heuristics.cpp
	$(CPP) $(CFLAGS) -c heuristics.cpp

test_reductions: $(TEST_OBJECTS)
	$(CPP) $(CFLAGS) $(TEST_OBJECTS) -o test_reductions -lemon

test_reductions.o: test_reductions.cpp SimplerDecoder.h
	$(CPP) $(CFLAGS) -c test_reductions.cpp

# Instance.o: Instance.cpp Instance.h
# 	$(CPP) $(CFLAGS) -c Instance.cpp

# Remove:
clean:
	rm -f sensores $(OBJECTS) test_reductions
