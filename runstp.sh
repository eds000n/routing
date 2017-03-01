#!/bin/bash

SCIP_STP_HOME="/home/edson/scipoptsuite-3.2.1/scip-3.2.1/applications/STP"

cd stp

cwd=`pwd`
for instance in `ls `
do
	cd ${instance}
	find `pwd` -name \*.stp | sort > .list_file
	test_file="${SCIP_STP_HOME}/check/testset/${instance}.test"
	if [ -e ${test_file} ]
	then
		rm ${test_file}
	fi
	c=0
	prev_file=`head -n 1 .list_file`
	echo $prev_file >> $test_file
	while read -r line 
	do
		if [ $c -eq 0 ]
		then
			let "c+=1"
			continue
		fi
		diff $prev_file $line >/dev/null
		if [ $? -eq 1 ]
		then
			echo $line >> $test_file
		fi
		prev_file=$line

		let "c+=1"
	done < .list_file

	cd ${SCIP_STP_HOME}
	make TEST=${instance} test
	cd $cwd
done



