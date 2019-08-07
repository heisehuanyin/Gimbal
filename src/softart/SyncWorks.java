package softart;

import softart.task.TaskRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
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
        OutputStream output = null;
        try {
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            RequestFeature request = new Request("token","pwd");
            request.postRequest(output);

            TaskRequest req = new TaskRequest("uuid","token",
                    EmpowerService.Privileges.TalkService.toString());
            req.postRequest(output);
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
