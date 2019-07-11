#!/bin/bash

javac -cp .:jdbc.jar:javax.websocket-client-api-1.1.jar:json-simple-1.1.jar:tyrus.jar *.java
java -cp .:jdbc.jar:javax.websocket-client-api-1.1.jar:json-simple-1.1.jar:tyrus.jar Server
