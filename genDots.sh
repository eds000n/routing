#!/bin/bash

for file in `ls gg*.dot`
do 
	neato -Tpdf $file -o $file.pdf; 
done

for file in `ls *.pdf` ; 
do 
	fname=`echo $file | cut -d. -f 1`
	pdftoppm -f 1 -rx 600 -ry 600 -scale-to 2500 -png $file $fname;
done

ffmpeg -framerate 30 -i gg"%04d"-1.png  -r 30 vpng.mp4
#mplayer -speed 0.05 vpng.mp4 

