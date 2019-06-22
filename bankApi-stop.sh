#!/bin/bash
# Grabs and kill a process from the pidlist that has the word bankApi

pid = `ps aux | grep bankApi | awk '{print $2}'`
kill -9 $pid
