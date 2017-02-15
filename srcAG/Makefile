# Macros:
#Compile me on agamenon, others doesn't have the required libs
CFLAGS= -O3 -fopenmp -lemon -std=c++11 
#CFLAGS= -O3 -fopenmp -lemon -I /home/edson/include -std=c++0x
#CFLAGS= -O3 -fopenmp -lemon -I /home/edson/include -std=gnu++0x
#CFLAGS= -O3 -fopenmp -lemon -I /home/edson/include -std=c++11
#CFLAGS= -O3 -lemon
#OBJECTS= Decoder.o sensores.o SimplerDecoder.o Reductions.o
OBJECTS= Decoder.o sensores.o SimplerDecoder.o
CPP = g++
# CPP = clang++

# Targets:
all: sensores

test:
	./sensores 100 100 example.dat

sensores: $(OBJECTS)
	$(CPP) $(CFLAGS) $(OBJECTS) -o sensores

sensores.o: sensores.cpp Decoder.h MTRand.h BRKGA.h SimplerDecoder.h
	$(CPP) $(CFLAGS) -c sensores.cpp

Decoder.o: Decoder.cpp Decoder.h
	$(CPP) $(CFLAGS) -c Decoder.cpp

SimplerDecoder.o: SimplerDecoder.cpp SimplerDecoder.h
	$(CPP) $(CFLAGS) -c SimplerDecoder.cpp

Reductions.o: reductions.cpp reductions.h
	$(CPP) $(CFLAGS) -c reductions.cpp

# Instance.o: Instance.cpp Instance.h
# 	$(CPP) $(CFLAGS) -c Instance.cpp

# Remove:
clean:
	rm -f sensores $(OBJECTS)
