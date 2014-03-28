pushd %~dp0
start /LOW java -cp nmaps.jar newtonpath.tasks.BatchRunner %*
popd