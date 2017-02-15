#!/bin/bash
#Script que genera los comandos para ejectuar las simulacion de First Dead Node (FDN): time vs #events

# $1: project name
# $2: seed
# $3: number of events
# $4: 1 to execute just for one event; 0 for 1 to `number of events'
function_exec_case1 ()
{
	project=$1	#SPT, InFRA, DAARP, DDAARP, DST, GA
	basecmd="java -Xmx38G -cp binaries/bin sinalgo.runtime.Main -project $project"
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

	#nodes="128 256 512 1024 2048"
	nodes="256 512 1024"
	dims="248 350 495 700 990"
	seed=$2
	numEvents=$3
	dimx=700
	dimy=700
	#datarate="1 2 3 4 5 6 7 8 9"
	#datarate="1 3 5 7 9"
	datarate="1"
	model="Random"
	events="1 2 3 4 5 6"
	#events=`seq 1 $numEvents`
	listevent=$4
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
		for ne in $events
		do
			x[$ne]=$((${xi[$ne]}%$dimx))
			y[$ne]=$((${yi[$ne]}%$dimy))
		done
		for ne in $events
		do
			if [ $listevent -eq 1 ] #Just process the event given
			then
				if [ $ne -ne $numEvents ]
				then
					continue;
				fi
			fi
			for dr in $datarate
			do
				if [ $1 == "GA" ]; then
					cmd="$basecmd -gen $n ${nodeType} $model -batch -overwrite Event/NumEvents=$ne -overwrite Population=$population -overwrite Generations=$generations -overwrite Objective=1 -overwrite fixedSeed=$seed -overwrite dimX=$dimx -overwrite dimY=$dimy -overwrite Event/DataRate=$dr -overwrite Event/Xposition1=${x[1]} -overwrite Event/Yposition1=${y[1]} -overwrite Event/Xposition2=${x[2]} -overwrite Event/Yposition2=${y[2]} -overwrite Event/Xposition3=${x[3]} -overwrite Event/Yposition3=${y[3]} -overwrite Event/Xposition4=${x[4]} -overwrite Event/Yposition4=${y[4]} -overwrite Event/Xposition5=${x[5]} -overwrite Event/Yposition5=${y[5]} -overwrite Event/Xposition6=${x[6]} -overwrite Event/Yposition6=${y[6]} > ${n}-${ne}-${seed}-${project}-${dr}.1.out"
					echo $cmd >> .cmdfile
					echo "./mvstp.sh" >>.cmdfile
					echo "./mvdot.sh" >>.cmdfile
					cmd="$basecmd -gen $n ${nodeType} $model -batch -overwrite Event/NumEvents=$ne -overwrite Population=$population -overwrite Generations=$generations -overwrite Objective=2 -overwrite FFactor=0.1 -overwrite KFactor=2 -overwrite fixedSeed=$seed -overwrite dimX=$dimx -overwrite dimY=$dimy -overwrite Event/DataRate=$dr -overwrite Event/Xposition1=${x[1]} -overwrite Event/Yposition1=${y[1]} -overwrite Event/Xposition2=${x[2]} -overwrite Event/Yposition2=${y[2]} -overwrite Event/Xposition3=${x[3]} -overwrite Event/Yposition3=${y[3]} -overwrite Event/Xposition4=${x[4]} -overwrite Event/Yposition4=${y[4]} -overwrite Event/Xposition5=${x[5]} -overwrite Event/Yposition5=${y[5]} -overwrite Event/Xposition6=${x[6]} -overwrite Event/Yposition6=${y[6]} > ${n}-${ne}-${seed}-${project}-${dr}.2.out"
					echo $cmd >> .cmdfile
					echo "./mvstp.sh" >>.cmdfile
					echo "./mvdot.sh" >>.cmdfile
					cmd="$basecmd -gen $n ${nodeType} $model -batch -overwrite Event/NumEvents=$ne -overwrite Population=$population -overwrite Generations=$generations -overwrite Objective=2 -overwrite FFactor=0.5 -overwrite KFactor=1.5 -overwrite fixedSeed=$seed -overwrite dimX=$dimx -overwrite dimY=$dimy -overwrite Event/DataRate=$dr -overwrite Event/Xposition1=${x[1]} -overwrite Event/Yposition1=${y[1]} -overwrite Event/Xposition2=${x[2]} -overwrite Event/Yposition2=${y[2]} -overwrite Event/Xposition3=${x[3]} -overwrite Event/Yposition3=${y[3]} -overwrite Event/Xposition4=${x[4]} -overwrite Event/Yposition4=${y[4]} -overwrite Event/Xposition5=${x[5]} -overwrite Event/Yposition5=${y[5]} -overwrite Event/Xposition6=${x[6]} -overwrite Event/Yposition6=${y[6]} > ${n}-${ne}-${seed}-${project}-${dr}.3.out"
					echo $cmd >> .cmdfile
					echo "./mvstp.sh" >>.cmdfile
					echo "./mvdot.sh" >>.cmdfile
				else
					cmd="$basecmd -gen $n ${nodeType} $model -batch -overwrite Event/NumEvents=$ne -overwrite fixedSeed=$seed -overwrite dimX=$dimx -overwrite dimY=$dimy -overwrite Event/DataRate=$dr -overwrite Event/Xposition1=${x[1]} -overwrite Event/Yposition1=${y[1]} -overwrite Event/Xposition2=${x[2]} -overwrite Event/Yposition2=${y[2]} -overwrite Event/Xposition3=${x[3]} -overwrite Event/Yposition3=${y[3]} -overwrite Event/Xposition4=${x[4]} -overwrite Event/Yposition4=${y[4]} -overwrite Event/Xposition5=${x[5]} -overwrite Event/Yposition5=${y[5]} -overwrite Event/Xposition6=${x[6]} -overwrite Event/Yposition6=${y[6]} > ${n}-${ne}-${seed}-${project}-${dr}.out"
				fi
				#echo $cmd >> .cmdfile
			done
		done
	done

}

function show_help()
{
cat <<"HELP"
Use: ./simulation.sh -r <number_of_rounds> -e <number_of_events> [-E] [-s] [-i] [-d] [-D] [-S] [-g]
	-E	The algorithm is only executed for <number_of_events> events, instead of 1 to <number_of_events> times
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
for i in `seq 1 $rounds`
do
	seed=$RANDOM
	if [ $spt -eq 1 ]
	then
		function_exec_case1 "SPT" $seed $nevents $listevent
	fi

	if [ $infra -eq 1 ]
	then
		function_exec_case1 "Infra" $seed $nevents $listevent
	fi
	if [ $daarp -eq 1 ]
	then
		function_exec_case1 "DAARPMSWIM" $seed $nevents $listevent
	fi
	if [ $ddaarp -eq 1 ]
	then
		function_exec_case1 "DDAARP" $seed $nevents $listevent
	fi
	if [ $dst -eq 1 ]
	then
		function_exec_case1 "DST" $seed $nevents $listevent
	fi
	if [ $ga -eq 1 ]
	then
		function_exec_case1 "GA" $seed $nevents $listevent
	fi
done

