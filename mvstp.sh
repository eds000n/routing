#!/bin/bash

SCIP_HOME="/home/edson/scipoptsuite-3.2.1/scip-3.2.1/applications/STP"

if [ ! -d stp ]
then
	mkdir stp
fi
instance=`ls -trh *.out | tail -n 1`
mkdir stp/${instance}
mv ff*.stp stp/${instance}/
