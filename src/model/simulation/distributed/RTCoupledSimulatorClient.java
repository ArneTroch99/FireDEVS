/*  
 * Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
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
 * A real-time simulator that can interface with a remote coordinator-server
 * as part of a distributed real-time simulation.
 */
public class RTCoupledSimulatorClient extends coupledRTSimulator
{
    /**
     * The client aspect of this class, which allows for communication
     * with a server.
     */
    protected Client client;

    /**
     * Constructs an object of this class.
     *
     * @param   devs            The devs component upon which this simulator
     *                          will operate.
     * @param   serverAddress   The server's IP address.
     * @param   serverPort      The server's port number.
     */
    public RTCoupledSimulatorClient(IOBasicDevs devs, String serverAddress,
        int serverPort)
    {
        super(devs);

        // create the client aspect of this simulator
        client = new Client(devs.getName(), serverAddress, serverPort);
    }

    /**
     * Overriding the parent class behavior, this sends this simulator's
     * current output message to the server, rather than to other local
     * simulators or coordinators.
     */
    public void sendMessages()
    {
        // if this simulator's output message is non-empty
        if (!output.isEmpty()) {
            // send the output message to the server
            client.sendMessageToServer(output.toString());

            // clear the output message
            output = new message();
        }
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
            simulate(numIterations);
        }

        /**
         * See parent method.
         */
        protected void inputReceived(message message)
        {
            // make the given message this simulator's input message
            input = message;

            // wake up the parent class run() method, which has been waiting
            // for an external input, so it can handle the message
            inputReady = true;
            synchronized (RTCoupledSimulatorClient.this) {
                RTCoupledSimulatorClient.this.notify();
            }
        }
    }
}
