JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
echo [debug.log]: =========> JAVA_HOME initiated
cd core/AllChangeCollector
./gradlew clean
./gradlew build
cd app/build/distributions
unzip app.zip
# cd /home/codemodel/turbstructor/M48A2