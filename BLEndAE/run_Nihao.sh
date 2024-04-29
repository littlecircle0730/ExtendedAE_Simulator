#!/bin/bash

# Compile the Java program
javac BLEDiscSimulator.java

# Define parameters
params=("3" "10" "25" "50" "75" "100")
discProbList=(0.7 0.8 0.9)

# Loop over each parameter and run the simulation
for param in "${params[@]}"; do
    for discProb in "${discProbList[@]}"; do
        java BLEDiscSimulator "./BLEndAE/AE_${param}n_${discProb}_nihao.properties" \
                            "./output/AE_${param}n_${discProb}_nihao.log" \
                            "./output_dc/AE_${param}n_${discProb}_nihao.dc" \
                            100
    done
done
