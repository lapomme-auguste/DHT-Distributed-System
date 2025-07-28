Auguste Lapomme

Instructions to run
Run command: docker build . -f BootstrapDockerfile -t prj5-bootstrap && docker build . -f PeerDockerfile -t prj5-peer && docker build . -f ClientDockerfile -t prj5-client  
Run command: docker compose -f <docker compose file> up
This is all that is needed.

To run testcases, use the 5 given docker composes.  Client behavior is determined based on which testcase it is.

Testcase 1:

Testcase 2:

Testcase 3:

Testcase 4:

Testcase 5:


Code is created with Java

Resources used:
For using TCP with java: 
- https://www.geeksforgeeks.org/how-to-create-a-simple-tcp-client-server-connection-in-java/ 
- https://www.geeksforgeeks.org/socket-programming-in-java/ 
- https://medium.com/@gaurangjotwani/creating-a-tcp-connection-between-two-servers-in-java-27fabe53deaa