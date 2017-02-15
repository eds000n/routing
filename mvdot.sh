#!/bin/bash

if [ ! -d dot ]
then
	mkdir dot
fi
instance=`ls -trh *.out | tail -n 1`
mkdir dot/${instance}
mv gg*.dot dot/${instance}/
