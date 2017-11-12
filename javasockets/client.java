

 import sun.jvm.hotspot.memory.SymbolTable;

 import java.io.*;
 import java.net.Socket;

 /**
  * This class implements java socket client
  * @author Rohit Tirmanwar (G01030038)
  *
  */

 public class client {


     // INSTANCE VARIABLES

     private Socket clientSocket = null;
     private OutputStream outStream = null;
     private InputStream inStream = null;


     /*
      * Constructor
      */
     client(String host, int port) {
         try {

             // Store the input and output streams of the socket
             this.clientSocket = new Socket(host, port);
             outStream = this.clientSocket.getOutputStream();
             inStream = this.clientSocket.getInputStream();

         } catch (Exception e) {
             // EXCEPTION HANDLING
             System.out.println("Connection error. Please check if the server is running.");
 //            e.printStackTrace();
         }

     }

     /*
      * To check any error code recieved from server
      */
     private boolean checkServerError () {
         try {
             // Read the response
             ObjectInputStream srvResponse = new ObjectInputStream(inStream);
             Object err = srvResponse.readObject();
             int errCode = 1;
             if (err != null) {
                 errCode = (int) err;
             }
             // Validate the error code
             if (errCode > 0) {
                 ObjectInputStream srvResponse1 = new ObjectInputStream(inStream);
                 String errMsg = (String)srvResponse1.readObject();
                 System.out.println("Error " + errCode + ": " + errMsg);

                 srvResponse.close();
                 srvResponse1.close();

                 return true;
             }
         }
         catch (Exception e) {
             // EXCEPTION HANDLING
             e.printStackTrace();
             System.out.println(e.getMessage());
         }

         return false;
     }


     /*
      * Download a file from server
      */
     private void downloadFile(String serverFile, String clientFile) {

         try {

             // Send download command to server
             ObjectOutputStream dataToServer = new ObjectOutputStream(outStream);
             dataToServer.writeObject("download :" + serverFile);

             // check error
             if (checkServerError())
                 return;

             // Get the file size info from server
             ObjectInputStream filelengthStream = new ObjectInputStream(inStream);
             int bytesToDownload = (int)filelengthStream.readObject();

             int bytesRead = 0;

             // Read the bytes from Server and write to the file
             readServerBytesAndWriteFile(clientFile, bytesRead, bytesToDownload);

             dataToServer.close();
             filelengthStream.close();

         }
         catch(Exception e) {
             // EXCEPTION HANDLING
             e.printStackTrace();
             System.out.println(e.getMessage());
         }

     }

     /*
      * Read the server outstream bytes and write to the file
      */

     private void readServerBytesAndWriteFile(String clientFile, int bytesRead, int bytesToDownload) {
         try {
             // Create a byte array of approrpriate size
             byte[] byteArr = new byte[1024];

             // Create file output stream to write the data into file
             FileOutputStream fos = null;

             fos = new FileOutputStream(clientFile);

             BufferedOutputStream bos = new BufferedOutputStream(fos);

             bytesRead += inStream.read(byteArr, 0, byteArr.length);
             bos.write(byteArr);
             bos.flush();

             while (bytesRead < bytesToDownload) {
                 bytesRead += inStream.read(byteArr);
                 bos.write(byteArr);
                 bos.flush();

                 float perc = (float) bytesRead / (float) bytesToDownload * (float) 100.0;
                 System.out.print("\r");
                 System.out.print("Downloading ... " + ((int) perc) + "%");

             }
             System.out.println("\nFile Downloaded");

         } catch (IOException e) {
             //EXCEPTION
             System.out.println(e.getMessage());
             e.printStackTrace();
 //            if (bytesRead < bytesToDownload) {
 //                System.out.println("Resuming download ");
 //                readServerBytesAndWriteFile(clientFile, bytesRead, bytesToDownload);
 //            }

         }
     }

     /*
      * Upload a file to server
      */
     private void uploadFile(String clientFile, String serverFile) {

         try {
             // Send upload file name to server
             ObjectOutputStream dataToServer = new ObjectOutputStream(outStream);
             dataToServer.writeObject("upload: " + serverFile);

             // check error
             if (checkServerError())
                 return;

             // Read the client file
             File file = new File(clientFile);

             ObjectOutputStream dataToServer1 = new ObjectOutputStream(outStream);
             int bytesToUpload = (int)file.length();
             dataToServer1.writeObject(bytesToUpload);

             System.out.println("File uploading to server");

             // Create the byte array with appropriate size
             byte[] byteArr = new byte[1024];
             FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis);

             // Read the file into byte array
             int bytesSent = bis.read(byteArr, 0, byteArr.length);
             outStream.write(byteArr, 0, byteArr.length);
             outStream.flush();
             System.out.print("Uploading ... 0%");

             // Continue to read the file
             while (bytesSent < (int)file.length()) {
                 bytesSent += bis.read(byteArr);
                 outStream.write(byteArr);
                 outStream.flush();

                 float perc = (float)bytesSent / (float) bytesToUpload * (float)100.0;
                 System.out.print("\r");
                 System.out.print("Uploading ... " + ((int)perc) + "%");
             }

             System.out.println("\nFile Uploaded");

             bis.close();
             outStream.close();

         }
         catch (FileNotFoundException e) {
             // EXCEPTION HANDLING
             System.out.println(e.getMessage());
             e.printStackTrace();
         } catch (IOException e) {
             // EXCEPTION HANDLING
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
         finally {

         }

     }



     /*
      * Sends a string request to server and recieved a string reply from server
      */
     private void sendCommandToServer(String command) {

         try {
             ObjectOutputStream dataToServer = new ObjectOutputStream(outStream);
             dataToServer.writeObject(command);
             dataToServer.flush();

             // check for server error
             if(checkServerError())
                 return;

             ObjectInputStream inFromServer = new ObjectInputStream(inStream);
             System.out.println((String)inFromServer.readObject());

             dataToServer.close();
             inFromServer.close();

         } catch (IOException e) {
             // EXCEPTION HANDLING
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
         catch (ClassNotFoundException e) {
             // EXCEPTION HANDLING
             System.out.println(e.getMessage());
             e.printStackTrace();
         }

     }

     private void shutDownServer(String command) {
         try {
             ObjectOutputStream dataToServer = new ObjectOutputStream(outStream);
             dataToServer.writeObject(command);
             dataToServer.flush();

             // check for server message
             ObjectInputStream dataFromServer = new ObjectInputStream(inStream);
             String msg = (String) dataFromServer.readObject();
             System.out.println(msg);

             dataFromServer.close();
             this.inStream.close();
             this.outStream.close();

         } catch (Exception e) {
             // EXCEPTION HANDLING
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
     }

     public void processCommand(String []commands) {

         if (commands.length > 0) {
             switch (commands[0]) {
                 case "dir": {
                     if (commands.length == 2) {
                         this.sendCommandToServer("dir: " + commands[1]);
                     } else {
                         this.sendCommandToServer("dir: ./");
                     }
                     break;
                 }
                 case "upload": {
                     if (commands.length == 3) {
                         this.uploadFile(commands[1], commands[2]);
                     } else {
                         System.out.println("INVALID COMMAND! Exiting...");
                         return;
                     }
                     break;
                 }
                 case "download": {
                     if (commands.length == 3) {
                         this.downloadFile(commands[1], commands[2]);
                     } else {
                         System.out.println("INVALID COMMAND! Exiting...");
                         return;
                     }
                     break;
                 }
                 case "shutdown": {
                     this.shutDownServer(commands[0]);
                     break;
                 }
                 case "mkdir": {
                     if (commands.length == 2) {
                         this.sendCommandToServer(commands[0] + ":" + commands[1]);
                     } else {
                         System.out.println("INVALID COMMAND! Exiting...");
                         return;
                     }
                     break;
                 }
                 case "rmdir": {
                     if (commands.length == 2) {
                         this.sendCommandToServer(commands[0] + ":" + commands[1]);
                     } else {
                         System.out.println("INVALID COMMAND! Exiting...");
                         return;
                     }
                     break;
                 }
                 case "rm": {
                     if (commands.length == 2) {
                         this.sendCommandToServer(commands[0] + ":" + commands[1]);
                     } else {
                         System.out.println("INVALID COMMAND! Exiting...");
                         return;
                     }
                     break;
                 }
                 default: {
                     System.out.println("INVALID COMMAND! Exiting...");
                     try {
                         this.inStream.close();
                         this.outStream.close();
                     } catch (IOException e) {
                         // EXCEPTION HANDLING
                         System.out.println(e.getMessage());
                         e.printStackTrace();
                     }
                     break;
                 }
             }
         } else {
             try {
                 this.inStream.close();
                 this.outStream.close();
             } catch (IOException e) {
                 // EXCEPTION HANDLING
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
         }

     }


     /*
      * Client Main Function (Entry point)
     */
     public static void main(String[] args) {

         try {
             String hostWithPort = System.getenv("PA1_SERVER");

             String hostnPort[] = hostWithPort.split("\\s*:\\s*");
             if(hostnPort.length < 2) {
                 System.out.println("Env variable <PA1_SERVER> is not valid");
                 return;
             }
             client clientObj = new client(hostnPort[0], Integer.parseInt(hostnPort[1]));
             if (clientObj.outStream != null && clientObj.inStream != null) {
                 clientObj.processCommand(args);
             }

         } catch (Exception e) {
             // EXCEPTION HANDLING
             System.out.println(e.getMessage());
             e.printStackTrace();
         }
     }

 }