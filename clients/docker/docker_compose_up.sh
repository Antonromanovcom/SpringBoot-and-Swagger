#!/usr/bin/env bash
{
    docker-compose up -d
} || {
    echo "WARNING: Insufficient permissions?"
}