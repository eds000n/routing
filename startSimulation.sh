#!/bin/bash
#Start ALL the scenarios of simulation

DATARATE=5
F_FACTOR=1
K_FACTOR=1

# $1: project name
# $2: seed
# $3: number of events
# $4: number of nodes
# $5: density
# $6: init_time
function exec_simulation()
{
	alg_list="$1"	#SPT, InFRA, DAARP, DDAARP, DST, GA
	seed=$2
	#echo "Alg list $alg_list"
	#echo "Seed: $seed"
	numEvents=$3
	nodes=$4
	density=$5
	#preprocessing=$6
	start_time=$6
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
	event_end_times="-overwrite SimTime=25250 -overwrite Event/EventEnd=25100"
	init_time=1500
	event_start_times=""
	for ne in $events
	do
		x[$ne]=$((${xi[$ne]}%$dimx))
		y[$ne]=$((${yi[$ne]}%$dimy))
		cmd_events_positions="${cmd_events_positions} -overwrite Event/Xposition${ne}=${x[$ne]} -overwrite Event/Yposition${ne}=${y[$ne]}"
		event_end_times="${event_end_times} -overwrite Event/EventEnd${ne}=25500"
		if [ $start_time -eq 0 ]
		then
			init_time=$(($init_time+500))
		else
			init_time=$(($RANDOM%$start_time))
			init_time=$(($init_time+2000))
		fi
		event_start_times="${event_start_times} -overwrite Event/EventStart${ne}=${init_time}"
	done
	#for ne in $events
	#do
	ne=$numEvents
	dr=$DATARATE

	for project in $alg_list #SPT, InFRA, DAARP, DDAARP, DST, GA
	do
		basecmd="java -Xmx8G -cp binaries/bin sinalgo.runtime.Main -project $project"
		if [ $project == "SPT" ]; then
			nodeType="SPT:SPTNode"
		elif [ $project == "Infra" ]; then
			nodeType="Infra:InfraNode"
		elif [ $project == "DAARPMSWIM" ]; then
			nodeType="DAARPMSWIM:DAARPMSWIMNode"
		elif [ $project == "DDAARP" ]; then
			nodeType="DDAARP:DDAARPNode"
		elif [ $project == "DST" ]; then
			nodeType="DST:DSTNode"
		elif [ $project == "GA" ]; then
			nodeType="GA:GANode"
		elif [ $project == "HCCRFD" ]; then
			nodeType="HCCRFD:HCCRFDNode"
		fi

	
		if [ $project == "GA" ]; then
		preprocessing=0
			ga_cmd="-overwrite Population=$population -overwrite Generations=$generations -overwrite Preprocessing=$preprocessing"
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch ${ga_cmd} -overwrite Objective=1 -overwrite fixedSeed=$seed -overwrite Density=$density -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} ${event_start_times} ${event_end_times} > ${n}-${ne}-${density}-${seed}-${dr}-${start_time}-${project}0.out"
			echo $cmd >> .cmdfile
			echo "./mvstp.sh" >>.cmdfile
			echo "./mvdot.sh" >>.cmdfile
		preprocessing=1
			ga_cmd="-overwrite Population=$population -overwrite Generations=$generations -overwrite Preprocessing=$preprocessing"
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch ${ga_cmd} -overwrite Objective=1 -overwrite fixedSeed=$seed -overwrite Density=$density -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} ${event_start_times} ${event_end_times} > ${n}-${ne}-${density}-${seed}-${dr}-${start_time}-${project}1.out"
			echo $cmd >> .cmdfile
			echo "./mvstp.sh" >>.cmdfile
			echo "./mvdot.sh" >>.cmdfile
			#cmd="$basecmd -gen $nodes ${nodeType} $model -batch ${ga_cmd} -overwrite Objective=2 -overwrite FFactor=0.1 -overwrite KFactor=2 -overwrite fixedSeed=$seed -overwrite Density=$density -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} ${event_end_times}  > ${n}-${ne}-${density}-${seed}-${dr}-${project}2.out"
			#echo $cmd >> .cmdfile
			#echo "./mvstp.sh" >>.cmdfile
			#echo "./mvdot.sh" >>.cmdfile
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch ${ga_cmd} -overwrite Objective=3 -overwrite FFactor=0.5 -overwrite KFactor=1.5 -overwrite fixedSeed=$seed -overwrite Density=$density -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} ${event_start_times} ${event_end_times} > ${n}-${ne}-${density}-${seed}-${dr}-${start_time}-${project}3.out"
			echo $cmd >> .cmdfile
			echo "./mvstp.sh" >>.cmdfile
			echo "./mvdot.sh" >>.cmdfile
		else
			cmd="$basecmd -gen $nodes ${nodeType} $model -batch -overwrite fixedSeed=$seed -overwrite Density=$density -overwrite Event/DataRate=$dr ${cmd_dimensions} ${cmd_events_positions} ${event_start_times} ${event_end_times} > ${n}-${ne}-${density}-${seed}-${dr}-${start_time}-${project}.out"
			echo $cmd >> .cmdfile
		fi
	done
}

# Scenario1: fixing number of events and density; varying the number of nodes 
# density=42 , ne=6, nodes={128,256,512,1024,2048}
function scenario1(){
	echo "#scenario1: varying nodes $1 $2" >> .cmdfile
	ne=6
	density=40
	nodes="128 256 512 1024 2048"
	for n in $nodes 
	do
		exec_simulation "$1" $2 $ne $n $density 0
		#exec_simulation "$1" $2 $ne $n $density 0
	done
}

# Scenario2: fixing number of nodes and density; variying number of events
# density=42 , ne={1,2,3,4,5,6} , nodes=1024
function scenario2(){
	echo "#scenario2: varying number of events $1 $2" >> .cmdfile
	density=42
	n=1024
	events="1 2 3 4 5" #6 is already included in scenario1
	for ne in $events
	do
		exec_simulation "$1" $2 $ne $n $density 0
	done
}

# Scenario3: fixing events nodes; varying density
# density={20,30,42} , ne=6, nodes=1024
function scenario3(){
	echo "#scenario3: vaying density $1 $2" >> .cmdfile
	density="10 20 30" #42 is already included in scenario1
	n=1024
	ne=6
	for d in $density
	do
		exec_simulation "$1" $2 $ne $n $d 0
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
	exec_simulation "$1" $2 $ne $nodes $density 0
}

# Scenario7: impact of RSPH (with and without it)
# ONLY FOR GA
# density=42, ne=6, nodes={128,256,512,1024,2048}
function scenario7(){
	if [ "$1" == "GA" ]
	then
		echo "#scenario7: impact of RSPH $1 $2" >> .cmdfile
		density=42		#the denser the more challenguing for RSPH
		ne=6
		#nodes="128 256 512 1024 2048"
		nodes="1024"
		for n in $nodes 
		do
			exec_simulation "$1" $2 $ne $n $density 0
			#exec_simulation "$1" $2 $ne $n $density 0
		done
	fi
}

# Scenario8: impact of reductions (with and without them)
# ONLY FOR GA
# density={20,30,42}, ne=6, nodes=1024
function scenario8(){
	echo "#scenario8: impact of RSPH $1 $2" >> .cmdfile

}

# Scenario9: impact of init time of events for many densities
# density={10, 20, 30, 40} ne=6, nodes=1024, init_times={1h, 2h, 3h, 4h, 5h}
function scenario9(){
	echo "#scenario9: random event init times $1 $2" >> .cmdfile
	ne="6"
	n="512"
	density="10 20 30 40"
	for den in $density
	do
		#hours       1h   2h   3h    4h    5h"
		init_times="3600 7200 10800 14400 18000"
		for t in $init_times
		do
			exec_simulation "$1" $2 $ne $n $den $t
		done
	done
}

function scenarios(){
	a="$1"
	seed=$2
	scenario1 "$a" $seed 	#THIS ONE
	#scenario2 "$a" $seed
	scenario3 "$a" $seed	# AND THIS
	#scenario4 "$a" $seed
	#scenario5 "$a" $seed
	#scenario6 "$a" $seed
	#scenario7 "$a" $seed
	scenario9 "$a" $seed	#AND THIS
}

function show_help()
{
cat <<"HELP"
Use: ./simulation.sh -r <number_of_rounds> [-s] [-i] [-d] [-D] [-S] [-g] [-H] [-t <seeds_file>]
	-s	for SPT
	-i	for Infra
	-d	for DAARP
	-D	for DDAARP
	-S	for DST
	-g	for GA
	-H	for HCCRFD
	-t	for a list of seeds instead of random seeds. The file has one seed per line. When this option
		is activated, it overrides the number_of_rounds to the number of seed given in the file

The script creates a file named .cmdfile with the commands to run the simulation. If the file
exists, it is overwritten.

 Scenario1: fixing number of events and density; varying the number of nodes 
 Scenario2: fixing number of nodes and density; variying number of events
 Scenario3: fixing events nodes; varying density
 Scenario4: fixing sink position (NOT IMPLEMENTED YET)
 Scenario5: long duration of events (NOT IMPLEMENTED YET)
 Scenario6: standard scenario (for eventing ending perhaps)
 Scenario7: impact of RSPH (with and without it), ONLY FOR GA
 Scenario8: impact of reductions (with and without them), ONLY FOR GA
 Scenario9: impact of init time of events for many densities

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
hccrfd=0;
rounds=30;
seeds_file="";
nevents=6;
listevent=0;
while getopts "r:e:EsidDSgHc:ht:" opt;
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
		H)
			echo "running simulation for HCCRFD"
			hccrfd=1;
			;;
		r)
			rounds=$OPTARG;
			echo "number of rounds: $rounds"
			;;
		t)
			seeds_file=$OPTARG;
			echo "using seed file: $seeds_file"
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
if [ $hccrfd -eq 1 ]
then
	alg="${alg} HCCRFD"
fi

if [ "$seeds_file" == "" ]; then
	for i in `seq 1 $rounds`
	do
		seed=$RANDOM
		scenarios "$alg" $seed
		#for a in $alg
		#do
		#	scenarios $a $seed
		#done
	done

else

	if [ ! -e $seeds_file ] 
	then
		echo "$seeds_file does not exist"
		exit 1
	fi
	while read -r line 
	do
		seed=$line
		scenarios "$alg" $seed
		#for a in $alg
		#do
		#	scenarios $a $seed
		#done
	done < $seeds_file
fi


