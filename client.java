

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

    private Socket clientSocket;
    private OutputStream outStream;
    private InputStream inStream;


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
            System.out.println(e.getMessage());
            e.printStackTrace();
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
            int errCode = -1;
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

            // Create a byte array of approrpriate size
            byte[] byteArr = new byte[10];
            int bytesRead = 0;

            // Create file output stream to write the data into file
            FileOutputStream fos = new FileOutputStream(clientFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            bytesRead = inStream.read(byteArr, 0, byteArr.length);
            bos.write(byteArr);

            while (bytesRead < bytesToDownload) {
                bos.flush();
                bytesRead += inStream.read(byteArr);
                bos.write(byteArr);
                float perc = (float) bytesRead / (float) bytesToDownload * (float) 100.0;
                System.out.print("\r");
                System.out.print("Downloading ... " + ((int) perc) + "%");
            }
            System.out.println("\nFile Downloaded");

            bos.flush();

            dataToServer.close();
            filelengthStream.close();
            outStream.close();
            inStream.close();
            // outStream.close();

        }
        catch(Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
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

            // Read the client file
            File file = new File(clientFile);

            ObjectOutputStream dataToServer1 = new ObjectOutputStream(outStream);
            int bytesToUpload = (int)file.length();
            dataToServer1.writeObject(bytesToUpload);

            System.out.println("File uploading to server");

            // Create the byte array with appropriate size
//            int byteArrSize = 10;
//            if (bytesToUpload/100000 > 10) {
//                byteArrSize = bytesToUpload/100000;
//            }
            byte[] byteArr = new byte[10];
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Read the file into byte array
            int bytesSent = bis.read(byteArr, 0, byteArr.length);
            outStream.write(byteArr, 0, byteArr.length);
            System.out.print("Uploading ... 0%");

            // Continue to read the file
            while (bytesSent < (int)file.length()) {
                outStream.flush();
                bytesSent += bis.read(byteArr);
                outStream.write(byteArr);

                float perc = (float)bytesSent / (float) bytesToUpload * (float)100.0;
                System.out.print("\r");
                System.out.print("Uploading ... " + ((int)perc) + "%");
            }

            System.out.println("\nFile Uploaded");

            outStream.flush();

            bis.close();
            outStream.close();

        }
        catch (FileNotFoundException e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
        } catch (IOException e) {
            // EXCEPTION HANDLING
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
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
        }

    }

    private void shutDownServer(String command) {
        try {
            ObjectOutputStream dataToServer = new ObjectOutputStream(outStream);
            dataToServer.writeObject(command);
            dataToServer.flush();

            // check for server error
            if(checkServerError())
                return;

            System.out.println("Server has been closed");

            this.inStream.close();
            this.outStream.close();

        } catch (IOException e) {
            // EXCEPTION HANDLING
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
            client client = new client(hostnPort[0], Integer.parseInt(hostnPort[1]));

            client.processCommand(args);

        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
        }
    }

}
