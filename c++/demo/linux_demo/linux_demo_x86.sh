#!/bin/sh

function print_step()
{
    echo "********************************************"
    echo "$1"
    echo "********************************************"
}

print_step "Compile DelegateTest.cpp to DelegateTest"
g++ ../DelegateTest.cpp -o DelegateTest

print_step "Compile EventTest.cpp to EventTest"
g++ ../EventTest.cpp -o EventTest

print_step "Run ./DelegateTest"
./DelegateTest

print_step "Run ./EventTest"
./EventTest

