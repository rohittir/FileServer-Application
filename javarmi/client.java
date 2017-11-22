
import java.io.*;
import java.rmi.*;

/**
 * This class implements java client
 * @author Rohit Tirmanwar (G01030038)
 *
 */

public class client {

    // INSTANCE VARIABLES
    private serverInterface serverObj = null;

    /*
     * Constructor
     */
    client(serverInterface obj) {
        this.serverObj = obj;
    }

    /**
     * Client Method of downloading a file from server
     */
    private void downloadFile(String serverFile, String clientFile) {
        try {
            // create file object
            File file = new File(clientFile);
            int bytesDownloaded = 0;

            // get the server file data size
            int bytesToDownload = this.serverObj.getServerFileLength(serverFile);

            if (bytesToDownload > 0) {

                // check if the file already downloaded on client
                if (file.exists()) {
                    bytesDownloaded = (int)file.length();
                }

                FileOutputStream fos = null;

                // check if there was any previous inturrupted dowload. If so, prepare for resume download
                if (bytesDownloaded > 0 && bytesDownloaded < bytesToDownload) {
                    fos = new FileOutputStream(file, true);
                } else {
                    bytesDownloaded = 0;
                    fos = new FileOutputStream(file, false);
                }

                BufferedOutputStream bos = new BufferedOutputStream(fos);

                // Start donloading file...
                byte[] bytesArr = this.serverObj.downloadFile(serverFile, bytesDownloaded);

                while (bytesArr != null && bytesArr.length > 0) {

                    // write to file on client
                    bos.write(bytesArr, 0, bytesArr.length);
                    bos.flush();  // flush all the data written to file

                    bytesDownloaded += bytesArr.length;

                    // continue downloading file...
                    bytesArr = this.serverObj.downloadFile(serverFile, bytesDownloaded);

                    // show percentage of download
                    float progress = (float) bytesDownloaded / (float)bytesToDownload * (float) 100.0;
                    System.out.print("\r");
                    System.out.print("downloading ... " + ((int) progress) + "%");
                }

                // show the success message
                if (bytesDownloaded > 0) {
                    System.out.println("\nFile downloaded");
                } else {
                    System.out.println("\nError occured while downloading the file");
                }

                // close the file
                fos.close();

            } else {
                System.out.println("File not found..");
            }

        } catch (Exception e) {
            System.out.println("\nError occured while downloading the file");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Client method to upload a file to server
     */
    private void uploadFile(String serverFile, String clientFile) {
        try {

            // check the file on server if available and get its data size
            int serverFileSize = this.serverObj.getServerFileLength(serverFile);

            File file = new File(clientFile);
            if (file.exists()) {

                FileInputStream fis = new FileInputStream(file);
                int fileLength = (int)file.length();
                int bytesread = 0;

                boolean isAppend = false;
                if (serverFileSize > 0 && serverFileSize < fileLength) {
                    fis.skip(serverFileSize);
                    isAppend = true;
                    bytesread = serverFileSize;
                }

                // Create the byte array with appropriate size
                byte[] byteArr = new byte[1024];
                boolean success = true;

                while(success && bytesread < fileLength) {

                    // Read the data from file
                    bytesread += fis.read(byteArr);

                    // Send file data to client
                    success = this.serverObj.uploadFile(serverFile, byteArr, isAppend);
                    isAppend = true;

                    float progress = (float) bytesread / (float) fileLength * (float) 100.0;
                    System.out.print("\r");
                    System.out.print("uploading ... " + ((int) progress) + "%");
                }
                if (success) {
                    System.out.println("\nSuccessfully uploaded the file");
                } else {
                    System.out.println("\nError occured while uploading the file");
                }

                // close the file
                fis.close();

            } else {
                System.out.println("File not found..");
            }

        } catch (Exception e) {
            System.out.println("Error occured while uploading the file");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send the requested client command to server
     **/
    public void sendCommandToServer(String[] commands) {
        try {
            switch(commands[0]) {
                case "dir":
                case "mkdir":
                case "rmdir":
                case "rm":
                case "shutdown": {
                    if (commands.length > 1) {
                        String reply = this.serverObj.processCommand(commands[0] + ":" + commands[1]);
                        System.out.println(reply);
                    } else if (0 == commands[0].compareTo("dir") || 0 == commands[0].compareTo("shutdown")) {
                        if (commands.length == 1) {
                            String reply = this.serverObj.processCommand(commands[0]+":");
                            System.out.println(reply);
                        }
                    }
                    break;
                }
                case "upload": {
                    if (commands.length > 2) {
                        this.uploadFile(commands[2], commands[1]);
                    } else System.out.println("Invalid command..");
                    break;
                }
                case "download": {
                    if (commands.length > 2) {
                        this.downloadFile(commands[1], commands[2]);
                    } else System.out.println("Invalid command..");
                    break;
                }
                default: {
                    System.out.println("Invalid command..");
                    break;
                }
            }
        } catch(RemoteException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * Client Main Function (Entry point)
    */
    public static void main(String[] args) {

        try {

            String server = System.getenv("PA2_SERVER");
            String[] parts = server.split(":");
            if (parts.length > 0 && parts[0] != null && !parts[0].isEmpty()) {

                String name = "rmi://" + parts[0] + "/FileServer";
                serverInterface s1 = (serverInterface)Naming.lookup(name);

                if (s1 != null) {
                    // System.out.println("Server object is found in the registry");
                    client c1 = new client(s1);
                    c1.sendCommandToServer(args);
                }
            } else {
                System.out.println("Please set the environment variable PA2_SERVER before running the client. e.g. export PA2_SERVER=\"localhost\"");
            }

        } catch (Exception e) {
            // EXCEPTION HANDLING
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

}