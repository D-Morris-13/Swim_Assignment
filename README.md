# Solution to Swim Interview Exercise (https://github.com/swimos/dev-exercise)

by David Morris.

## Running The Server
The server can be run from SwimServer.jar. It takes no command line arguments and listens on port 8080.

## Running The Client

The client can be run from SwimClient.jar. With no arguments, it will connect to the loopback address on port 8080.

There are two optional arguments:
- -h : Specify the host name
- -p : Specify the port number

Commands input into the client are sensitive to whitespace; `{"command" :"get"}` is not considered valid. An error response will be returned from the server when a bad command is input.

If the server exits, the client will exit on attempting to send the next message.

## Dependencies

This project depends on JUnit for testing.