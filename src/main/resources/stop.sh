echo "LGHV SND Stop..."
PID=$(<application.pid)

if [ -n "$PID" ]; then
    kill -15 "$PID" # 종료 신호(SIGTERM)를 보냅니다.
    echo "Waiting for process with PID $PID to exit gracefully..."
    # 프로세스가 종료될 때까지 대기합니다.
    while ps -p $PID > /dev/null; do
        sleep 1
    done

    # 프로세스가 종료되었는지 확인합니다.
    if ps -p "$PID" > /dev/null; then
        echo "Process with PID $PID did not exit gracefully. Killing forcefully..."
        kill -9 "$PID" # 종료되지 않았다면 강제로 종료합니다.
    else
        echo "Process with PID $PID exited gracefully."
    fi

    # Remove the PID file after killing the process
    rm -f application.pid
else
    echo "No PID found in application.pid file."
fi

