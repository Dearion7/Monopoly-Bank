#!/bin/bash

javac -cp .:*.jar *.java
java -cp .:*.jar Server
