# Akka-streams process example
### Status
[![Build Status](https://travis-ci.org/sothach/jetstream.png)](https://travis-ci.org/sothach/jetstream)
[![Coverage Status](https://coveralls.io/repos/github/sothach/jetstream/badge.svg?branch=master)](https://coveralls.io/github/sothach/jetstream?branch=master)

## Weather report
This app is an example of using Akka-streams, Akka-http to query a web-service, and to process the results.
It demonstrates how Akka-streams provides an easy-to-read DSL, allowing business processes to be 
coded very much as they were white-boarded, but at the same time, taking care to manage concurrency and error handling
in a systematic manner, making the application reliable and resilient.

Scala and Akka are a powerful framework for producing work-a-day applications, as a 21st century COBOL. 

Scala is a multi-faceted language, providing the tools needed for modern development: it's type system allows us to
readily define our domain concepts, in a domain-driven manner, whilst it's functional properties encourage a succinct
and powerful style of defining the logic, without becoming lost in syntax.

Akka builds on this, with a simple and scalable model for asynchronous programming, which is exploited by Akka-streams,
further reducing boilerplate code and helping us application developers to focus on creating elegant solutions to the
business requirements.

## App design
An interactive command-line interface is provided to exercise the process flow, prompting the user for a location 
whose weather should be queried, e.g., `Munich, de`, calling the weather process and displaying the current weather
conditions at that location.
```sbtshel
% sbt run
Please enter town/country: Skibbereen,IE
Current Weather in Skibbereen: broken clouds 16.0c wind: 5.7 kph NNW IE daylight: 05:34:13 to 19:44:23
Please enter town/country: q
```
The logical process flow to query the weather at specified locations and parse the response is:
```
              +--------------+    +------+    +--------+    +--------+  
(location) => | buildRequest | -> | call | -> | accept | -> | parser | => (weather report)           
              +--------------+    +------+    +--------+    +--------+   
```
and the code is a direct representation of this process flow:
```scala
val process = buildRequest via call via accept via parser

def lookup(town: String, country: String): Future[Seq[, ]] 
          = Source.single((town,country)) via process runWith Sink.seq
```
The value `process` is a blue-print for an asynchronous process that, provided a source or one or more location
tuples, will query the weather API for current weather conditions and render the results into domain objects,
as the process's output.

The sample `lookup` method connects this process to an input source, and instantiates the process blueprint,
returning the output as a sequence of weather reports.

The weather process flow can be used as-is, or combined with higher-level flows, to further process the results,
such as saving to a data store, sending to subscribers, etc.

### Processing Stages
#### `buildRequest`
As input to the stage, receives town & country names as tuple, building the `HttpRequest` object, from the configured URI and 
requested location(s) as its output
#### `call`
Asynchronously executes the HTTP request supplied as its input, creating the HTTP response.  
Depending upon the parallelism setting, may perform multiple calls in parallel (see property `stream-width`)

This stage uses a configurable dispatcher, (property `api-dispatcher`) should a different execution context be deemed appropriate, 
see discussion [here](http://doc.akka.io/docs/akka/current/dispatchers.html)
#### `accept`
Interpret the HTTP response code, unpacking the payload.  This stage must ensure that any response entity is 
fully consumed, preventing back-pressure on the underlying TCP connection
#### `parser`
Parse the JSON response received, building the domain objects

### Prerequisites 
The target language is Scala version 2.12, and uses the build tool sbt 1.2.1.
Clone this repository in a fresh directory:
```git
% git clone git@bitbucket.org:royp/jetstream.git
```
Compile the example with the following command:
```sbtshell
% sbt clean compile
[info] Done compiling.
[success] Total time: 6 s, completed 12-Aug-2018 11:38:12
```
The only explicit library dependency outside of the Scala language environment is Databinder dispatch version 0.13.4

## Dependencies

| library          | version  | purpose           |
|------------------|----------|-------------------|
| `akka-stream`    |  2.5.14  | stream processing |
| `akka-http`      |  10.1.4  | http & webservice |
| `argonaut`       |   6.2.2  | JSON processing   |
| `scala-loggging` |   3.9.0  | logging framework |
| `scopt`          |   3.7.0  | option processing |

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

[Attribution 4.0 International (CC BY 4.0)](LICENSE.md)
