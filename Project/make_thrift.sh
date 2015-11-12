#!/bin/bash

# Script for generating thrift classes from .thrift definition files

THRIFT_FOLDER=./thrift
GEN_FOLDER=./thrift-gen

if [ -e $GEN_FOLDER ]
then
  chmod -R u+w $GEN_FOLDER
  rm -rf $GEN_FOLDER
fi
mkdir $GEN_FOLDER

for thrift in  $THRIFT_FOLDER/*.thrift
do
  thrift --gen java -out $GEN_FOLDER $thrift
done

chmod -R -w $GEN_FOLDER/*
