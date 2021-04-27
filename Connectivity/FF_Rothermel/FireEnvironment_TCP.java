package FF_Rothermel;

import util.Logging;

import java.io.*;
import java.net.Socket;

public class FireEnvironment_TCP extends FireEnvironment {

    final String localHost = "localhost";
    final int port = 65535;

    Socket socket;

    public String receiveStringData() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            Logging.log("ERROR: Could not read from the TCP connection, default dynamic values will be used. \n This happened be" +
                    "because of the following exception: \n " + e.toString(), Logging.error);
        }
        return "";
    }

    public void connect() {
        try {
            socket = new Socket(localHost, port);
        } catch (IOException e) {
            Logging.log("ERROR: Could not open the TCP connection, default dynamic values will be used. \n This happened be" +
                    "because of the following exception: \n " + e.toString(), Logging.error);
        }
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            Logging.log("ERROR: Could not close the TCP connection. \n This happened be because of the following exception: \n "
                    + e.toString(), Logging.error);
        } catch (NullPointerException ignore){}     // This means the connection never opened
    }


}
