#!/bin/bash
# Plot ALL the metrics

#Usual plot
#===========
#./draw -T 0 -t titulo -x rotulo_x -y rotulo_y -s file.png file1.data
#file2.data file3.data ...

#Errorbar
#========
#./draw -T 1 -t titulo -x rotulo_x -y rotulo_y -s file.png file1.data
#file2.data file3.data ...

#Normal with steps (for ladder graph)
#====================================
#./draw -T 2 -t titulo -x rotulo_x -y rotulo_y -s file.png file1.data
#file2.data file3.data ...
#

#Usual normalized
#=================
#./draw -T 3 -t titulo -x rotulo_x -y rotulo_y -s file.png file1.data file2.data 
# fil3.data ...


#
# $1 number of events
# $2 density
# $3 number of nodes
# $4 algorithm
# $5 log file
# $6 out file
# $7 x-axis values
# $8 parameter: { datapackets:0, overhead: 1, efficiency: 2, tree cost:3 }
function get_confidence_interval(){
	bn=`basename $5`
	tmpfile=".tmp_$bn"
	outfile=$6
	if [ -e $tmpfile ]
	then
		rm $tmpfile
	fi
	if [ $8 -eq 0 ]
	then
		awk -v ne=$1 -v d=$2 -v n=$3 -v alg=$4 '{if($2==ne && $3==n && $12==d && $15==alg) print $7}' $5 > $tmpfile
	elif [ $8 -eq 1 ]; then
		awk -v ne=$1 -v d=$2 -v n=$3 -v alg=$4 '{if($2==ne && $3==n && $12==d && $15==alg) print $6}' $5 > $tmpfile
	elif [ $8 -eq 2 ]; then
		awk -v ne=$1 -v d=$2 -v n=$3 -v alg=$4 '{if($2==ne && $3==n && $12==d && $15==alg) print $6}' $5 > $tmpfile
	elif [ $8 -eq 3 ]; then
		awk -v ne=$1 -v d=$2 -v n=$3 -v alg=$4 '{if($2==ne && $3==n && $12==d && $15==alg) print $10}' $5 > $tmpfile
	fi
	lines=`cat $tmpfile | wc -l `
	if [ $lines -ge 1 ]
	then
		awk -f ci.awk -v x=$7 -v out_file=$outfile $tmpfile 
	else
		echo "No matching for { events:$1, density:$2, nodes:$3, algorithm:$4, file:$5 }"
	fi
}

#plot lifetime
#This needs the *.outs of the instances
function plot_lifetime(){
	if [ -e lifetime.data ]
	then
		rm lifetime.data
	fi
	rm scenariol.*.data	#clean lifetime data
	rm scenarioc.*.data	#clean disconnected nodes data
	#for file in `ls outs/*.out | grep GA`
	files_btmp=""
	files_ctmp=""
	for file in $@
	do
		bfile=`basename $file`
		n=`echo $bfile | cut -d"-" -f1`
		ne=`echo $bfile | cut -d"-" -f2`
		de=`echo $bfile | cut -d"-" -f3`
		#seed=`echo $bfile | cut -d"-" -f4`
		dr=`echo $bfile | cut -d"-" -f5`
		st=`echo $bfile | cut -d"-" -f6`
		alg=`echo $bfile | cut -d"-" -f7`
		#rest=`echo $bfile | cut -d"-" -f7`
		#preprocessing=`echo $bfile | cut -d"-" -f7 | cut -d"." -f1`
		#at=`echo $bfile | cut -d"-" -f7 | cut -d"." -f2` #algorithm type (1: normal, 2:node weighted, 3:normalized node weighted)
		total=`echo $bfile | cut -d"-" -f1`
		bfile="scenariol.${n}${ne}${de}${dr}${alg}.data"
		cfile="scenarioc.${n}${ne}${de}${dr}${alg}.data"
		#bfile="scenariol.${n}${ne}${de}${alg}${dr}${at}-${preprocessing}.data"
		#cfile="scenarioc.${n}${ne}${de}${alg}${dr}${at}-${preprocessing}.data"
		if [ -e ".tmp.$bfile" ] ; then
			rm ".tmp.$bfile"
			rm ".tmp.$cfile"
		else
			files_btmp="$files_btmp ${bfile}"
			files_ctmp="$files_ctmp ${cfile}"
		fi
		echo 0 $total >> ".tmp.$bfile"
		echo 0 $total >> ".tmp.$cfile"
		#read m
		IFS=$'\n'
		for line in `grep "connected nodes" $file`			## processing disconnected nodes
		do
			sim_time=`echo $line | cut -d" " -f2`
			cur_total=`echo $line | cut -d" " -f11`		#UNCOMMENT ME!!
			#cur_total=`echo $line | cut -d" " -f9`			#COMMENT ME!
			echo $sim_time $cur_total >> ".tmp.$cfile"
		done
		#read m
		for line in `grep "reported itself as dead with battery of" $file`	## processing dead nodes
		do
			let "total-=1"
			t=`echo $line | cut -d"]" -f1 | cut -d"[" -f2`
			echo $t $total >> ".tmp.$bfile"
		done

		declare -a xticks=(`seq 2000 500 21000`);
		IFS=' '

		i=0
		val=$total
		while read line 
		do
			tmp=`echo $line | xargs`
			c_time=`echo $tmp | cut -d" " -f1`

			if [ $i -ge ${#xticks[@]} ]
			then
				break
			fi
			cd_time=${xticks[${i}]}
			cmp=`echo "$c_time"'>='"${cd_time}" | bc -l`
			while [ $cmp -eq 1 ]
			do
				echo ${xticks[${i}]} ${val} >> .tmp${bfile}
				let "i+=1";
				if [ $i -ge ${#xticks[@]} ]
				then
					break
				fi
				cd_time=${xticks[${i}]}
				cmp=`echo "$c_time"'>='"${cd_time}" | bc -l`
			done
			val=`echo $tmp | cut -d" " -f2`
		done < ".tmp.$bfile"
		if [ -e ${bfile} ]
		then
			awk '{print $2}' .tmp${bfile} > .tmp
			paste ${bfile} .tmp > .tmp2
			mv .tmp2 ${bfile}
			rm .tmp
			rm .tmp${bfile}
		else
			mv .tmp${bfile} ${bfile}
		fi
		while read line
		do
			tmp=`echo $line | xargs` 
			echo $tmp >> .tmp
		done < ${bfile}
		mv .tmp ${bfile}

		j=0
		val=$total
		while read line
		do
			tmp=`echo $line | xargs`
			c_time=`echo $tmp | cut -d" " -f1`
			if [ $j -ge ${#xticks[@]} ]
			then
				break
			fi
			cd_time=${xticks[${j}]}
			cmp=`echo "$c_time"'>='"${cd_time}" | bc -l`
			while [ $cmp -eq 1 ]
			do
				echo ${xticks[${j}]} ${val} >> .tmp${cfile}
				let "j+=1"
				if [ $j -ge ${#xticks[@]} ]
				then
					break
				fi
				cd_time=${xticks[${j}]}
				cmp=`echo "$c_time"'>='"${cd_time}" | bc -l`
			done
			val=`echo $tmp | cut -d" " -f2`
		done < ".tmp.$cfile"
		if [ -e ${cfile} ]
		then
			awk '{print $2}' .tmp${cfile} > .tmp
			paste ${cfile} .tmp > .tmp2
			mv .tmp2 ${cfile}
			rm .tmp
			rm .tmp${cfile}
		else
			mv .tmp${cfile} ${cfile}
		fi
		while read line
		do
			tmp=`echo $line | xargs`
			echo $tmp >> .tmp
		done < ${cfile}
		mv .tmp ${cfile}
	done
	for file in $files_btmp
	do
		awk -f ci2.awk -v out_file=".tmp$file" $file 
		mv ".tmp$file" $file
	done
	for file in $files_ctmp
	do
		awk -f ci2.awk -v out_file=".tmp$file" $file
		mv ".tmp$file" $file
	done

	./draw.py -T 5 -t "Lifetime" -x "Lifetime (s)" -y "% of dead nodes" -s lifetime.png $files_btmp
	echo 'Plotting with ./draw.py -T 5 -t "Lifetime" -x "Lifetime (s)" -y "% of dead nodes" -s lifetime.png' "$files_btmp"
	./draw.py -T 5 -t "Connectivity time" -x "Lifetime (s)" -y "% of disconnected nodes" -s disconnected.png $files_ctmp
	echo './draw.py -T 5 -t "Connectivity time" -x "Lifetime (s)" -y "% of disconnected nodes" -s disconnected.png' "$files_ctmp"
	rm .tmp.scenariol.*.data
	rm .tmp.scenarioc.*.data
}

#plot {datapackets, overhead, eff, tree_cost} vs network size
#scenario2
function plot_ne(){
	tmps_d=""
	tmps_o=""
	tmps_e=""
	tmps_t=""
	events="1 2 3 4 5 6"
	density=42
	n=1024

	for logfile in $@
	do
		for alg in `awk '{print $15}' $logfile  | sort | uniq`
		do
			scenario2_d_file="d_scenario2.${alg}.data"
			scenario2_o_file="o_scenario2.${alg}.data"
			#scenario2_e_file="e_scenario2.${alg}.data"
			scenario2_t_file="t_scenario2.${alg}.data"
			if [ -e $scenario2_d_file ]
			then
				rm $scenario2_d_file
			fi
			if [ -e $scenario2_o_file ]
			then
				rm $scenario2_o_file
			fi
			#if [ -e $scenario2_e_file ]
			#then
			#	rm $scenario2_e_file
			#fi
			if [ -e $scenario2_t_file ]
			then
				rm $scenario2_t_file
			fi
		done
	done
	for logfile in $@
	do
		for alg in `awk '{print $15}' $logfile  | sort | uniq`
		do
			scenario2_d_file="d_scenario2.${alg}.data"
			scenario2_o_file="o_scenario2.${alg}.data"
			scenario2_e_file="e_scenario2.${alg}.data"
			scenario2_t_file="t_scenario2.${alg}.data"
			tmps_d="$tmps_d ${scenario2_d_file}"
			tmps_o="$tmps_o ${scenario2_o_file}"
			#tmps_e="$tmps_e ${scenario2_e_file}"
			tmps_t="$tmps_t ${scenario2_t_file}"
			for ne in $events
			do
				get_confidence_interval $ne $density $n $alg $logfile $scenario2_d_file $ne 0
				get_confidence_interval $ne $density $n $alg $logfile $scenario2_o_file $ne 1
				#get_confidence_interval $ne $density $n $alg $logfile $scenario2_e_file $ne 2
				get_confidence_interval $ne $density $n $alg $logfile $scenario2_t_file $ne 3
			done
		done
	done
	./draw.py -T 1 -t "Datapackets" -x "# of events" -y "Packets (\$ \\times 10^3\$)" -s ne_packets.png $tmps_d
	./draw.py -T 1 -t "Overhead" -x "# of events" -y "Packets (\$\\times 10^3\$)" -s ne_overhead.png $tmps_o
	#./draw.py -T 1 -t "Efficiency" -x "# of events" -y "Packets (\$\\times 10^3\$)" -s ne_eff.png $tmps_e
	./draw.py -T 1 -t "Tree Cost" -x "# of events" -y "# of Steiner Nodes" -s ne_treesize.png $tmps_t
}

#plot {datapackets, overhead, eff, tree_cost} vs number of events
#scenario1
function plot_ns(){
	tmps_d=""
	tmps_o=""
	tmps_e=""
	tmps_t=""
	ne=6
	density=40
	nodes="128 256 512 1024 2048"

	for logfile in $@
	do
		for alg in `awk '{print $15}' $logfile  | sort | uniq`
		do
			scenario1_d_file="d_scenario1.${alg}.data"
			scenario1_o_file="o_scenario1.${alg}.data"
			#scenario1_e_file="e_scenario1.${alg}.data"
			scenario1_t_file="t_scenario1.${alg}.data"
			if [ -e $scenario1_d_file ]
			then
				rm $scenario1_d_file
			fi
			if [ -e $scenario1_o_file ]
			then
				rm $scenario1_o_file
			fi
			#if [ -e $scenario1_e_file ]
			#then
			#	rm $scenario1_e_file
			#fi
			if [ -e $scenario1_t_file ]
			then
				rm $scenario1_t_file
			fi

		done
	done

	for logfile in $@
	do
		for alg in `awk '{print $15}' $logfile  | sort | uniq`
		do
			scenario1_d_file="d_scenario1.${alg}.data"
			scenario1_o_file="o_scenario1.${alg}.data"
			#scenario1_e_file="e_scenario1.${alg}.data"
			scenario1_t_file="t_scenario1.${alg}.data"
			tmps_d="$tmps_d ${scenario1_d_file}"
			tmps_o="$tmps_o ${scenario1_o_file}"
			#tmps_e="$tmps_e ${scenario1_e_file}"
			tmps_t="$tmps_t ${scenario1_t_file}"
			for n in $nodes
			do 
				get_confidence_interval $ne $density $n $alg $logfile $scenario1_d_file $n 0
				get_confidence_interval $ne $density $n $alg $logfile $scenario1_o_file $n 1
				#get_confidence_interval $ne $density $n $alg $logfile $scenario1_e_file $n 2
				get_confidence_interval $ne $density $n $alg $logfile $scenario1_t_file $n 3
			done
		done
	done
	./draw.py -T 1 -t "Datapackets" -x "# of nodes" -y "Packets (\$ \\times 10^3\$)" -s ns_packets.png $tmps_d
	echo 'Plotting with ./draw.py -T 1 -t "Datapackets" -x "# of nodes" -y "Packets (\$ \\times 10^3\$)" -s ns_packets.png' "$tmps_d"
	./draw.py -T 1 -t "Overhead" -x "# of nodes" -y "Packets (\$ \\times 10^3\$)" -s ns_overhead.png $tmps_o
	echo 'Plotting with ./draw.py -T 1 -t "Overhead" -x "# of nodes" -y "Packets (\$ \\times 10^3\$)" -s ns_overhead.png' "$tmps_o"
	#./draw.py -T 1 -t "Efficiency" -x "# of events" -y "Packets (x10^3)" -s ns_eff.png $tmps
	./draw.py -T 1 -t "Tree Cost" -x "# of nodes" -y "# of Steiner Nodes" -s ns_treesize.png $tmps_t
	echo 'Plotting with ./draw.py -T 1 -t "Tree Cost" -x "# of nodes" -y "# of Steiner Nodes" -s ns_treesize.png' "$tmps_t"
}

#plot {datapackets, overhead, eff, tree_cost} vs density
#scenario3
function plot_density(){
	tmps_d=""
	tmps_o=""
	tmps_e=""
	tmps_t=""
	ne=6
	density="20 30 42"
	n=1024
	for logfile in $@
	do
		for alg in `awk '{print $15}' $logfile  | sort | uniq`
		do
			scenario3_d_file="d_scenario3.${alg}.data"
			scenario3_o_file="o_scenario3.${alg}.data"
			#scenario3_e_file="e_scenario3.${alg}.data"
			scenario3_t_file="t_scenario3.${alg}.data"
			if [ -e $scenario3_d_file ]
			then
				rm $scenario3_d_file
			fi
			if [ -e $scenario3_o_file ]
			then
				rm $scenario3_o_file
			fi
			#if [ -e $scenario3_e_file ]
			#then
			#	rm $scenario3_e_file
			#fi
			if [ -e $scenario3_t_file ]
			then
				rm $scenario3_t_file
			fi
		done
	done

	for logfile in $@
	do
		for alg in `awk '{print $15}' $logfile  | sort | uniq`
		do
			scenario3_d_file="d_scenario3.${alg}.data"
			scenario3_o_file="o_scenario3.${alg}.data"
			#scenario3_e_file="e_scenario3.${alg}.data"
			scenario3_t_file="t_scenario3.${alg}.data"
			tmps_d="$tmps_d ${scenario3_d_file}"
			tmps_o="$tmps_o ${scenario3_o_file}"
			tmps_e="$tmps_e ${scenario3_e_file}"
			tmps_t="$tmps_t ${scenario3_t_file}"
			for d in $density
			do
				get_confidence_interval $ne $d $n $alg $logfile $scenario3_d_file $d 0
				get_confidence_interval $ne $d $n $alg $logfile $scenario3_o_file $d 1
				#get_confidence_interval $ne $d $n $alg $logfile $scenario3_e_file $d 2
				get_confidence_interval $ne $d $n $alg $logfile $scenario3_t_file $d 3
			done
		done
	done
	./draw.py -T 1 -t "Datapackets" -x "Density" -y "Packets (\$ \\times 10^3\$)" -s de_packets.png $tmps_d
	./draw.py -T 1 -t "Overhead" -x "density" -y "Packets (\$ \\times 10^3\$)" -s de_overhead.png $tmps_o
	#./draw.py -T 1 -t "Efficiency" -x "density" -y "Packets (x10^3)" -s de_eff.png $tmps_e
	./draw.py -T 1 -t "Tree Cost" -x "density" -y "# of Steiner nodes" -s de_treesize.png $tmps_t
}

#plot rsph 
function plot_rsph(){
	./draw.py -T 2 -t "Impact of Repetitive Shortest Path Heuristic" -x "Generation" -y "Objective value" -s rsph.png rsph.data norsph.data
}

function plot_scenario7(){
	echo ""
}

#DOING
function plot_dtime(){
	tmps_t=""
	for logfile in $@
	do
		bfile=`basename $logfile`
		n=`echo $bfile | cut -d"-" -f1`
		ne=`echo $bfile | cut -d"-" -f2`
		de=`echo $bfile | cut -d"-" -f3`
		#seed=`echo $bfile | cut -d"-" -f4`
		dr=`echo $bfile | cut -d"-" -f5`
		st=`echo $bfile | cut -d"-" -f6`
		alg=`echo $bfile | cut -d"-" -f7`
		bfile="scenariod.${n}${ne}${de}${dr}${alg}.data"
		if [ -e $bfile ]
		then
			rm $bfile
			#rm .tmp$bfile
		fi
		cp .dtimes ${bfile}
		tmps_t="$tmps_t .tmp${bfile}"
	done
	tmps_t=`echo $tmps_t | xargs -n1 | sort -u | xargs`
	for logfile in $@
	do
		bfile=`basename $logfile`
		n=`echo $bfile | cut -d"-" -f1`
		ne=`echo $bfile | cut -d"-" -f2`
		de=`echo $bfile | cut -d"-" -f3`
		#seed=`echo $bfile | cut -d"-" -f4`
		dr=`echo $bfile | cut -d"-" -f5`
		st=`echo $bfile | cut -d"-" -f6`
		alg=`echo $bfile | cut -d"-" -f7`
		bfile="scenariod.${n}${ne}${de}${dr}${alg}.data"
		grep DeliveryTimeAwareMessage $logfile | sed "s/[a-zA-Z]\|\[\|\]\| *//g" > .tmp
		sed -i "s/:/ /g" .tmp
		#lines=`wc -l .tmp | cut -d" " -f1` 
		awk -v initt=2000 -v step=400 '
		BEGIN{max=0}
		{
			if ( $1 > initt + step ){
				print initt" "max;
				initt=initt+step;
				max=0;
			}
			else
				if($2>max)
					max=$2; 
		}
		END{}
		#END{print time" "max}
		' .tmp > .tmp1
		# .tmp1 has two columns, the first is the time and the second is the delivery time
		rm .tmp

		#paste ${bfile} .tmp > .tmp2
		if [ ! -e .dtimes ]
		then
			cat .tmp1 | cut -d" " -f1 > .dtimes
		fi
		cat .tmp1 | cut -d" " -f2 > .tmp2
		# .tmp2 only has delivery time for the current log file
		paste ${bfile} .tmp2 > .tmp3
		mv .tmp3 ${bfile}
		# ${bfile} has the accumulated values for delivery time
		rm .tmp1
		rm .tmp2
		
		if [ -e .tmp$bfile ]
		then
			rm .tmp$bfile
		fi
		awk -f ci2.awk -v out_file=".tmp$bfile" $bfile 
	done
		#rm .dtimes
	./draw.py -T 1 -t "Delivery Time" -x "Simulation Time" -y "Delivery Time" -s dtime.png $tmps_t
	echo "./draw.py -T 1 -t "Delivery Time" -x "Simulation Time" -y "Delivery Time" -s dtime.png $tmps_t"
}

function show_help(){
cat <<HELP
Usage: ./plotAll.sh GALog.txt SPTLog.txt DAARPMSWIM.txt
Plots all the graphic (the 6 scenarios) for GALog.txt SPTLog.txt SPTLog.txt DAARPMSWIM.txt
All the plots are in the same graphic for comparison
HELP
}


if [ $# -eq 0 ]
then
	echo "you need one argument at last"
	show_help
	exit 0
fi
#plot_lifetime $@
plot_dtime $@
#plot_ns $@
#plot_ne $@
#plot_density $@
#plot_rsph
