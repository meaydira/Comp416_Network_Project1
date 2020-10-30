package src;

import java.util.Scanner;

public class Main
{
    public static void main(String args[])
    {
        src.ConnectionToServer connectionToServer = new ConnectionToServer(ConnectionToServer.DEFAULT_SERVER_ADDRESS, ConnectionToServer.DEFAULT_SERVER_PORT);
        connectionToServer.Connect();
        Scanner scanner = new Scanner(System.in);
        //System.out.println("Enter a message for the echo");
        String message = "";

        System.out.println("Response from server: " + connectionToServer.getServerOutput());

        while (!message.equals("QUIT"))
        {
            message = scanner.nextLine();
            System.out.println("Response from server: " + connectionToServer.SendForAnswer(message));

        }
        connectionToServer.Disconnect();
    }
}
