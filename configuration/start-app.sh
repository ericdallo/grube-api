#!/bin/bash

docker rmi -f docker.pkg.github.com/ericdallo/grube-api/grube-api
docker pull docker.pkg.github.com/ericdallo/grube-api/grube-api
docker rm -f grube-api
docker run -d --name grube-api -p 8080:8080 docker.pkg.github.com/ericdallo/grube-api/grube-api
