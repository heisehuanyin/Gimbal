package softart;

import softart.task.TaskGroove;
import softart.task.TaskServer;
import softart.task.talk.TalkServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SyncWorksServer {
    private ServerSocket serverSocket = null;
    private EmpowerServiceFeature empower = null;
    private HashMap<String, TaskServer> pStack = new HashMap<>();
    private ThreadPoolExecutor tPool = new ThreadPoolExecutor(4, 500,
            1, TimeUnit.HOURS, new LinkedBlockingDeque<Runnable>());

    public EmpowerServiceFeature getEmpower(){
        return this.empower;
    }
    public HashMap<String, TaskServer> getpStack(){
        return this.pStack;
    }
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

                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();




                RequestFeature request = null;
                try {
                    request = new Request(input);
                } catch (MsgException e) {
                    System.out.println("登录解析过程异常+++++++++++");
                    System.out.println(e.type() + "<" + e.getDetail() + ">.");

                    try {
                        ReplyFeature reply = new Reply("NO_TOKEN",false);
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
                    if (newToken.equals(""))
                        reply = new Reply("NO_TOKEN", false).supply("用户名或密码错误");
                    else
                        reply = new Reply(newToken, true).supply("Welcome");

                    reply.postReply(output);
                } catch (MsgException e) {
                    System.out.println("登录回复过程异常++++++++++++++");
                    System.out.println(e.type() + "<" + e.getDetail() + ">.");
                    e.printStackTrace();

                    output.close();
                    input.close();
                    continue;
                }




                TaskGroove unit = new TaskGroove(this, newToken, input, output);
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

