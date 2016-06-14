#!/bin/bash

#bash -p 2 -n 10 -i input.txt

if [ $# -ne 6 ]
then
    echo "Usage: bash run.sh -p 2 -n 10 -i input.txt"
    exit 0;
fi


gen_pro=$2
total=$4
flag=$5
file=$6

javac -sourcepath ./src/ -d ./bin/ ./src/**/**/**/*.java


tab=" --tab-with-profile=Default"
options=()

cmd="java -classpath  bin: com.POD.Server.Server"
space=" "

cmd_array=()
title=()

for i in `seq 1 $gen_pro`;
do
    temp1=$cmd$space$i$space$total$space$flag$space$file
    cmd_array[i]=$temp1
    title[i]="Terminal"$i
done

forwarder=`expr $gen_pro + 1`

for i in `seq $forwarder $total`;
do
    temp1=$cmd$space$i$space$total
    cmd_array[i]=$temp1
    title[i]="Terminal"$i
done

for i in `seq 1 $total`;
do
    echo ${cmd_array[i]} #$space${title[i]}
done



for i in `seq 1 $total`;
do 
    options+=($tab --title="${title[i]}"  -e "bash -c \"${cmd_array[i]} ; bash\"")
done

gnome-terminal "${options[@]}"



#echo "shesh"
exit 0

