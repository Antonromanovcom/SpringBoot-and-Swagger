#!/usr/bin/env bash
{
    echo "WARNING: removing postgres volumes"
    docker-compose down -v
} || {
    echo "WARNING: Insufficient permissions?"
}