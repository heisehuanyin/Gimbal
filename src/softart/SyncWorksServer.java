package softart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import softart.basictype.SPair;
import softart.task.TaskProcessor;
import softart.task.TaskStartRequestFeature;
import softart.task.Transaction;
import softart.task.talk.TalkServer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SyncWorksServer {
    private ServerSocket serverSocket = null;
    private AuthServiceFeature empower = null;
    private HashMap<String, TaskProcessor> pStack = new HashMap<>();


    private ThreadPoolExecutor tPool = new ThreadPoolExecutor(4, 500,
            1, TimeUnit.HOURS, new LinkedBlockingDeque<Runnable>());

    //              taskMask,
    private HashMap<String, SPair<Thread, ArrayList<String>>> daemons = new HashMap<>();


    /**
     * 生成同步服务器，指明服务器监听端口
     *
     * @param port 监听端口号
     */
    public SyncWorksServer(int port, AuthServiceFeature empowerService) {
        this.empower = empowerService;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("建立服务器过程出错！");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * 注册任务处理器
     *
     * @param processor 针对性的任务处理器
     */
    public void registerProcess(TaskProcessor processor) {
        this.pStack.put(processor.taskMask(), processor);
    }

    public Thread registerDaemons(TaskStartRequestFeature cell, Thread daemon){
        synchronized (this){
            SPair<Thread, ArrayList<String>> one = daemons.get(cell.taskMark());

            if (one == null){
                ArrayList<String> list = new ArrayList<>();
                list.add(cell.getUuidStr());
                daemons.put(cell.taskMark(), new SPair<>(daemon, list));
                daemon.start();

                return daemon;
            }
            else {
                if (one.getValue().contains(cell.getUuidStr()))
                    return one.getKey();

                one.getValue().add(cell.getUuidStr());
                daemons.put(cell.taskMark(), new SPair<>(one.getKey(), one.getValue()));
                return one.getKey();
            }
        }
    }

    public void closeDaemons(TaskStartRequestFeature cell){
        synchronized (this){
            SPair<Thread, ArrayList<String>> one = daemons.get(cell.taskMark());

            if (one == null)
                return;

            if (one.getValue().size() <= 1){
                daemons.remove(cell.taskMark());
                return;
            }

            one.getValue().remove(cell.getUuidStr());
        }
    }



    public AuthServiceFeature getAuthSrv() {
        return this.empower;
    }

    public HashMap<String, TaskProcessor> getpStack() {
        return this.pStack;
    }

    /**
     * 进入主循环，正式工作
     */
    public void workLoop() {

        while (true) {

            try {
                System.out.println("等待远程连接，端口号为：" + serverSocket.getLocalPort() + "...");

                Socket socket = serverSocket.accept();
                System.out.println("远程主机地址：" + socket.getRemoteSocketAddress());

                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();


                RequestFeature request = null;
                try {
                    request = new Request(input);
                } catch (MsgException e) {
                    System.out.println("登录解析过程异常+++++++++++");
                    System.out.println(e.type() + "<" + e.getDetail() + ">.");

                    try {
                        ReplyFeature reply = new Reply("NO_TOKEN", false);
                        reply.supply(e.type() + "<" + e.getDetail() + ">.").postReply(output);
                    } catch (MsgException e1) {
                        e1.printStackTrace();
                    }

                    output.close();
                    input.close();
                    e.printStackTrace();
                    continue;
                }


                String uuid = request.getUuidStr();
                String pswd = request.getKeyString();

                String newToken = empower.newToken(uuid, pswd);
                try {
                    ReplyFeature reply = null;
                    if (newToken.equals("")){
                        reply = new Reply("NO_TOKEN", false).supply("用户名或密码错误");
                        reply.postReply(output);
                        continue;
                    }
                    else{
                        reply = new Reply(newToken, true).supply("Welcome");
                        reply.postReply(output);
                    }

                } catch (MsgException e) {
                    System.out.println("登录回复过程异常++++++++++++++");
                    System.out.println(e.type() + "<" + e.getDetail() + ">.");
                    e.printStackTrace();

                    output.close();
                    input.close();
                    continue;
                }


                Transaction unit = new Transaction(this, newToken, input, output);
                this.tPool.execute(unit);

            } catch (IOException e) {
                System.out.println("服务器建立链接过程异常！");
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        File file = new File("./server_config.xml");
        if (!file.exists()){
            try {
                FileWriter writer = new FileWriter(file);
                writer.write("<?xml version='1.0' encoding='UTF-8'?>" +
                        "<root>" +
                        "<server-port>52525</server-port>" +
                        "<db-addr>127.0.0.1</db-addr>" +
                        "<db-port>3306</db-port>" +
                        "<db-name>web_account</db-name>" +
                        "<db-account>ws</db-account>" +
                        "<db-pswd>wspassword</db-pswd>" +
                        "</root>");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }




        Document doc = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(file);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return;
        } catch (SAXException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Element serPort = (Element) doc.getElementsByTagName("server-port").item(0);
        Element dbAddress = (Element) doc.getElementsByTagName("db-addr").item(0);
        Element dbPort = (Element) doc.getElementsByTagName("db-port").item(0);
        Element dbName = (Element) doc.getElementsByTagName("db-name").item(0);
        Element dbAccount = (Element) doc.getElementsByTagName("db-account").item(0);
        Element dbPswd = (Element) doc.getElementsByTagName("db-pswd").item(0);

        AuthServiceFeature service = new AuthService(dbAddress.getTextContent(),
                dbPort.getTextContent(),
                dbName.getTextContent(),
                dbAccount.getTextContent(),
                dbPswd.getTextContent());

        SyncWorksServer one = new SyncWorksServer(Integer.parseInt(serPort.getTextContent()), service);

        one.registerProcess(new TalkServer());
        one.workLoop();
    }
}

