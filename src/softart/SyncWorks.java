package softart;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.Charset;
import java.util.Scanner;


public class SyncWorks {
    private Socket socket = null;
    private final String _address;
    private final int _port;

    public SyncWorks(String addr, int port) {
        _address = addr;
        _port = port;

        try {
            socket = new Socket(_address, port);
        } catch (IOException e) {
            System.out.println("未能建立Socket通道");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void doWork(){
        try {
            Scanner in = new Scanner(System.in);
            OutputStream out = socket.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));

            while (in.hasNext()){
                String line = in.nextLine();
                writer.write(line+"\n");
                writer.flush();
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SyncWorks one = new SyncWorks(args[0], Integer.parseInt(args[1]));
        one.doWork();
    }
}
