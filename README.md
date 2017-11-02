# Java Socket FileServer

<b>This is the program for file server developed using Java Sockets.</b>

<p>To start the server, run the following command - </p>
<p>java -cp pa1.jar server start <portnumber></p>

<b>To run the client</b>, first you need to set the enviornment variable named PA1_SERVER=<hostname:port>

<br>

<b>Following are the different commands supported by the client.</b>
<p>java -cp pa1.jar client upload path_on_client /path/filename/on/server</p>
<p>java -cp pa1.jar client download /path/existing_filename/on/server> <path_on_client></p>
<p>java -cp pa1.jar client dir /path/existing_directory/on/server </p>
<p>java -cp pa1.jar client mkdir /path/new_directory/on/server</p>
<p>java -cp pa1.jar client rmdir /path/existing_directory/on/server</p>
<p>java -cp pa1.jar client rm /path/existing_filename/on/server</p>
<p>java -cp pa1.jar client shutdown</p>

<br>

<p>Multiple clients can talk to the server at the same time. </p>

<p> The server's root directory is the working directory where the server has been sarted and all the files are stored on that root folder.</p>
<p> It is recommended to use the server file path as /folder/file.ext and not the absolute path (when running on the same system and you know the absolute server path), when using the commands for upload/download on the client </p>