#!/bin/sh

###
# #%L
# OW2 Chameleon - Core
# %%
# Copyright (C) 2009 - 2014 OW2 Chameleon
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###
PRG="$0"
# resolve relative/absolute symlinks
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="`dirname "$PRG"`/$link"
  fi
done
dir=`dirname $PRG`

if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi

if test "$1" = "status"; then
    if [ -f RUNNING_PID ]; then
        pid=`cat RUNNING_PID`
        if ps -p $pid > /dev/null
        then
             echo "[info] chameleon running in the process ${pid}"
             exit 0
        else
             echo "[error] the chameleon should be running in the process ${pid}, but the process does not exist"
             exit 1
        fi
    else
        echo "[info] No RUNNING_PID file. Chameleon not running"
        exit 3
    fi
fi

if test "$1" = "clean-all"; then
  rm -rf chameleon-cache
  rm -rf logs
  if [ $# -ne 1 ]
  then
   shift
  else
    echo "[info] Done!"
    exit 0
  fi
fi

if test "$1" = "stop"; then
  if [ -f RUNNING_PID ]; then
    echo "[info] Stopping chameleon (with PID `cat RUNNING_PID`)..."
    kill `cat RUNNING_PID`

    RESULT=$?
    if test "$RESULT" = 0; then
      echo "[info] Done!"
      rm RUNNING_PID
      exit 0
    else
      echo "[error] Failed ($RESULT)"
      exit ${RESULT}
    fi
  else
    echo "[error] No RUNNING_PID file. Is this chameleon running?"
    exit 1
  fi
fi


# Check if the RUNNING_PID file is not there already
if [ -f RUNNING_PID ]; then
    echo "[error] RUNNING_PID existing. Is this chameleon already running?"
    exit 1
fi

#CLASSPATH=$(JARS=("bin"/*.jar); IFS=:; echo "${JARS[*]}")
for i in `ls ./bin/*.jar`
do
  CLASSPATH=${CLASSPATH}:${i}
done

if test "$1" = "--interactive"; then
    "$JAVA" -cp ${CLASSPATH} ${JVM_ARGS} -Dchameleon.home=$dir org.ow2.chameleon.core.Main "$@"
else
    "$JAVA" -cp ${CLASSPATH} ${JVM_ARGS} -Dchameleon.home=$dir org.ow2.chameleon.core.Main "$@" &
    echo $! > RUNNING_PID
fi

