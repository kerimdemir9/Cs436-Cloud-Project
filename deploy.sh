#!/bin/bash

# Copy build file into cloud server

scp -i "PRIVATE KEY" target/customerModel.transactionModel-0.0.1-SNAPSHOT.jar root@"IP_ADDRESS":~