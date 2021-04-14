/*  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation.distributed;

import java.io.*;
import java.net.*;
import java.util.*;

import GenCol.*;


import model.modeling.*;
import util.*;

/**
 * A component meant to be composed into a simulator or coordinator to
 * allow it to communicate with a server-coordinator overseeing its
 * operation within a distributed simulation.
 */
abstract public class Client
{
    /**
     * The name of the devs component to identify this client by.
     */
    protected String devsName;

    /**
     * The input stream for receiving input from the server.
     */
    protected DataInputStream inputStream;

    /**
     * The print stream for sending output to the server.
     */
    protected PrintStream printStream;

    /**
     * Constructs an object of this class.
     *
     * @param   devsName_       The name of the devs component to identify
     *                          this client by.
     * @param   serverAddress   The server's IP address.
     * @param   serverPort      The server's port number.
     */
    public Client(String devsName_, String serverAddress, int serverPort)
    {
        devsName = devsName_;

        // connect to the server
        connect(serverAddress, serverPort);

        // get this client's listen-for-server-messages-thread going
        (new ListenForServerMessagesThread()).start();
    }

    /**
     * Receives and processes all the messages sent by the server.
     */
    protected class ListenForServerMessagesThread extends Thread
    {
        public void run()
        {
            // wait for this coordinator's proxy to be created on the
            // server side
            util.Util.sleep(1000);

            // send the name of this client's devs-component to the server,
            // where it will be intercepted by this client's proxy, which
            // will register itself with the server under that name
            sendMessageToServer(devsName);

            // wait for the simulation to begin
            waitForInitialize();
            waitForStartSimulate();

            // react to input from the server as its arrives
            while (true) {
                waitForInput();
            }
        }
    }

    /**
     * Informs this client that its initialization message has been
     * received.
     *
     * @param   message     The message received, in case the method
     *                      implementation would like to further parse it.
     */
    abstract protected void initializeMessageReceived(String message);

    /**
     * Waits for a message from the server that says to initialize this
     * client.
     */
    protected void waitForInitialize()
    {
        // if the next message from the server is not the one that says
        // to initialize
        String message = readMessage();
        if (!message.startsWith(Constants.initializeMessage)) {
            // report this
            System.out.println("initialize message did not arrive when expected");
        }

        initializeMessageReceived(message);
    }

    /**
     * Informs this client that it has received its start-simulate message
     * from the server.
     *
     * @param   numIterations       How many simulation iterations the server
     *                              say this client is to perform.
     */
    abstract protected void startSimulateMessageReceived(int numIterations);

    /**
     * Waits for a message from the server that says to start simulating.
     */
    protected void waitForStartSimulate()
    {
        // if the next message from the server is not the one that says
        // to start simulating
        String message = readMessage();
        if (!message.startsWith(Constants.startSimulateMessage)) {
            // report this
            System.out.println("start-simulate message did not arrive when expected");
        }

        // detm the number of iterations specified in the message
        int numIterations = (int)Double.parseDouble(
            message.substring(message.indexOf(":") + 1, message.length()));

        System.out.println("---start simulate");

        startSimulateMessageReceived(numIterations);
    }

    /**
     * Informs this client it has received an input message from the server.
     *
     * @param   message     The message received.
     */
    abstract protected void inputReceived(message message);

    /**
     * Waits for an input message from the server, then gets that input
     * processed.
     */
    protected void waitForInput()
    {
        // wait for the next input from the server
        String string = readMessage();
        System.out.println(devsName + " received input: " + string);

        // if a message can be formed from the input
        message message = Util.interpret(string);
        if (message != null) {
            inputReceived(message);
        }
    }

    /**
     * Sends the given message to the server.
     *
     * @param   message     The message to send.
     */
    protected void sendMessageToServer(String message)
    {
        printStream.println(message);
        System.out.println(devsName + " sends " + message);
    }

    /**
     * Returns the next line read from this client's input-stream.
     */
    protected String readMessage()
    {
        try {
            return inputStream.readLine();
        } catch (IOException e) {e.printStackTrace(); return "";}
    }

    /**
     * Connects using a socket to the given server address and port.  Creates
     * an input-stream and print-stream on that socket.
     *
     * @param   serverAddress       The IP address to which to connect.
     * @param   serverPort          The port number on which to connect.
     */
    protected void connect(String serverAddress, int serverPort)
    {
        Socket socket = null;
        try {
            // try to connect
            socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected!");
        } catch (Exception e) {
            e.printStackTrace();

            // wait a bit, then retry
            util.Util.sleep(1000);
            connect(serverAddress, serverPort);
            return;
        }

        try {
            // create input and output streams on the socket
            inputStream = new DataInputStream(socket.getInputStream());
            printStream = new PrintStream(
                new DataOutputStream(socket.getOutputStream()));
            System.out.println("Open was successful!");
        } catch (IOException e) {e.printStackTrace();}
    }
}
