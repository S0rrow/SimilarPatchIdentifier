#!/bin/bash
echo "Tracking the bash script execution result ..."
echo "project_dir : $1"
echo "email : $2"

# -z means empty
if [ -z "$1" ]; then
    echo "project_dir is empty"
    exit 1
fi

if [ -z "$2" ]; then
    echo "email is empty"
    exit 1
fi
# if email domain is not handong.ac.kr, exit
if [[ $2 != *@handong.ac.kr ]]; then
    echo "email domain is not handong.ac.kr"
    exit 1
fi
# -n means not empty
if [ -n "$3" ]; then
    echo "log file : $3"
    log_file=$3
fi

# variables
project_dir=$1
email=$2
sender="codemodel@ubuntu"

# start time
init_time=$(date +%s)

# bash script execution
cd $project_dir
python3 launcher.py --config SPI_tested-first.ini

# finish time
fin_time=$(date +%s)

# Path: main.sh
mail_title="Execution Complete Notification"

# send mail after run
if [ -n "$3" ]; then
    echo "bash script execution complete on $project_dir
====================================
Start time : $(date -d @$init_time)
Finish time : $(date -d @$fin_time)
====================================
Elapsed time : $(($fin_time - $init_time)) seconds
====================================" | mail -s "$mail_title" -aFrom:$sender -A "$log_file" "$email"
else
    echo "bash script execution complete on $project_dir
====================================
Start time   : $(date -d @$init_time)
Finish time  : $(date -d @$fin_time)
Elapsed time : $(($fin_time - $init_time)) seconds
====================================" | mail -s "$mail_title" "$email"
fi
