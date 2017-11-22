
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

    // Client Method of downloading a file from server
    private void downloadFile(String serverFile, String clientFile) {
        try {
            File file = new File(clientFile);
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            int bytesDownloaded = 0;
            int bytesToDownload = this.serverObj.getFileSize(serverFile);

            if (bytesToDownload > 0) {

                byte[] bytesArr = new byte[1024];
                bytesDownloaded = this.serverObj.downloadFile(serverFile, bytesArr, bytesDownloaded);
                bos.write(bytesArr, 0, bytesArr.length);
                bos.flush();

                while (bytesDownloaded > 0 && bytesDownloaded < bytesToDownload) {
                    bytesArr = new byte[1024];
                    bytesDownloaded = this.serverObj.downloadFile(serverFile, bytesArr, bytesDownloaded);
                    bos.write(bytesArr);
                    bos.flush();

                    float perc = (float) bytesDownloaded / (float) bytesToDownload * (float) 100.0;
                    System.out.print("\r");
                    System.out.print("downloading ... " + ((int) perc) + "%");
                }

                if (bytesDownloaded > 0) {
                    System.out.println("\nFile downloaded");
                } else {
                    System.out.println("\nError occured while downloading the file");
                }
            } else {
                System.out.println("File not found..");
            }

        } catch (Exception e) {
            System.out.println("\nError occured while downloading the file");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void uploadFile(String serverFile, String clientFile) {
        try {

            File file = new File(clientFile);
            if (file.exists()) {

                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream buffStream = new BufferedInputStream(fis);
                int fileLength = (int)file.length();

                // Create the byte array with appropriate size
                byte[] byteArr = new byte[1024];

                // Send file data to client
                int bytesread = buffStream.read(byteArr, 0, byteArr.length);
                boolean success = this.serverObj.uploadFile(serverFile, byteArr, false);

                while(success && bytesread < fileLength) {
                    bytesread += buffStream.read(byteArr);
                    success = this.serverObj.uploadFile(serverFile, byteArr, true);

                    float perc = (float) bytesread / (float) fileLength * (float) 100.0;
                    System.out.print("\r");
                    System.out.print("uploading ... " + ((int) perc) + "%");
                }
                if (success) {
                    System.out.println("\nSuccessfully uploaded the file");
                } else {
                    System.out.println("\nError occured while uploading the file");
                }

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