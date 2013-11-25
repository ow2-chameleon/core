#!/bin/sh
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

if test "$1" = "--interactive"; then
    "$JAVA" ${JVM_ARGS} -Dchameleon.home=$dir -jar bin/${project.artifactId}-${project.version}.jar "$@"
else
    "$JAVA" ${JVM_ARGS} -Dchameleon.home=$dir -jar bin/${project.artifactId}-${project.version}.jar "$@" &
    echo $! > RUNNING_PID
fi

