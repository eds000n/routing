#!/bin/awk -f
BEGIN {
	sum=0; sumsq=0; ci=0; sd=0; low=0; high=0;
}
{
	sum+=$1;
	sumsq+=$1*$1;
}
END {
	mean=sum/NR;
	if ( NR > 1 && sumsq > NR*mean*mean )
		sd=sqrt((sumsq-NR*mean*mean)/(NR-1.0));
	else
		sd=0;
	ci=1.96*sd/sqrt(NR);
	#low=mean-ci; high=mean+ci; #for gnuplot
	printf("%d %4.5f %4.5f %4.5f\n", x, mean, ci, ci) >> out_file;
}

