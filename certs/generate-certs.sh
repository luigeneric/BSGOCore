#!/bin/bash

openssl req -newkey rsa:2048 -nodes -keyout key.pem -out csr.pem -config san.cnf

openssl x509 -req -in csr.pem -signkey key.pem -days 365 \
  -extfile san.cnf -extensions v3_req -out cert.pem
