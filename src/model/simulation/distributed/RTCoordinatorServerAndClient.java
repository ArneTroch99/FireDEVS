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
import util.*;

/**
 * A combination of an RTCoordinatorServer and an RTCoordinatorClient
 * which can coordinate the actions of a subhierarchy of components in a
 * real-time distributed simulation, while interfacing with a superordinate
 * server in the hierarchy of coordinators presiding over the simulation.
 */
public class RTCoordinatorServerAndClient extends RTCoordinatorServer
{
    /**
     * The client coordinator aspect of this object.
     */
    protected RTCoordinatorClient client;

    /**
     * Constructs an object of this class.
     *
     * @param   devs            The devs component over whose simulation this
     *                          coordinator will preside.
     * @param   serverAddress   The superordinate server's IP address.
     * @param   serverPort      The superordinate server's port number.
     * @param   myPort          This server's port number.
     */
    public RTCoordinatorServerAndClient(coupledDevs devs, String serverAddress,
        int serverPort, int myPort)
    {
        super(devs, 0, myPort, false);

        // wait for all clients to be registered with this object's
        // server before creating its client aspect below
        while (registerCount > 0) util.Util.sleep(1000);

        // create the client-coordinator aspect of this server
        client = new RTCoordinatorClient(
            (Coupled)devs, serverAddress, serverPort);
    }

    /**
     * Overrides the parent class method's bevavior to have
     * this server's associated client coordinator pass its
     * output message on to the next-higher-up server.
     */
    public void putMyMessages(ContentInterface content)
    {
        client.putMyMessages(content);
    }

    /**
     * The client-coordinator aspect of this class.
     */
    protected class RTCoordinatorClient
        extends model.simulation.distributed.RTCoordinatorClient
    {
        /**
         * See parent constructor.
         */
        public RTCoordinatorClient(Coupled devs, String serverAddress,
            int serverPort)
        {
            super(devs, serverAddress, serverPort);
        }

        /**
         * See parent method.
         */
        protected void createClient(Coupled devs, String serverAddress,
            int serverPort)
        {
            // create the client aspect of this simulator
            this.client = new Client(devs.getName(), serverAddress, serverPort);
        }

        /**
         * See parent class.
         */
        protected class Client
            extends model.simulation.distributed.RTCoordinatorClient.Client
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
            protected void startSimulateMessageReceived(int numIterations)
            {
                // tell all subordinate simulators to start simulating
                broadcast(Constants.startSimulateMessage + numIterations);
            }

            /**
             * See parent method.
             */
            protected void initializeMessageReceived(String message)
            {
                // broadcast an initialize message (including the time specified
                // in the given message) to the subordinate simulators
                double currentTime = Double.parseDouble(message.substring(
                    message.indexOf(':') + 1));
                broadcast("initialize:" + currentTime);
            }
        }

        /**
         * Overrides the parent class method's behavior to get
         * this client's input message transmitted
         * by its associated server down to its subordinate simulators.
         */
        public void sendDownMessages()
        {
            RTCoordinatorServerAndClient.this.input = input;
            RTCoordinatorServerAndClient.this.sendDownMessages();
        }
    }

    /**
     * Overrides the parent class method's behavior to transmit this
     * server's current input message down to its subordinate simulators.
     */
    public void sendDownMessages()
    {
        // if this server's current input message is non-empty
        if (!input.isEmpty()) {
            // for each content-destination pair in the input message
            convertInput(input).forEach((key, value) -> {

                // if we can find the proxy of the simulator of the
                // destination component of this content, pass it
                // this content
                SimulatorProxy proxy = (SimulatorProxy)
                    modelToSim.get(key);
                if (proxy != null) proxy.putMessages(value);
            });
        }
    }
}
