
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/*
 * Class - Server Socket Class
 * @author Rohit Tirmanwar (G01030038)
 *
 */
public class server {

    /*
     * INSTANCE VARIABLES
     */

    private ServerSocket srvSocket;
    private OutputStream outStream;
    private InputStream inStream;

    private static int numThreadsRunning = 0;
    private static boolean isShutDownCalled = false;


    /*
     * CONSTRUCTOR
     */
    server(Socket clSocket, ServerSocket srvSkt) {
        try {
            this.srvSocket = srvSkt;
            this.outStream = clSocket.getOutputStream();
            this.inStream = clSocket.getInputStream();

        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /*
     * Receives the file uploaded by client
     */
    private void receiveClientFile(String filePath) {
        try {

            // Send the size of file to client
            ObjectInputStream bytesToRecieveStream = new ObjectInputStream(inStream);
            int numBytes = (int)bytesToRecieveStream.readObject();

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            filePath = s + filePath;

            int bytesRead = 0;
            byte[] bytesArray = new byte[10];
            FileOutputStream fos = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            // Read file data bytes from client
            bytesRead = inStream.read(bytesArray, 0, bytesArray.length);
            bos.write(bytesArray);

            while(bytesRead < numBytes) {
                bos.flush();
                bytesRead += inStream.read(bytesArray);
                bos.write(bytesArray);
            }

            bos.flush();
            inStream.close();
            outStream.close();

        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        finally {

        }
    }

    /*
     * Sends the file requested by client (download)
     */
    private void sendFileToClient(String filePath) {

//        int bytesSent =  0;

        try {

            // Get File path
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            filePath = s + filePath;

            // Create and check for file existance
            File file = new File(filePath);
            int numBytes = (int) file.length();
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            // Read file is success, send to client
            sendErrorToClient(0, null);

            // Create the byte array with appropriate size
            byte[] byteArr = new byte[10];

            // Send file length to client
            ObjectOutputStream filelengthStream = new ObjectOutputStream(outStream);
            filelengthStream.writeObject(numBytes);

            // Send file data to client
            int bytesSent = bis.read(byteArr, 0, byteArr.length);
            outStream.write(byteArr, 0, byteArr.length);

            // Continue to read the file
            while (bytesSent < numBytes) {
                outStream.flush();
                bytesSent += bis.read(byteArr);
                outStream.write(byteArr);
            }

            outStream.flush();

            bis.close();
            inStream.close();
        }
        catch (FileNotFoundException e) {
            // Error
            e.printStackTrace();
            sendErrorToClient(201, "File not found");
        }
        catch (IOException e) {
            e.printStackTrace();
            print(e.getMessage());
        }
        finally {

        }

    }

    /*
    * Creates a new directory from server location
    */
    private void createDir(String dirPath) {
        try {
            if(0 == dirPath.indexOf("/")) {
                dirPath = dirPath.substring(1);
            }

            File theDir = new File(dirPath);

            if (!theDir.exists()) {
                theDir.mkdir();

                // send success code to client
                sendErrorToClient(0, null);

                ObjectOutputStream outToClient1 = new ObjectOutputStream(outStream);
                outToClient1.writeObject("successfully created directory: " + dirPath);

                outToClient1.flush();
                outToClient1.close();
            } else {
                sendErrorToClient(210, "Directory already exists");
            }
        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

     /*
     * Deletes a directory from server location
     */

    private void removeDir(String dirPath) {
        try {

            if(0 == dirPath.indexOf("/")) {
                dirPath = dirPath.substring(1);
            }

            File theDir = new File(dirPath);

            if (theDir.exists()) {
                if (theDir.listFiles().length > 0) {
                    sendErrorToClient(203, "Directory is not empty");
                }
                else {
                    theDir.delete();

                    // Send success code to client
                    sendErrorToClient(0, null);

                    ObjectOutputStream outToClient1 = new ObjectOutputStream(outStream);
                    outToClient1.writeObject("successfully deleted directory: " + dirPath);

                    outToClient1.flush();
                    outToClient1.close();
                }
            } else {
                sendErrorToClient(202, "Directory not found");
            }
        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    /*
     * Deletes a file from server location
     */

    private void removeFile(String filePath) {
        try {

            if(0 == filePath.indexOf("/")) {
                filePath = filePath.substring(1);
            }

            File theFile = new File(filePath);

            if (theFile.exists()) {
                theFile.delete();

                // Send success code to client
                sendErrorToClient(0, null);


                ObjectOutputStream outToClient1 = new ObjectOutputStream(outStream);
                outToClient1.writeObject("successfully deleted directory: " + filePath);

                outToClient1.flush();
                outToClient1.close();
            } else {
                sendErrorToClient(201, "File not found");
            }
        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

    }

    /*
     * Sends the dir information of given path to client
     */

    void displayFilesOfDirectory(String dirPath) {

        try {

            if(0 == dirPath.indexOf("/")) {
                dirPath = dirPath.substring(1);
            }

            File theFile = new File(dirPath);
            String fileNames = "";
            if (theFile.exists()) {

                // Send success code to client
                sendErrorToClient(0, null);

                String[] files = new File(dirPath).list();
                Path currentRelativePath = Paths.get("");
                fileNames = fileNames + "Root Directory: " + currentRelativePath.toAbsolutePath().toString();

                for (String file : files) {
                    fileNames = fileNames + "\n" + file;
                }
            }
            else {
                // DIRECTORY DOESNOT EXISTS
                sendErrorToClient(202, "Directory not found");
                return;
            }

            ObjectOutputStream outToClient1 = new ObjectOutputStream(outStream);
            outToClient1.writeObject(fileNames);

            outToClient1.flush();
            outToClient1.close();


        } catch(IOException e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /*
     * shut down the server
     */
    private void shutdownServer() {
        try {
            this.srvSocket.close();
            this.inStream.close();
            this.outStream.close();

        } catch (IOException e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            sendErrorToClient(215, "Error shutting down the server..");
        }
    }

    /*
     * Processes the fist command of client and call the respective method
     */
    private void processClientRequest() {

        try {
            // Read the commands sent by client
            ObjectInputStream objInpStream = new ObjectInputStream(inStream);
            String command = (String) objInpStream.readObject();
            String commands[] = command.split("\\s*:\\s*");
            if (commands.length <= 0) {
                objInpStream.close();
                return;
            }

            // Validate and accept the right command
            switch (commands[0]) {
                case "dir": {
                    if (commands.length <= 1) {
                        this.displayFilesOfDirectory("./");
                    } else {
                        this.displayFilesOfDirectory(commands[1]);
                    }
                    break;
                }
                case "upload": {
                    this.receiveClientFile(commands[1]);
                    break;
                }
                case "download": {
                    this.sendFileToClient(commands[1]);
                    break;
                }
                case "shutdown": {
                    try {
                        // CLOSE THE SERVER
                        server.isShutDownCalled = true;
                        while(true) {
                            if (server.numThreadsRunning > 0) {
                                wait();
                            } else {
                                this.srvSocket.close();
                                server.isShutDownCalled = false;
                                break;
                            }
                        }
                    } catch (IOException e) {

                        // ADD EXCEPTION HANDLING
                        System.out.println(e.getMessage());
                    }
                    break;
                }
                case "mkdir": {
                    this.createDir(commands[1]);
                    break;
                }
                case "rmdir": {
                    this.removeDir(commands[1]);
                    break;
                }
                case "rm": {
                    this.removeFile(commands[1]);
                    break;
                }
                default: {
                    System.out.println("INVALID COMMAND FROM CLIENT! Please try again with valid command...");
                    break;
                }
            }

            objInpStream.close();
        }
        catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private void print(String msg) {
        System.out.println(msg);
    }

    /*
     * Sends error codes and message to client
     */
    private void sendErrorToClient(int errCode, String errMessage) {
        try {
            ObjectOutputStream errToClient = new ObjectOutputStream(outStream);
            errToClient.writeObject(errCode);
            if(errCode != 0) {
                ObjectOutputStream errMsgToClient = new ObjectOutputStream(outStream);
                errMsgToClient.writeObject(errMessage);
                errToClient.close();
                errMsgToClient.close();
            }
        }
        catch (Exception e){
            // EXCEPTION HANDLING
            e.printStackTrace();
        }
    }

    /*
     * Create a thread for client connection
     */
    public static void startConnectionThread(Socket clientSocket, ServerSocket srvSocket) {

        Runnable srvRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    server sktServer = new server(clientSocket, srvSocket);
                    sktServer.processClientRequest();
                    clientSocket.close();

                    server.numThreadsRunning--;
                    notifyAll();
                }
                catch (IOException e) {
                    // EXCEPTION HANDLING
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            }
        };

        Thread srvThread = new Thread(srvRunnable);
        server.numThreadsRunning++;
        srvThread.start();
    }

    /*
     * Start the server
     */
    public static void start(ServerSocket srvSocket) {

        // Loop to accept all incoming client requests
        while (!srvSocket.isClosed() && !server.isShutDownCalled) {
            try {
                // Accept incoming request
                Socket clientSocket = srvSocket.accept();
                startConnectionThread(clientSocket, srvSocket);

            } catch (Exception e) {
                // EXCEPTION HANDLING
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
    }


    /*
     * Server main Function
     */
    public static void main(String[] args) {

        try {
            if (args.length < 2) {
                System.out.println("Wrong command. Use the command \"start <port>\" to start the server.");
                return;
            }

            if (0 == args[0].compareTo("start")) {
                int port = Integer.parseInt(args[1]);
                ServerSocket socket = new ServerSocket(port);
                System.out.println("Server started on port#: " + port);
                start(socket);
            } else {
                System.out.println("Wrong command. Use the command \"start <port>\" to start the server.");
            }
        }
        catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
        }
        finally {

        }

    }




}
