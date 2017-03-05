#!/bin/bash
#Start ALL the scenarios of simulation

DATARATE=20
F_FACTOR=1
K_FACTOR=1

# $1: project name
# $2: seed
# $3: number of events
# $4: number of nodes
# $5: density
function exec_simulation()
{
	project=$1	#SPT, InFRA, DAARP, DDAARP, DST, GA
	basecmd="java -Xmx8G -cp binaries/bin sinalgo.runtime.Main -project $project"
	if [ $1 == "SPT" ]; then
		nodeType="SPT:SPTNode"
	elif [ $1 == "Infra" ]; then
		nodeType="Infra:InfraNode"
	elif [ $1 == "DAARPMSWIM" ]; then
		nodeType="DAARPMSWIM:DAARPMSWIMNode"
	elif [ $1 == "DDAARP" ]; then
		nodeType="DDAARP:DDAARPNode"
	elif [ $1 == "DST" ]; then
		nodeType="DST:DSTNode"
	elif [ $1 == "GA" ]; then
		nodeType="GA:GANode"
	fi

	seed=$2
	numEvents=$3
	nodes=$4
	density=$5
	PI=3.141592
	rc=80
	dim=`echo "sqrt($nodes*$PI*$rc*$rc/$density)" | bc`
	dimx=$dim
	dimy=$dim
	cmd_dimensions=" -overwrite dimX=$dimx -overwrite dimY=$dimy "
	datarate=$DATARATE
	model="Random"
	events=`seq 1 $numEvents`
	declare -a x
	declare -a y
	declare -a xi
	declare -a yi
	for ne in $events
	do
		xi[$ne]=$RANDOM
		#sleep 1s
		yi[$ne]=$RANDOM
	done
	generations="50"
	population="2"
	ffactor="1"
	kfactor="1"
	#dimensions=[248,350,495,700,990]
	cmd_events_positions="-overwrite Event/NumEvents=$numEvents"
	for ne in $events
	do
		x[$ne]=$((${xi[$ne]}%$dimx))
		y[$ne]=$((${yi[$ne]}%$dimy))
		cmd_events_positions="${cmd_events_positions} -overwrite Event/Xposition${ne}=${x[$ne]} -overwrite Event/Yposition${ne}=${y[$ne]}"
	done
	#for ne in $events
	#do
	ne=$numEvents

		dr=$DATARATE
		if [ $1 == "GA" ]; then
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch -overwrite Population=$population -overwrite Generations=$generations -overwrite Objective=1 -overwrite fixedSeed=$seed -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions}  > ${n}-${ne}-${density}-${seed}-${project}-${dr}.1.out"
			echo $cmd >> .cmdfile
			echo "./mvstp.sh" >>.cmdfile
			echo "./mvdot.sh" >>.cmdfile
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch -overwrite Population=$population -overwrite Generations=$generations -overwrite Objective=2 -overwrite FFactor=0.1 -overwrite KFactor=2 -overwrite fixedSeed=$seed -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} > ${n}-${ne}-${density}-${seed}-${project}-${dr}.2.out"
			echo $cmd >> .cmdfile
			echo "./mvstp.sh" >>.cmdfile
			echo "./mvdot.sh" >>.cmdfile
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch -overwrite Population=$population -overwrite Generations=$generations -overwrite Objective=3 -overwrite FFactor=0.5 -overwrite KFactor=1.5 -overwrite fixedSeed=$seed -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} > ${n}-${ne}-${density}-${seed}-${project}-${dr}.3.out"
			echo $cmd >> .cmdfile
			echo "./mvstp.sh" >>.cmdfile
			echo "./mvdot.sh" >>.cmdfile
		else
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch -overwrite fixedSeed=$seed -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} > ${n}-${ne}-${density}-${seed}-${project}-${dr}.out"
			echo $cmd >> .cmdfile
		fi
	#done
}

# Scenario1: fixing number of events and density; varying the number of nodes 
# density=42 , ne=6, nodes={128,256,512,1024,2048}
function scenario1(){
	echo "#scenario1: varying nodes $1 $2" >> .cmdfile
	ne=6
	density=42
	nodes="128 256 512 1024 2048"
	for n in $nodes 
	do
		exec_simulation $1 $2 $ne $n $density
	done
}

# Scenario2: fixing number of nodes and density; variying number of events
# density=42 , ne={1,2,3,4,5,6} , nodes=1024
function scenario2(){
	echo "#scenario2: varying number of events $1 $2" >> .cmdfile
	density=42
	nodes=1024
	events="1 2 3 4 5" #6 is already included in scenario1
	for ne in $events
	do
		exec_simulation $1 $2 $ne $nodes $density
	done
}

# Scenario3: fixing events nodes; varying density
# density={20,30,42} , ne=6, nodes=1024
function scenario3(){
	echo "#scenario3: vaying density $1 $2" >> .cmdfile
	density="20 30" #42 is already included in scenario1
	nodes=1024
	ne=6
	for d in $density
	do
		exec_simulation $1 $2 $ne $nodes $d
	done
}

# Scenario4: fixing sink position
# sink_position=(0,0), density= , ne=6, nodes=1024
function scenario4(){
	echo "#scenario4: not implemented yet $1 $2" >> .cmdfile
}


# Scenario5: long duration of events
# density= , ne=6, nodes={128,256,512,1024,2048}
function scenario5(){
	echo "#scenario5: not implemented yet" >> .cmdfile
}

# Scenario6: standard scenario (for eventing ending perhaps)
# density= , ne=6, nodes=1024
function scenario6(){
	echo "#scenario6: standard scenario $1 $2" >> .cmdfile
	density=42
	nodes=1024
	ne=6
	exec_simulation $1 $2 $ne $nodes $density
}

function show_help()
{
cat <<"HELP"
Use: ./simulation.sh -r <number_of_rounds> [-s] [-i] [-d] [-D] [-S] [-g]
	-s	for SPT
	-i	for Infra
	-d	for DAARP
	-D	for DDAARP
	-S	for DST
	-g	for GA

The script creates a file named .cmdfile with the commands to run the simulation. If the file
exists, it is overwritten.

TODO: The script will dinamically allocate one process in each host. The list of hosts is defined
in the hosts.lst file. Please verify that the hosts have the required libraries and the 
JRE7, NOT OPENJDK!!!!
HELP
}

spt=0;
infra=0;
daarp=0;
ddaarp=0;
dst=0;
ga=0;
rounds=30;
nevents=6;
listevent=0;
while getopts "r:e:EsidDSgc:h" opt;
do
	case $opt in
		s)
			echo "runnig simulation for SPT"
			spt=1;
			;;
		i)
			echo "runnig simulation for Infra"
			infra=1;
			;;
		d)
			echo "runnig simulation for DAARP"
			daarp=1;
			;;
		D)
			echo "runnig simulation for DDAARP"
			ddaarp=1;
			;;
		S)
			echo "runnig simulation for DST"
			dst=1;
			;;
		g)
			echo "runnig simulation for GA"
			ga=1;
			;;
		r)
			rounds=$OPTARG;
			echo "number of rounds: $rounds"
			;;
		e)
			nevents=$OPTARG;
			echo "number of events: $nevents (from 1 to n)"
			;;
		E)	
			listevent=1;
			echo "Just executing for one event which is indicated in the -e param"
			;;
		c)
			echo "scenario"
			;;
		h)
			show_help
			exit 0
			;;
		\?)
			echo "Invalid option: -$OPTARG" >&2
			exit 1
			;;
	esac
done

if [ "$#" -eq "0" ]
then
	show_help
	exit 1
fi

if [ -e .cmdfile ]
then
	rm .cmdfile
fi
alg=""
if [ $spt -eq 1 ]
then
	alg="${alg} SPT"
fi

if [ $infra -eq 1 ]
then
	alg="${alg} Infra"
fi
if [ $daarp -eq 1 ]
then
	alg="${alg} DAARPMSWIM"
fi
if [ $ddaarp -eq 1 ]
then
	alg="${alg} DDAARP"
fi
if [ $dst -eq 1 ]
then
	alg="${alg} DST"
fi
if [ $ga -eq 1 ]
then
	alg="${alg} GA"
fi

for i in `seq 1 $rounds`
do
	seed=$RANDOM
	for a in $alg
	do
		scenario1 $a $seed
		scenario2 $a $seed
		scenario3 $a $seed
		#scenario4 $a $seed
		#scenario5 $a $seed
		#scenario6 $a $seed
	done
done

