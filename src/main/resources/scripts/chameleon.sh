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
      echo "[\033[31merror\033[0m] Failed ($RESULT)"
      exit $RESULT
    fi
  else
    echo "[\033[31merror\033[0m] No RUNNING_PID file. Is this chameleon running?"
    exit 1
  fi
fi


# Check if the RUNNING_PID file is not there already
if [ -f RUNNING_PID ]; then
    echo "[\033[31merror\033[0m] RUNNING_PID existing. Is this chameleon already running?"
    exit 1
fi

if test "$1" = "--interactive"; then
    "$JAVA" -Dchameleon.home=$dir -jar bin/${project.artifactId}-${project.version}.jar "$@"
else
    "$JAVA" -Dchameleon.home=$dir -jar bin/${project.artifactId}-${project.version}.jar "$@" &
    echo $! > RUNNING_PID
fi

