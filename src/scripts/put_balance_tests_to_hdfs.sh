#!/bin/env bash

export HADOOP_USER_NAME=hdfs
AUTO_TETSTS_DIR=/tmp/das-autotests

if [ ! -d $AUTO_TETSTS_DIR ]; then
	mkdir /tmp/das-autotests
fi

#put files to hdfs
cd $AUTO_TETSTS_DIR
cp /misc/das-autotests/*.gz .
tar -xzvf *.gz
hdfs dfs -rm -r /user/das/das-autotests/balances/*.sql && hdfs dfs -put sql/balances/*.sql /user/das/das-autotests/balances/ > $HOME/update_balance_tests_out.log 2>&1
rm -rf *

