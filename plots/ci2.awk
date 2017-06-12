#!/bin/awk -f
BEGIN {
}
{
	sum=0; sumsq=0; ci=0; sd=0; low=0; high=0; procsd=0;
	for (i=2; i<=NF; i++){
		sum+=$i;
		sumsq+=$i*$i;
	}
	procsd=NF-1;
	if  ( procsd > 0 ){
		mean=sum/procsd;
		if ( procsd > 1 && sumsq > procsd*mean*mean )
			sd=sqrt((sumsq-procsd*mean*mean)/(procsd-1.0));
		else
			sd=0;
		ci=1.96*sd/sqrt(procsd);
		#sum+=$1;
		#sumsq+=$1*$1;
		printf("%d %4.5f %4.5f %4.5f\n", $1, mean, ci, ci) >> out_file;
	}
}
END {
}

