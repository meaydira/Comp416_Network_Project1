package src;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer
{
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    public static final int DEFAULT_SERVER_PORT = 4444;
    private Socket s;
    //private BufferedReader br;
    protected DataInputStream is;
    protected DataOutputStream os;

    protected String serverAddress;
    protected int serverPort;
    final byte[] buf = new byte[4096];


    /**
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port port number of the server
     */
    public ConnectionToServer(String address, int port)
    {
        serverAddress = address;
        serverPort    = port;
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    public void Connect()
    {
        try
        {
            s=new Socket(serverAddress, serverPort);
            //br= new BufferedReader(new InputStreamReader(System.in));
            /*
            Read and write buffers on the socket
             */
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    /**
     * sends the message String to the server and retrives the answer
     * @param message input message string to the server
     * @return the received server answer
     */
    public String SendForAnswer(String message)
    {
        String response = new String();
        System.out.println("answer is :" + message);
        try
        {
            /*
            Sends the message to the server via PrintWriter
             */
            byte phase = (byte)0;
            byte type = (byte)1; // auth_challenge;
            Message.sendMessage(os, new Message(phase,type, message.getBytes().length, message));
            //os.println(message);
           // os.flush();
            /*
            Reads a line from the server via Buffer Reader
             */
            Message server_response = Message.nextMessageFromSocket(is);
            response = server_response.payload;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
        return response;
    }

    public String getServerOutput()
    {

        System.out.println("getting server response");

        String response = new String();
        try
        {
            ByteBuffer buff = ByteBuffer.wrap(buf);
            Message server_response = Message.nextMessageFromSocket(is);
            response = server_response.payload;
            System.out.println("response is here");
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("ConnectionToServer. SendForAnswer. Socket read Error");
        }
        return response;
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    public void Disconnect()
    {
        try
        {
            is.close();
            os.close();
            //br.close();
            s.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
