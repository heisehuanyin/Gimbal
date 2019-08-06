package softart;

import softart.task.TaskRequest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.*;
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

    public void doWork() {
        BufferedWriter writer = null ;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(),"UTF-8"
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            RequestFeature request = new Request("token","pwd");
            request.postRequestToServer(writer);

            TaskRequest req = new TaskRequest("uuid","token",
                    EmpowerService.Privileges.TalkService.toString());
            req.postRequestToServer(writer);
        } catch (MsgException e) {
            e.printStackTrace();
        }

        (new Scanner(System.in)).nextLine();

    }

    public static void main(String[] args) {
        SyncWorks one = new SyncWorks(args[0], Integer.parseInt(args[1]));
        one.doWork();
    }
}
