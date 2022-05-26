# $1 = http://example.com/repo.git
# $2 = directory where to clone
cd $2
if ! git clone $1
then
  echo "[debug.log] git clone "$1" failed at "$2
else
  echo "[debug.log] git clone "$1" successed at "$2
fi