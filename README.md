# Akka-streams process example
### Status
[![Build Status](https://travis-ci.org/sothach/jetstream.png)](https://travis-ci.org/sothach/jetstream)
[![Coverage Status](https://coveralls.io/repos/github/sothach/jetstream/badge.svg?branch=master)](https://coveralls.io/github/sothach/jetstream?branch=master)

## Weather report
This app is an example of using Akka-streams, Akka-http to query a web-service, and process the results.
The objective is to demonstrate how Akka-streams provides an easy-to-read DSL, allowing business processes to be 
coded very much as they were white-boarded, but at the same time, taking care to manage concurrency and error handling
in a systematic manner.
### Prerequisites 
The target language is Scala version 2.12, and uses the build tool sbt 1.2.1.
Clone this repository in a fresh directory:
```git
% git clone git@bitbucket.org:royp/bluebus.git
```
Compile the example with the following command:
```sbtshell
% sbt clean compile
[info] Done compiling.
[success] Total time: 6 s, completed 12-Aug-2018 11:38:12
```
The only explicit library dependency outside of the Scala language environment is Databinder dispatch version 0.13.4

## App design

The logical process flow to query the weather at specified locations and parse the response is:
```
 +--------------+    +------+    +--------+    +--------+    +-----------+ 
 | buildRequest | -> | call | -> | accept | -> | parser | -> | extractor |             
 +--------------+    +------+    +--------+    +--------+    +-----------+  
```
and in code:
```scala
def lookup(town: String, country: String) =
 Source.single((town,country)) via buildRequest via call via accept via parser via diversion runWith Sink.seq
```
### Processing Stages
#### `buildRequest`
As input to the stage, receives otn & country names, building the HttpRequest object, from the configured URI and requested location(s)
#### `call`
Asynchronously executes the HTTP request supplied as its input, creating the HTTP response.  Depending upon the parallelism setting,
may perform multiple calls in parallel
#### `accept`
Interpret the HTTP response code, unpacking the payload
#### `parser`
Parse the JSON response received, building the domain objects
#### `extractor`
On success, extracts the weather domain objects and returns, or logs the error, if call failed

## Dependencies

| library        | version  | purpose           |
|----------------|----------|-------------------|
| `akka-stream`  |  2.5.14  | stream processing |
| `akka-http`    |  10.1.4  | http & webservice |
| `argonaut`     |   6.2.2  | JSON processing   |
| `airframe-log` |    0.54  | logging framework |
| `scopt`        |   3.7.0  | option processing |

## Configuration
The weather API end-point and API key are read from 

Obtain the API key from [OpenWeatherMap](https://openweathermap.org/appid) and create the configuraton in
`/resources/endpoint.properties`, for example:
```properties
weather-app-id=eabb12404d141ed6e8ee2193688178cb
weather-api=http://api.openweathermap.org/data/2.5/weather
```



## Testing
### Running the tests
Run the test suite to verify correct behaviour.  

From the command line:
```sbtshell
% sbt test
```
### Test Coverage Report
To measure test coverage, this app uses the 'scoverage' SBT plugin.
To create the report, rom the command line:
```sbtshell
% sbt coverage test coverageReport
```

## Author
* [Roy Phillips](mailto:phillips.roy@gmail.com)

## License
[![License](https://licensebuttons.net/l/by/3.0/88x31.png)](https://creativecommons.org/licenses/by/4.0/) 

(c) 2018 This project is licensed under Creative Commons License

[Attribution 4.0 International (CC BY 4.0)](file:LICENSE.md)
