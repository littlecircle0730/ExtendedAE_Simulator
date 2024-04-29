#!/bin/bash

# Compile the Java program
javac BLEDiscSimulator.java

# Define parameters
params=("3" "10" "25" "50" "75" "100")
discProbList=(0.7 0.8 0.9)
latencyList=(10.0 30.0)
# chunkList=(1 2 3 4 5 6 7 8 9)
chunkList=(11)

########## TEST #########
# # params=("10")
# discProbList=(0.9)
# discProbList=(0.7)
# discProbList=(0.8)
# latencyList=(30.0)


# Loop over each parameter and run the simulation
for n_chunk in "${chunkList[@]}"; do
    for discProb in "${discProbList[@]}"; do
        for latency in "${latencyList[@]}"; do
            for param in "${params[@]}"; do
                java BLEDiscSimulator "./BLEndAE/ExtendedAE_${param}n_${discProb}_${latency}s_${n_chunk}chunk_blend.properties" \
                                    "./output/ExtendedAE_${param}n_${discProb}_${latency}s_${n_chunk}chunk_blend.log" \
                                    "./output_dc/ExtendedAE_${param}n_${discProb}_${latency}s_${n_chunk}chunk_blend.dc" \
                                    1000
            done
        done
    done
done