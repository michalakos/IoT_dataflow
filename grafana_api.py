#!/usr/bin/python3

# -*- coding: utf-8 -*-
from flask import Flask, make_response, jsonify
import database_api

app = Flask(__name__)

app.make_response("Hello there")
@app.route('/')
def hello_world():
    return 'Hello World!'

@app.route('/<sensor_id>/<use_case>')
def sensor_data(sensor_id, use_case):
    result = []
    sensor_id = sensor_id.replace('sensor','')
    
    if use_case == 'max':
        result = database_api.get_max(sensor_id)
    elif use_case == 'min':
        result = database_api.get_min(sensor_id)
    elif use_case == 'avg':
        result = database_api.get_avg(sensor_id)
    elif use_case == 'sum':
        result = database_api.get_sum(sensor_id)

    response = make_response(jsonify(result),200,)
    return response

@app.route('/lateEvents')
def late_events():
    response = make_response(jsonify(database_api.get_late()),200,)
    return response

if __name__ == '__main__':
    app.run(host='localhost', port=5000)