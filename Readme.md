Auguste Lapomme

Instructions to run
Run command: docker build . -f BootstrapDockerfile -t prj5-bootstrap && docker build . -f PeerDockerfile -t prj5-peer && docker build . -f ClientDockerfile -t prj5-client  
Run command: docker compose -f <docker compose file> up
This is all that is needed.

To run testcases, use the 5 given docker composes.  Client behavior is determined based on which testcase it is.

Testcase 1: After the bootstrap server started, peers join the ring one by one, the list of peers joining the ring is with IDs in increasing order. The first peer is the peer with ID 1.

Testcase 2: Same as above, but the list of peers that will join the list is with IDs in random order.

Testcase 3: Client wants to store an object with a given ID. If the object is successfully stored, the client should print "STORED: <objectID>". It is hardcoded that the client will request to store 58 with client Id 1. This means that node 66 should store 58::1 in its object txt file.

Testcase 4: Client wants to retrieve an object that was previously stored with a given ID. If the object is successfully retrieved, the client should print "RETRIEVED: <objectID>". It is hardcoded that the client will request to retrieve 2 with client id 1. This means that node 5 should return 2 as it is already present in its objects file with client id 1.

Testcase 5: Client wants to retrieve an object with a given ID, but the object does not exist. As in TESTCASE 4 an object should only be retrieved if its record matches both the clientID and the objectID. If the object does not exist the bootstrap server should return -1 to the client, with the meaning that object was not found—this is detected if the request went around the ring without the object being found. The client should print "NOT FOUND: <objectID>". It is hardcoded that the client will request to retrieve 72 with client id 1. This means that the request should go around the ring and realize that it is not present anywhere.


Code is created with Java

Resources used:
For using TCP with java: 
- https://www.geeksforgeeks.org/how-to-create-a-simple-tcp-client-server-connection-in-java/ 
- https://www.geeksforgeeks.org/socket-programming-in-java/ 
- https://medium.com/@gaurangjotwani/creating-a-tcp-connection-between-two-servers-in-java-27fabe53deaa
