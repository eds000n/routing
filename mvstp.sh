#!/bin/bash

if [ ! -d stp ]
then
	mkdir stp
fi
instance=`ls -trh *.out | tail -n 1`
mkdir stp/${instance}
mv ff*.stp stp/${instance}/
