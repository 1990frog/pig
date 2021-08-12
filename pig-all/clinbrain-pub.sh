#!/bin/bash
PORTS=(5001 5003 9999 3000 4000 5004)
MODULES=(monitor sentinel gateway auth upms-biz xxl-job-admin)
MODULE_NAMES=("monitor" "sentinel" "gateway" "auth" "upms-biz" "xxl-job-admin")
JARS=(pig-monitor.jar pig-sentinel-dashboard.jar pig-gateway.jar pig-auth.jar pig-upms-biz.jar pig-xxl-job-admin.jar)
JAR_PATH='./'
LOG_PATH='./logs'
start() {
  local MODULE=
  local MODULE_NAME=
  local JAR_NAME=
  local command="$1"
  local commandOk=0
  local count=0
  local okCount=0
  local PORT=0
  local PID=
  for((i=0;i<${#MODULES[@]};i++))
  do
    MODULE=${MODULES[$i]}
    MODULE_NAME=${MODULE_NAMES[$i]}
    JAR_NAME=${JARS[$i]}
    PORT=${PORTS[$i]}
    if [ "$command" == "all" ] || [ "$command" == "$MODULE" ];then
      commandOk=1
      count=0
      PID=`ps -ef |grep $(echo $JAR_NAME | awk -F/ '{print $NF}')|grep -v grep | awk '{print $2}'`
      if [ -n "$PID" ];then
        echo "$MODULE---$MODULE_NAME:was running,PID=$PID"
      else
        exec nohup java -jar $JAR_PATH/$JAR_NAME --server.port=$PORT >> $LOG_PATH/$MODULE.out &
        PID=`netstat -tunlp | grep $PORT | awk '{print $7}' | cut -d/ -f 1`
        while [ -z "$PID" ]
        do
          if (($count == 100));then
            echo "$MODULE---$MODULE_NAME:$(expr $count \* 1)s is not start,please check your program!"
            break
          fi
          count=$(($count+1))
          echo "$MODULE_NAME is starting please wait .................."
          PID=`netstat -tunlp | grep $PORT | awk '{print $7}' | cut -d/ -f 1`
	      sleep 3
        done
        okCount=$(($okCount+1))
        echo "$MODULE---$MODULE_NAME:has start success,PID=$PID"
      fi
    fi
    sleep 15s
  done
  if(($commandOk == 0));then
    echo "the second param enter eg:monitor|sentinel|gateway|auth|upms-biz|xxl-job-admin"
  else
    echo "............there has:$okCount server started..........."
  fi
}
 
stop() {
  local MODULE=
  local MODULE_NAME=
  local JAR_NAME=
  local command="$1"
  local commandOk=0
  local okCount=0
  local PID=
  for((i=0;i<${#MODULES[@]};i++))
  do
    MODULE=${MODULES[$i]}
    MODULE_NAME=${MODULE_NAMES[$i]}
    JAR_NAME=${JARS[$i]}
    if [ "$command" = "all" ] || [ "$command" = "$MODULE" ];then
      commandOk=1
      PID=`ps -ef |grep $(echo $JAR_NAME | awk -F/ '{print $NF}') | grep -v grep | awk '{print $2}'`
      until [ ! -n "$PID" ]
      do
		echo "$MODULE---$MODULE_NAME:prestart kill,PID=$PID"
		kill -9 $PID
		sleep 5s
		PID=`ps -ef |grep $(echo $JAR_NAME | awk -F/ '{print $NF}') | grep -v grep | awk '{print $2}'`
        if [ -n "$PID" ];then
            echo "$MODULE---$MODULE_NAME:success stop"
            okCount=$(($okCount+1))
        fi
      done
        echo "$MODULE---$MODULE_NAME:didn't run"
    fi
  done
  if (($commandOk == 0));then
    echo "the second param enter eg: monitor|sentinel|gateway|auth|upms-biz|xxl-job-admin"
  else
    echo "............there has:$okCount server stoped..........."
  fi
} 


case "$1" in
  start)
    start "$2"
  ;;
  stop)
    stop "$2"
  ;;
  restart)
    stop "$2"
    sleep 3s
    start "$2"
  ;;
  *)
    echo "the first param enter eg:start|stop|restart"
    exit 1
  ;;
esac
