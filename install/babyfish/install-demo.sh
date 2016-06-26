#!/bin/bash

echo -n "Please choose database, o(Oracle, suggested) or h(HSQLDB): "
while [ 1 -eq 1 ]
do
    read database
    database=$(echo "$database" | tr '[A-Z]' '[a-z]')
    if [ "$database" = "o" ]; then
        break
    elif [ "$database" = "oracle" ]; then
        database="o"
        break
    elif [ "$database" = "h" ]; then
        break
    elif [ "$database" = "hsqldb" ]; then
        database="h"
        break
    else
        echo -n "Invalid choice, please enter your choice again: "
    fi
done

args=
if [ "$database" = "o" ]; then
    cd ../ojdbc
    ./install.sh
    cd ../babyfish
    read -p "Please enter the host name of Oracle(Nothing means 'localhost'): " hostName
    read -p "Please enter the port of Oracle(Nothing means '1521'): " port
    read -p "Please enter the sid of Oracle(Nothing means 'babyfish') " sid
    read -p "Please enter the user name of Oracle(Nothing means 'babyfish_demo'): " user
    read -p "Please enter the password of Oracle(Nothing means '123'): " password
    if [ "$hostName" = "" ]; then
        hostName=localhost
    fi
    if [ "$port" = "" ]; then
        port=1521
    fi
    if [ "$sid" = "" ]; then
        sid=babyfish
    fi
    if [ "$user" = "" ]; then
        user=babyfish_demo
    fi
    if [ "$password" = "" ]; then
        password=123
    fi
    args="-Doracle=jdbc:oracle:thin:@$hostName:$port:$sid -Doracle.user=$user -Doracle.password=$password"
fi

cd ../../demo
echo "Use maven to create eclipse projects with the arguments "[$args]
mvn eclipse:eclipse $args
echo "Use maven to compile and test projects with the arguments "[$args]
mvn clean install $args
cd ../install/babyfish

