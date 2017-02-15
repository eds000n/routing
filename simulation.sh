#!/bin/bash
#Script que genera los comandos para ejectuar las simulaciones normales: overhead, datapackets, efficiency, steiner nodes vs #of events and #of nodes

# $1: project name
# $2: seed
# $3: objective funcion, ONLY FOR GA
function_exec_case1 ()
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
		objfunction=$3
	fi

	nodes="128 256 512 1024 2048"
	dims="248 350 495 700 990"
	seed=$2
	dimx=700
	dimy=700
	model="Random"
	numEvents="1 2 3 4 5 6"
	declare -a x
	declare -a y
	declare -a xi
	declare -a yi
	for ne in $numEvents
	do
		xi[$ne]=$RANDOM
		#sleep 1s
		yi[$ne]=$RANDOM
	done
	#numEvents="1"
	generations="50"
	population="2"
	for n in $nodes
	do
		if [ "$n" -eq "128" ]
		then
			dimx=248
			dimy=248
		elif [ "$n" -eq "256" ]
		then
			dimx=350
			dimy=350
		elif [ "$n" -eq "512" ]
		then
			dimx=495
			dimy=495
		elif [ "$n" -eq "1024" ]
		then
			dimx=700
			dimy=700
		elif [ "$n" -eq "2048" ]
		then
			dimx=990
			dimy=990
		fi
		for ne in $numEvents
		do
			x[$ne]=$((${xi[$ne]}%$dimx))
			y[$ne]=$((${yi[$ne]}%$dimy))
		done
		for ne in $numEvents
		do
			#for rounds in `seq 1 $numRounds`
			#do
			eventpositioncmd=" -overwrite Event/Xposition1=${x[1]} -overwrite Event/Yposition1=${y[1]} -overwrite Event/Xposition2=${x[2]} -overwrite Event/Yposition2=${y[2]} -overwrite Event/Xposition3=${x[3]} -overwrite Event/Yposition3=${y[3]} -overwrite Event/Xposition4=${x[4]} -overwrite Event/Yposition4=${y[4]} -overwrite Event/Xposition5=${x[5]} -overwrite Event/Yposition5=${y[5]} -overwrite Event/Xposition6=${x[6]} -overwrite Event/Yposition6=${y[6]} "
				if [ $1 == "GA" ]; then
					cmd="$basecmd -gen $n ${nodeType} $model -batch -overwrite Event/NumEvents=$ne -overwrite Population=$population -overwrite Generations=$generations -overwrite Objective=${objfunction} -overwrite fixedSeed=$seed -overwrite dimX=$dimx -overwrite dimY=$dimy $eventpositioncmd > ${n}-${ne}-${seed}-${project}.out"
				else
					cmd="$basecmd -gen $n ${nodeType} $model -batch -overwrite Event/NumEvents=$ne -overwrite fixedSeed=$seed -overwrite dimX=$dimx -overwrite dimY=$dimy $eventpositioncmd > ${n}-${ne}-${seed}-${project}.out"
				fi
				echo $cmd >> .cmdfile
			#done
		done
	done

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

The script will dinamically allocate one process in each host. The list of hosts is defined
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
objfunction=1;
while getopts "r:sidDSgc:ho:" opt;
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
		o)
			echo "objective funcion for GA"
			if [ "$OPTARG" -eq "1" ]
			then
				echo "Classic Steiner Tree objective function"
				objfunction=$OPTARG
			elif [ "$OPTARG" -eq "2" ]
			then
				echo "Node-weighted Steiner Tree objective function"
				objfunction=$OPTARG
			fi
			;;
		r)
			rounds=$OPTARG
			echo "number of rounds: $rounds"
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
for i in `seq 1 $rounds`
do
	seed=$RANDOM
	if [ $spt -eq 1 ]
	then
		function_exec_case1 "SPT" $seed
	fi

	if [ $infra -eq 1 ]
	then
		function_exec_case1 "Infra" $seed
	fi
	if [ $daarp -eq 1 ]
	then
		function_exec_case1 "DAARPMSWIM" $seed
	fi
	if [ $ddaarp -eq 1 ]
	then
		function_exec_case1 "DDAARP" $seed
	fi
	if [ $dst -eq 1 ]
	then
		function_exec_case1 "DST" $seed
	fi
	if [ $ga -eq 1 ]
	then
		function_exec_case1 "GA" $seed $objfunction
	fi
done

