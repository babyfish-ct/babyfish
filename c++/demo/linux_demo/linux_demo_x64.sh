#!/bin/sh

function print_step()
{
    echo "********************************************"
    echo "$1"
    echo "********************************************"
}

print_step "Compile DelegateTest.cpp to DelegateTest"
g++ -m64 ../DelegateTest.cpp -o DelegateTest

print_step "Compile EventTest.cpp to EventTest"
g++ -m64 ../EventTest.cpp -o EventTest

print_step "Run ./DelegateTest"
./DelegateTest

print_step "Run ./EventTest"
./EventTest

