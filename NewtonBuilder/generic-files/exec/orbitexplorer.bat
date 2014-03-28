pushd %~dp0
start /REALTIME java -jar nmaps.jar %*
popd
