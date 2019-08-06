package softart;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import softart.task.TaskGroove;
import softart.task.TaskServer;
import softart.task.talk.TalkServer;

public class SyncWorksServer {
    private ServerSocket serverSocket = null;
    private EmpowerServiceFeature empower = null;
    private HashMap<String, TaskServer> pStack = new HashMap<>();
    private ThreadPoolExecutor tPool = new ThreadPoolExecutor(4, 500,
            1, TimeUnit.HOURS, new LinkedBlockingDeque<Runnable>());

    /**
     * 生成同步服务器，指明服务器监听端口
     *
     * @param port 监听端口号
     */
    public SyncWorksServer(int port, EmpowerServiceFeature empowerService) {
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
    public void registerProcess(TaskServer processor) {
        this.pStack.put(processor.taskMask(), processor);
    }

    /**
     * 进入主循环，正式工作
     */
    public void waitFor() {

        while (true) {

            try {
                System.out.println("等待远程连接，端口号为：" + serverSocket.getLocalPort() + "...");

                Socket socket = serverSocket.accept();
                System.out.println("远程主机地址：" + socket.getRemoteSocketAddress());

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream(), Charset.forName("UTF-8")
                ));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(),Charset.forName("UTF-8")
                ));




                RequestFeature request = null;
                try {
                    request = new Request(reader);
                } catch (MsgException e) {
                    System.out.println("登录解析过程异常+++++++++++");
                    System.out.println(e.type() + "<" + e.getDetail() + ">.");

                    try {
                        ReplyFeature reply = new Reply("NO_TOKEN",false);
                        reply.supply(e.type() + "<" + e.getDetail() + ">.").postReplyToClient(writer);
                    } catch (MsgException e1) {
                        e1.printStackTrace();
                    }

                    writer.close();
                    reader.close();
                    e.printStackTrace();
                    continue;
                }




                String uuid = request.getUuidStr();
                String pswd = request.getKeyString();

                String newToken = empower.newToken(uuid, pswd);
                try {
                    ReplyFeature reply = null;
                    if (newToken.equals(""))
                        reply = new Reply("NO_TOKEN", false).supply("用户名或密码错误");
                    else
                        reply = new Reply(newToken, true).supply("Welcome");

                    reply.postReplyToClient(writer);
                } catch (MsgException e) {
                    System.out.println("登录回复过程异常++++++++++++++");
                    System.out.println(e.type() + "<" + e.getDetail() + ">.");
                    e.printStackTrace();

                    writer.close();
                    reader.close();
                    continue;
                }




                TaskGroove unit = new TaskGroove(this.pStack,
                        this.empower, newToken, reader, writer);
                this.tPool.execute(unit);

            } catch (IOException e) {
                System.out.println("服务器建立链接过程异常！");
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        EmpowerServiceFeature service = new EmpowerService();
        SyncWorksServer one = new SyncWorksServer(Integer.parseInt(args[0]), service);

        one.registerProcess(new TalkServer());
        one.waitFor();
    }
}

