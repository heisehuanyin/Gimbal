package softart;

import softart.task.TaskStartRequest;
import softart.task.TaskStartRequestFeature;
import softart.task.talk.MsgPostRequest;
import softart.task.talk.TalkStartRequest;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;


public class SyncWorks {
    private InputStream inputStream=null;
    private OutputStream outputStream=null;
    private final String _address;
    private final int _port;

    public SyncWorks(String addr, int port) {
        _address = addr;
        _port = port;

        try {
            Socket socket = new Socket(_address, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("未能建立Socket通道");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void doWork() {
        new ReceiveMsg(inputStream).start();

        try {
            RequestFeature login = new Request("uuid","pwd");
            TaskStartRequest start = new TalkStartRequest("uuid","token");

            this.ConnectToServer(login, start);
            Scanner can = new Scanner(System.in);

            while (true){
                String line = can.nextLine();

                MsgPostRequest msgone = new MsgPostRequest("uuid","token");
                msgone.appendTargetUser("uuid");
                msgone.setMessage(line);

                msgone.postRequest(outputStream);
            }

        } catch (MsgException e) {
            e.printStackTrace();
        }

    }
    public void ConnectToServer(RequestFeature loginReq, TaskStartRequestFeature startReq) throws MsgException {
        loginReq.postRequest(this.outputStream);
        startReq.postRequest(this.outputStream);
    }

    public static void main(String[] args) {

        SyncWorks one = new SyncWorks(args[0], Integer.parseInt(args[1]));
        one.doWork();
    }
}

class ReceiveMsg extends Thread{
    private InputStream inPort = null;

    public ReceiveMsg(InputStream srvPort){
        this.inPort = srvPort;
    }

    @Override
    public void run(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inPort, Charset.forName("UTF-8")
        ));

        while (true){
            try {
                String msgitem = reader.readLine();
                System.out.println(msgitem);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}