package softart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import softart.task.TaskStartRequest;
import softart.task.talk.MsgPostRequest;
import softart.task.talk.TalkStartRequest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * 配置 server——address
 * 配置 server-port
 * <p>
 * 命令传入 用户名 密码
 */

public class SyncWorksClient {
    private Socket socket = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private final String uuid;
    private final String pswd;
    private final String uuid2;

    public SyncWorksClient(String uuid, String pswd, String uuid2) {
        this.uuid = uuid;
        this.pswd = pswd;
        this.uuid2 = uuid2;
        File file = new File("./config.xml");

        if (!file.exists()) {
            try {
                file.createNewFile();
                FileWriter writer = new FileWriter(file);

                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<config>" +
                        "<server-ip>127.0.0.1</server-ip>" +
                        "<server-port>52525</server-port>" +
                        "</config>");
                writer.flush();
                writer.close();

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
            }
        }

        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docb = factory.newDocumentBuilder();
            doc = docb.parse(file);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (SAXException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Element root = doc.getDocumentElement();
        Element ip = (Element) root.getElementsByTagName("server-ip").item(0);
        Element port = (Element) root.getElementsByTagName("server-port").item(0);

        try {
            socket = new Socket(ip.getTextContent(), Integer.parseInt(port.getTextContent()));
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
            RequestFeature login = new Request(this.uuid, this.pswd);
            login.postRequest(outputStream);
            TaskStartRequest start = new TalkStartRequest(this.uuid, "token");
            start.postRequest(outputStream);

            Scanner can = new Scanner(System.in);

            while (true) {
                String line = can.nextLine();

                MsgPostRequest msgone = new MsgPostRequest(this.uuid, "token");
                msgone.appendTargetUser(this.uuid2);
                msgone.setMessage(line);

                msgone.postRequest(outputStream);
            }

        } catch (MsgException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        SyncWorksClient one = new SyncWorksClient(args[0], args[1], args[2]);
        one.doWork();
    }
}

class MsgAcceptWorker extends Thread {
    private InputStream inPort = null;

    public MsgAcceptWorker(InputStream srvPort) {
        this.inPort = srvPort;
    }

    @Override
    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inPort, Charset.forName("UTF-8")
        ));

        while (true) {
            try {
                String msgitem = reader.readLine();

                if (msgitem == null)
                    System.exit(0);

                System.out.println(msgitem);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}