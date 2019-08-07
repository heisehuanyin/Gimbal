package softart.task.talk;

import javafx.util.Pair;
import softart.MsgException;
import softart.task.TaskServer;
import softart.task.TaskStartRequestFeature;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class TalkServer implements TaskServer {
    private static MsgRouterThread msg_service = new MsgRouterThread();

    private TaskStartRequestFeature request = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private LinkedBlockingQueue<String> msg_list = new LinkedBlockingQueue<>();


    public TalkServer() {
    }

    public TalkServer(TaskStartRequestFeature requestFeature, InputStream input, OutputStream output) {
        this.request = requestFeature;
        this.inputStream = input;
        this.outputStream = output;

        if (!msg_service.isAlive()) {
            msg_service.start();
        }
        msg_service.connectRouter(requestFeature.getUuidStr(), this.outputStream);
    }

    /**
     * 本处理任务针对的任务类型
     */
    @Override
    public String taskMask() {
        return this.getClass().getSimpleName();
    }

    /**
     * 新建可运行处理实体
     *
     * @param request 消息头XML的根节点
     * @param input   socket端口
     * @param output
     * @return 返回一个可运行实体
     */
    @Override
    public TaskServer newEntities(TaskStartRequestFeature request, InputStream input, OutputStream output) {
        TalkServer rtn = new TalkServer(request, input, output);
        return rtn;
    }

    /**
     * 执行任务
     */
    @Override
    public void taskProcess() {
        System.out.println("Talk Server Started.");

        while (true){
            try {
                MsgPostRequest req = new MsgPostRequest(inputStream);

                ArrayList<String> list = req.getTargetsUser();
                for (String usr:list){
                    msg_service.postMsg(usr, req.getMessage());
                }
            } catch (MsgException e) {
                msg_service.disconnect(request.getUuidStr());
                System.out.println(e.type()+"<"+e.getDetail()+">.");
                e.printStackTrace();
            }
        }
    }
}


class MsgRouterThread extends Thread {
    private BlockedMap<BufferedWriter> UserPool = new BlockedMap<>();
    private LinkedBlockingQueue<Pair<String, String>> msgQueue =
            new LinkedBlockingQueue<>();

    @Override
    public void run() {
        while (true){

            try {
                Pair<String, String> msgItem = msgQueue.take();
                BufferedWriter outPort = UserPool.get(msgItem.getKey());

                if (outPort!=null){
                    outPort.write(msgItem.getValue());
                    outPort.newLine();
                    outPort.flush();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 连接到消息派发服务
     * @param uuid 目标id
     * @param clientPort 面向客户端
     */
    public void connectRouter(String uuid, OutputStream clientPort) {
        try {
            UserPool.put(uuid, new BufferedWriter(
                    new OutputStreamWriter(clientPort, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 断开消息派发服务
     * @param uuid 目标id线程
     */
    public void disconnect(String uuid) {
        UserPool.remove(uuid);
    }

    /**
     * 发送消息
     * @param toUuid 目标
     * @param msg 消息
     */
    public void postMsg(String toUuid, String msg) {
        try {
            msgQueue.put(new Pair<>(toUuid, msg));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class BlockedMap<T> {
    private HashMap<String, T> container = new HashMap<>();

    public void put(String key, T value) {
        synchronized (this) {
            container.put(key, value);
        }
    }

    public T get(String key) {
        synchronized (this) {
            return container.get(key);
        }
    }

    public void remove(String key) {
        synchronized (this) {
            container.remove(key);
        }
    }
}