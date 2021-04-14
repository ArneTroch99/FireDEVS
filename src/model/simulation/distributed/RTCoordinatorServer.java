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
 * The top-level coordinator-server in a distributed real-time simulation
 * (DRTS).  This interfaces with remote client simulators (as well as
 * remote subordinate coordinators) to coordinate a DRTS.  There is
 * exactly one of these in each DRTS.
 */
public class RTCoordinatorServer extends RTcoordinator
{
    /**
     * How many simulation iterations this coordinator is to perform.
     */
    protected int numIterations;

    /**
     * How many client simulators have yet to register themselves with this
     * server.  It is initialized with a positive value so that its reaching
     * zero can be used as a signal for all clients being registered,
     * even before the real count has been set.
     */
    protected int registerCount = 1;

    /**
     * Constructs an object of this class.
     *
     * @param   devs            The coupled component whose simulation this
     *                          coordinator is to coordinate.
     * @param   numIterations_  How many simulation iterations this
     *                          coordinator is to perform.
     * @param   port            The port number on which this server should
     *                          listen for new clients.
     * @param   shouldBroadcastInitialize
     *                          Whether this server should broadcast a
     *                          message that says to initialize to all
     *                          subordinate simulators, once all clients
     *                          have connected.
     */
    public RTCoordinatorServer(coupledDevs devs, int numIterations_,
        int port, boolean shouldBroadcastInitialize)
    {
        super(devs, true);
        numIterations = numIterations_;

        // start the thread that will wait for the clients to connect
        // with this server
        new WaitForClientsToConnectThread(
            port, shouldBroadcastInitialize).start();
    }

    /**
     * A convienence constructor.
     */
    public RTCoordinatorServer(coupledDevs devs, int numIterations)
    {
        this(devs, numIterations, Constants.serverPortNumber, true);
    }

    /**
     * Informs this coordinator of the existence of (a proxy of) one of the
     * client simulators it is to coordinate.  Also, has this coordinator
     * associate that proxy with the name of the devs component upon which
     * the actual simulator will operate.
     *
     * @param   proxy       A local proxy for a client-side simulator.
     * @param   devsName    The name of the devs component on which the
     *                      simulator will operate.
     */
    public void registerSimulatorProxy(coupledSimulator proxy,
        String devsName)
    {
        // add the given proxy to the set of simulator proxies with which
        // this coordinator will interact
        simulators.add(proxy);

        // associate the proxy with the name of the devs component
        modelToSim.put(devsName, proxy);

        // one more simulator has registered with this coordinator
        registerCount--;
    }

    /**
     * This leaves out the creation of simulators for this coordinator's
     * subordinate components that is found in the parent class
     * method, because in a distributed simulation those simulators
     * are instead created one-by-one on the client side.
     */
    public void setSimulators()
    {
        tellAllSimsSetModToSim();
    }

    /**
     * Sends the given message to all the client simulators via their
     * proxies.
     *
     * @param   message     The message to send.
     */
    protected void broadcast(String message)
    {
        System.out.println("broadcast: tell all send " + message);
        Class[] classes = { String.class };
        Object[] args = {message};
        simulators.tellAll("sendMessage", classes, args);
    }

    /**
     * A thread that waits for the client simulators to connect with this
     * server.
     */
    protected class WaitForClientsToConnectThread extends Thread
    {
        protected int port;

        /**
         * Whether this thread should broadcast a message that says
         * to initialize to all subordinate simulators, once all clients
         * have connected.
         */
        protected boolean shouldBroadcastInitialize;

        /**
         * Constructs an object of this class.
         *
         * @param   port_       The port number on which to listen for clients.
         * @param   shouldBroadcastInitialize_
         *                      Whether this thread should broadcast a
         *                      message that says to initialize to all
         *                      subordinate simulators, once all clients
         *                      have connected.
         */
        public WaitForClientsToConnectThread(int port_,
            boolean shouldBroadcastInitialize_)
        {
            port = port_;
            shouldBroadcastInitialize = shouldBroadcastInitialize_;
        }

        public void run()
        {
            // create a server socket on the given port to accept
            // client connections
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(port);
            } catch (Exception e) {e.printStackTrace();}

            // detm how many subordinate components there are that must
            // register their names with this server
            registerCount = myCoupled.getComponents().size();

            // while not all the components subordinate to this server have
            // connected
            int numConnected = 0;
            final int numShouldConnect = registerCount;
            while (numConnected < numShouldConnect) {
                // wait for another client component to connect
                Socket socket = null;
                try {
                    System.out.println("Waiting for connection...");
                    socket = serverSocket.accept();
                } catch (Exception e) {e.printStackTrace();}

                // create a proxy for the newly connected component
                System.out.println("Yes!  Received a connection!");
                numConnected++;
                System.out.println("number connected:" + numConnected);
                new SimulatorProxy(socket, RTCoordinatorServer.this);
            }

            // wait for all the client components to have registered themselves
            // with this server by calling its addSimulator() method
            while (registerCount > 0) util.Util.sleep(1000);

            setSimulators();
            informCoupling();

            if (shouldBroadcastInitialize) {
                broadcast(Constants.initializeMessage + util.Util.time());
                broadcast(Constants.startSimulateMessage + numIterations);
            }
        }
    }
}
