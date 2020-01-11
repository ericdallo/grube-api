#!/bin/bash

set -ve

APP=grube-api
NGINX=nginx

docker pull $NGINX
if docker ps | awk -v app="NGINX" 'NR>1{  ($(NF) == NGINX )  }'; then
    docker stop "$NGINX" && docker rm -f "$NGINX"
fi

docker run --name $NGINX -d \
    -v /opt/nginx.conf:/etc/nginx/nginx.conf \
    --link $APP:$APP -p 80:80 $NGINX
