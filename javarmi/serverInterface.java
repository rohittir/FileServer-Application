import java.io.BufferedInputStream;
import java.rmi.*;

public interface serverInterface extends java.rmi.Remote {

    public String processCommand(String command) throws RemoteException;

    public int getFileSize(String filePath) throws RemoteException;

    public byte[] downloadFile(String filePath, int bytesDownloaded) throws RemoteException;

    public boolean uploadFile(String filePath, byte[] bytes, boolean isAppend) throws RemoteException;

}


