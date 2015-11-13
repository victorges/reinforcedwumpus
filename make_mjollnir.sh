#!/bin/bash

cat Project/src/State.java > mjollnir.tmp
cat Project/src/QTable.java >> mjollnir.tmp

DATA=`cat $1 | tr '\n' ' '`
cat QTableStorage.java | sed -e "s/<data>/$DATA/" >> mjollnir.tmp

cat Project/src/QTableStorageManager.java >> mjollnir.tmp
cat Project/src/ClientLogic.java >> mjollnir.tmp

cat mjollnir.tmp | grep "^import " | sort | uniq
cat mjollnir.tmp | grep -v "^import "
rm mjollnir.tmp
