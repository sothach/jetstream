# Akka-streams process example
### Status
[![Build Status](https://travis-ci.org/sothach/jetstream.png)](https://travis-ci.org/sothach/jetstream)
[![Coverage Status](https://coveralls.io/repos/github/sothach/jetstream/badge.svg)](https://coveralls.io/github/sothach/jetstream)

## Weather report

### Prerequisites 
The target language is Scala version 2.12, and uses the build tool sbt 1.2.1.
Clone this repository in a fresh directory:
```
% git clone git@bitbucket.org:royp/bluebus.git
```
Compile the example with the following command:
```
% sbt clean compile
[info] Done compiling.
[success] Total time: 6 s, completed 12-Aug-2018 11:38:12
```
The only explicit library dependency outside of the Scala language environment is Databinder dispatch version 0.13.4

## App design

## Testing
### Running the tests
Run the test suite to verify correct behaviour.  

From the command line:
```
% sbt test
```
### Test Coverage Report
To measure test coverage, this app uses the 'scoverage' SBT plugin.
To create the report, rom the command line:
```
% sbt coverage test coverageReport
```

## Author
* [Roy Phillips](mailto:phillips.roy@gmail.com)

## License
[![License](https://licensebuttons.net/l/by/3.0/88x31.png)](https://creativecommons.org/licenses/by/4.0/) 

(c) 2018 This project is licensed under Creative Commons License

[Attribution 4.0 International (CC BY 4.0)](file:LICENSE.md)
