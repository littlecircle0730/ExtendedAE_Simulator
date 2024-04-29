#!/bin/bash

# Compile the Java program
javac BLEDiscSimulator.java

# Define parameters
params=("3" "10" "25" "50" "75" "100")
discProbList=(0.7 0.8 0.9)
latencyList=(10.0 30.0)

## TEST 
# params=("50")
# discProbList=(0.7)
# discProbList=(0.8)
# discProbList=(0.9)
# latencyList=(30.0)

# Loop over each parameter and run the simulation
for discProb in "${discProbList[@]}"; do
    for latency in "${latencyList[@]}"; do
        for param in "${params[@]}"; do
            java BLEDiscSimulator "./BLEndAE/AE_FixSize_${param}n_${discProb}_${latency}s_blend.properties" \
                                "./output/AE_FixSize_${param}n_${discProb}_${latency}s_blend.log" \
                                "./output_dc/AE_FixSize_${param}n_${discProb}_${latency}s_blend.dc" \
                                1000
        done
    done
done