

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;


/*
 * Class - Server Class
 * @author Rohit Tirmanwar (G01030038)
 *
 */
public class server extends UnicastRemoteObject implements serverInterface  {


    /*
     * Constructor
     */
    server() throws RemoteException {

    }

    /*
     *
     */
    public String processCommand(String command) throws RemoteException {

        String retMessage = "INVALID COMMAND FROM CLIENT! Please try again with valid command...";
        try {

            String[] commands = command.split(":");
            if(commands.length <= 0) {
                return retMessage;
            }

            // Validate and accept the right command
            switch (commands[0]) {
                case "dir": {
                    if (commands.length <= 1) {
                        retMessage = this.displayFilesOfDirectory("./");
                    } else {
                        retMessage = this.displayFilesOfDirectory(commands[1]);
                    }
                    break;
                }
                case "shutdown": {

                    break;
                }
                case "mkdir": {
                    if (commands.length > 1) {
                        retMessage = this.createDir(commands[1]);
                    }
                    break;
                }
                case "rmdir": {
                    if (commands.length > 1) {
                        retMessage = this.removeDir(commands[1]);
                    }
                    break;
                }
                case "rm": {
                    if (commands.length > 1) {
                        retMessage = this.removeFile(commands[1]);
                    }
                    break;
                }
                default: {
                    System.out.println("INVALID COMMAND FROM CLIENT! Please try again with valid command...");
                    break;
                }
            }

        }
        catch(Exception e) {
            // handle Exception
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return retMessage;

    }

    public int getFileSize(String filePath) throws RemoteException {
        int fileSize = -1;
        try {

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            filePath = s + "/" + filePath;

            File file = new File(filePath);
            if (file.exists()) {
                fileSize = (int)file.length();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return fileSize;
    }

    public int downloadFile(String filePath, byte[] bytesArr, int bytesDownloaded) throws RemoteException {

        int retVal = -1;
        try {

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            filePath = s + "/" + filePath;

            File file = new File(filePath);
            if (file.exists()) {

                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);

                int val = bis.read(bytesArr);
                if (val > 0) {
                    bytesDownloaded += val;
                    retVal = bytesDownloaded;
                } else {
                    System.out.println("Error in downloading file.");
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return retVal;

    }

    public boolean uploadFile(String filePath, byte[] bytes, boolean isAppend) throws RemoteException {

        try {

            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            filePath = s + "/" + filePath;

            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file, isAppend);

            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(bytes, 0, bytes.length);
            bos.flush();

            return true;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /*
     * Sends the dir information of given path to client
     */

    private String displayFilesOfDirectory(String dirPath) {

        String retValue = "";
        try {
            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
                dirPath = dirPath.substring(1);
            }

            File theFile = new File(dirPath);
            String fileNames = "";
            if (theFile.exists()) {
                String[] files = new File(dirPath).list();
                fileNames = "Root Directory: /";
                for (String file : files) {
                    fileNames = fileNames + "\n" + file;
                }
                retValue = fileNames;
            }
            else {
                // DIRECTORY DOESNOT EXISTS
                retValue = "Error-202: Directory not found";
                return retValue;
            }
        } catch(Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retValue;
    }

    /*
     * Deletes a file from server location
     */
    private String removeFile(String filePath) {
        String retValue = "";

        try {

            if(0 == filePath.indexOf("/") || 0 == filePath.indexOf("\\")) {
                filePath = filePath.substring(1);
            }

            File theFile = new File(filePath);

            if (theFile.exists()) {
                if (theFile.listFiles() != null) {
                    if (theFile.listFiles().length > 0) {
                        retValue = "Error-201: File not found";
                        return retValue;
                    }
                }
                theFile.delete();
                retValue = "Successfully deleted the file.";
                return retValue;

            } else {
                retValue = "Error-201: File not found";
                return retValue;
            }
        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retValue;

    }

    /*
     * Deletes a directory from server location
     */

    private String removeDir(String dirPath) {

        String retValue = "";
        try {

            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
                dirPath = dirPath.substring(1);
            }

            File theDir = new File(dirPath);

            if (theDir.exists()) {
                if (theDir.listFiles().length > 0) {
                    retValue = "Error-203: Directory is not empty";
                }
                else {
                    theDir.delete();
                    retValue = "Successfully deleted the directory.";
                }
            } else {
                retValue = "Error-202: Directory not found";
            }
        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retValue;

    }

    /*
    * Creates a new directory from server location
    */
    private String createDir(String dirPath) {

        String retVal = "";
        try {
            if(0 == dirPath.indexOf("/") || 0 == dirPath.indexOf("\\")) {
                dirPath = dirPath.substring(1);
            }

            File theDir = new File(dirPath);

            if (!theDir.exists()) {
                theDir.mkdir();

                // send success code to client
                retVal = "Successfully created the directory.";

            } else {
                retVal = "Error-210: Directory already exists";
            }

        } catch (Exception e) {
            // EXCEPTION HANDLING
            e.printStackTrace();
            System.out.println(e.getMessage());
        }

        return retVal;
    }

    /*
     * Server main Function
     */
    public static void main(String[] args) {

        try {
            server s1 = new server();
            Naming.rebind("rmi://localhost/FileServer", s1);
            System.out.println("FileServer Server is ready.");

        }
        catch (Exception e) {
            System.out.println("FileServer Server has a problem.");
            // EXCEPTION HANDLING
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


}