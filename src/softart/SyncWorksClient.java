package softart;

import softart.task.TaskStartRequest;
import softart.task.TaskStartRequestFeature;
import softart.task.talk.MsgPostRequest;
import softart.task.talk.TalkStartRequest;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;


public class SyncWorksClient {
    private Socket socket = null;
    private InputStream inputStream=null;
    private OutputStream outputStream=null;
    private final String _address;

    public SyncWorksClient(String addr, int port) {
        _address = addr;

        try {
            socket = new Socket(_address, port);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("未能建立Socket通道");
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void doWork() {
        new MsgAcceptWorker(inputStream).start();

        try {
            RequestFeature login = new Request("uuid","pwd");
            login.postRequest(outputStream);
            TaskStartRequest start = new TalkStartRequest("uuid","token");
            start.postRequest(outputStream);

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

    public static void main(String[] args) {

        SyncWorksClient one = new SyncWorksClient(args[0], Integer.parseInt(args[1]));
        one.doWork();
    }
}

class MsgAcceptWorker extends Thread{
    private InputStream inPort = null;

    public MsgAcceptWorker(InputStream srvPort){
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

                if (msgitem==null)
                    System.exit(0);

                System.out.println(msgitem);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}