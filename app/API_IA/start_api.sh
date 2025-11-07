#!/bin/bash
cd ~/Documentos/profesor_Oak
sudo pkill -f uvicorn
sleep 2
uvicorn api_pokemon:app --host 0.0.0.0 --port 8002 --reload
