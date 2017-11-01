# Socket_FileServer

<b>This is the program for file server developed using Java Sockets.</b>

To start the server, run the following command - 
java -cp pa1.jar server start <portnumber>

<b>To run the client</b>, first youe need to set the enviornment variable named PA1_SERVER=<hostname:port>

<b>Following are the different commands supported by the client.</b>
java -cp pa1.jar client upload <path_on_client> </path/filename/on/server>
java -cp pa1.jar client download </path/existing_filename/on/server> <path_on_client>
java -cp pa1.jar client dir </path/existing_directory/on/server>
java -cp pa1.jar client mkdir </path/new_directory/on/server>
java -cp pa1.jar client rmdir </path/existing_directory/on/server>
java -cp pa1.jar client rm </path/existing_filename/on/server>
java -cp pa1.jar client shutdown


Multiple clients can talk to the server at the same time. 
