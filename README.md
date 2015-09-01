# CAN
Partial implementation of a Content Addressable Network

This implementation simply deals with adding new peers and giving “addresses/locations” to these newly added peers relative to the server. On adding a new file, a location for the file (which peer gets the file) is determined by the server, and a path is created to that peer in the network and the file is then sent.

1. Compile the Bootstrap.java and Peer.java files
2. Run the Bootstrap.java file. It will ask for a server port. Note the IP address of this machine.
3. Run the Peer.java file on another machine. It will ask for the IP address of the host. Then, it will ask for the host port number (from step 2). Then, it will ask for peer port number (for this machine).
4. Commands join, insert <filename> and search <filename> can be given in the console. 
5. Type the word exit to exit.
