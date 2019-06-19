#!/bin/bash

javac -cp .:jdbc.jar *.java
java -cp .:jdbc.jar Server
