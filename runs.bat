java -jar -Xms768M -Xmx1024M ./target/activepf-0.1-jar-with-dependencies.jar useAsyncSend useOptimizeAck producer disableSessionAsync msgSize 10 brokerURL "tcp://192.168.56.103:61616"