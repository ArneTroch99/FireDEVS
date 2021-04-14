/* Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */ 

package model.simulation.distributed;

import java.io.*;
import java.net.*;
import java.util.*;

import GenCol.*;


import model.modeling.*;
import model.simulation.*;
import model.simulation.realTime.*;
import util.*;

/**
 * A real-time coordinator that can interface with a remote coordinator-server
 * as part of a real-time distributed simulation.  The devs components
 * whose simulation this client will coordinate are all local to each other
 * and to this client.
 */
public class RTCoordinatorClient extends RTcoordinator
{
    /**
     * The client aspect of this class, which allows for communication
     * with a server.
     */
    protected Client client;

    /**
     * Constructs an object of this class.
     *
     * @param   devs            The coupled devs component over whose
     *                          simulation this coordinator will preside.
     * @param   serverAddress   The server's IP address.
     * @param   serverPort      The server's port number.
     */
    public RTCoordinatorClient(Coupled devs, String serverAddress,
        int serverPort)
    {
        super(devs);

        createClient(devs, serverAddress, serverPort);
    }

    /**
     * This may be overridden to return a client object of a different class.
     */
    protected void createClient(Coupled devs, String serverAddress,
        int serverPort)
    {
        // create the client aspect of this simulator
        client = new Client(devs.getName(), serverAddress, serverPort);
    }

    /**
     * Overrides the parent class method behavior to send this
     * coordinator's current output message to the server.
     *
     * @param   content     A content to add to the output message
     *                      before sending it.
     */
    public void putMyMessages(ContentInterface content)
    {
        // send this coordinator's current output message, augmented
        // with the given content, to the server
        output.add(content);
        if (!output.isEmpty()) client.sendMessageToServer(output.toString());

        // reset the output message
        output = new message();
    }

    /**
     * See parent class.
     */
    protected class Client extends model.simulation.distributed.Client
    {
        /**
         * See parent constructor.
         */
        public Client(String devsName, String serverAddress, int serverPort)
        {
            super(devsName, serverAddress, serverPort);
        }

        /**
         * See parent method.
         */
        protected void initializeMessageReceived(String message) {initialize();}

        /**
         * See parent method.
         */
        protected void startSimulateMessageReceived(int numIterations)
        {
            // tell all of this coordinator's subordinate simulators to
            // simulate
            Class[] classes = { Integer.class };
            Object[] args = {new Integer(numIterations)};
            simulators.tellAll("simulate", classes, args);
        }

        /**
         * See parent method.
         */
        protected void inputReceived(message message)
        {
            // pass the given message along to subordinate simulators as this
            // coordinator's input message
            input = message;
            sendDownMessages();

            // reset this coordinator's input message
            input = new message();
        }
    }
}
