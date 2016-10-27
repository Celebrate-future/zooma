#!/bin/bash

base=${0%/*};

SOLR_HOME=$base/../solr-5-config


if [ -z $SOLR_DIR ]
    then
        echo '$SOLR_DIR not set - please set this to the location of your Solr installation' >&2
        exit 1;
    else
        echo "Starting Solr using $SOLR_HOME"
        $SOLR_DIR/bin/solr start -s $SOLR_HOME
fi

