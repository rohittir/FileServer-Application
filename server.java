
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
     * increament/decreament the thread count
     */
    public static synchronized void increamentRunningThreadsCount(int num) {
        server.numThreadsRunning = numThreadsRunning + num;
    }

    /*
     * get current thread counts
     */
    public static synchronized int getRunningThreadsCount() {
        return server.numThreadsRunning;
    }

    /*
     * get current thread counts
     */
    public static synchronized boolean getIsShutDownCalled() {
        return server.isShutDownCalled;
    }


    /*
     * get current thread counts
     */
    public static synchronized void setIsShutDownCalled(boolean isShutDown) {
        server.isShutDownCalled = isShutDown;
    }



    /*
     * Receives the file uploaded by client
     */
    private void receiveClientFile(String filePath) {
        try {

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            filePath = s + filePath;

            // Create file output stream to write the data into file
            FileOutputStream fos = new FileOutputStream(filePath);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            sendErrorToClient(0, null);

            // Send the size of file to client
            ObjectInputStream bytesToRecieveStream = new ObjectInputStream(inStream);
            int numBytes = (int)bytesToRecieveStream.readObject();

            // Read the bytes from client and write into the file
            readClientBytesAndWriteFile(bos, 0, numBytes);

            bytesToRecieveStream.close();
            inStream.close();
            outStream.close();

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            sendErrorToClient(201, "File not found");
        }catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        finally {

        }
    }

    /*
     * Read the client outstream bytes and write to the file
     */

    private void readClientBytesAndWriteFile(BufferedOutputStream bos, int bytesRead, int bytesToRecieve) {
        try {
            // Create a byte array of approrpriate size
            byte[] byteArr = new byte[1024];

            bytesRead = inStream.read(byteArr, bytesRead, byteArr.length);
            bos.write(byteArr);
            bos.flush();

            while (bytesRead < bytesToRecieve) {
                bytesRead += inStream.read(byteArr);
                bos.write(byteArr);
                bos.flush();
            }

        } catch (IOException e) {
            //EXCEPTION
            System.out.println(e.getMessage());
            e.printStackTrace();
//            if (bytesRead < bytesToRecieve) {
//                System.out.println("Resuming upload ");
//                readClientBytesAndWriteFile(bos, bytesRead, bytesToRecieve);
//            }

        }
    }

    private void readFileAndSendBytesToClient(BufferedInputStream buffStream, int startByteCount, int totalBytesCount) {
        int bytesSent = 0;
        try {
            // Create the byte array with appropriate size
            byte[] byteArr = new byte[1024];

            // Send file data to client
            bytesSent = buffStream.read(byteArr, startByteCount, byteArr.length);
            outStream.write(byteArr, 0, byteArr.length);
            outStream.flush();

            // Continue to read the file
            while (bytesSent+startByteCount < totalBytesCount) {
                outStream.flush();
                bytesSent += buffStream.read(byteArr);
                outStream.write(byteArr);
            }

        } catch (IOException e) {

            // Exception handelling
            e.printStackTrace();
            System.out.println(e.getMessage());

            if (bytesSent+startByteCount < totalBytesCount) {
                System.out.println("Resuming download...");
                readFileAndSendBytesToClient(buffStream, bytesSent, totalBytesCount);
            }

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

            // Send file length to client
            ObjectOutputStream filelengthStream = new ObjectOutputStream(outStream);
            filelengthStream.writeObject(numBytes);

            readFileAndSendBytesToClient(bis, 0, numBytes);

            bis.close();
            inStream.close();
        }
        catch (FileNotFoundException e) {
            // Error
            System.out.println(e.getMessage());
            e.printStackTrace();
            sendErrorToClient(201, "File not found");
        }
        catch (Exception e) {
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
            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
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

            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
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

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            File theFile = new File(filePath);

            if (theFile.exists()) {
                theFile.delete();

                // Send success code to client
                sendErrorToClient(0, null);


                ObjectOutputStream outToClient1 = new ObjectOutputStream(outStream);
                outToClient1.writeObject("successfully deleted directory: /" + filePath);

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

            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
                dirPath = dirPath.substring(1);
            }

            File theFile = new File(dirPath);
            String fileNames = "";
            if (theFile.exists()) {

                // Send success code to client
                sendErrorToClient(0, null);

                String[] files = new File(dirPath).list();
//                Path currentRelativePath = Paths.get("");
//                fileNames = fileNames + "Root Directory: " + currentRelativePath.toAbsolutePath().toString();
                fileNames = "Root Directory: /";
//                if (dirPath.compareTo("./") == 0) {
//                    fileNames = "Current Directory: /";
//                }

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
            inStream.close();


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
            // CLOSE THE SERVER
            server.setIsShutDownCalled(true);

            ObjectOutputStream msgToClient = new ObjectOutputStream(outStream);

            if (server.getRunningThreadsCount() > 1) {
                msgToClient.writeObject("Server is serving other clients. Server will not accept any new commands now and will shut down once all services are completed. ");
                while(true) {
                    if (server.getRunningThreadsCount() <= 1) {
                        this.srvSocket.close();
                        server.setIsShutDownCalled(false);
                        break;
                    }
                }
            } else {
                msgToClient.writeObject("Server has been closed");
                msgToClient.close();
                this.srvSocket.close();
            }

        } catch (IOException e) {
            // EXCEPTION HANDLING
            System.out.println(e.getMessage());
            e.printStackTrace();
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
                    this.shutdownServer();
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
            if (inStream != null) inStream.close();
            if (outStream != null) outStream.close();
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
            System.out.println(e.getMessage());
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
                    increamentRunningThreadsCount(1);

                    server sktServer = new server(clientSocket, srvSocket);
                    sktServer.processClientRequest();
                    clientSocket.close();
                }
                catch (IOException e) {
                    // EXCEPTION HANDLING
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                } finally {
                    increamentRunningThreadsCount(-1);
                }
            }
        };

        Thread srvThread = new Thread(srvRunnable);
        srvThread.start();
    }


    /*
     * Start the server
     */
    public static void start(ServerSocket srvSocket) {

        // Loop to accept all incoming client requests
        while (!srvSocket.isClosed() && !server.getIsShutDownCalled()) {
            try {
                // Accept incoming request
                Socket clientSocket = srvSocket.accept();
                startConnectionThread(clientSocket, srvSocket);

            } catch (IOException e) {
                // EXCEPTION HANDLING
                if (srvSocket.isClosed()) {
                    System.out.println("Server has been closed");
                } else {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
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
            System.out.println(e.getMessage());
        }
        finally {

        }

    }




}
