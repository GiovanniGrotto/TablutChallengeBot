#!/bin/bash

#Line that guarantees a case-unsensitive behaviour
uppercolor=$(echo $1 | tr '[:lower:]' '[:upper:]')

ant sigmazero -Dargs="$uppercolor $2 $3"