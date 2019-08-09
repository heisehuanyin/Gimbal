package softart.task.talk;

import softart.basictype.SPair;

import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;

class MsgRouterThread extends Thread {
    private BlockedMap<BufferedWriter> UserPool = new BlockedMap<>();
    private LinkedBlockingQueue<SPair<String, String>> msgQueue = new LinkedBlockingQueue<>();

    @Override
    public void run() {
        try {

            while (true) {
                SPair<String, String> msgItem = msgQueue.take();
                BufferedWriter outPort = UserPool.get(msgItem.getKey());

                if (outPort != null) {
                    outPort.write(msgItem.getValue());
                    outPort.newLine();
                    outPort.flush();
                }
            }


        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("-------------------------------------------------------------");
    }

    /**
     * 连接到消息派发服务
     *
     * @param uuid       目标id
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
     *
     * @param uuid 目标id线程
     */
    public void disconnect(String uuid) {
        UserPool.remove(uuid);
    }

    /**
     * 发送消息
     *
     * @param toUuid 目标
     * @param msg    消息
     */
    public void postMsg(String toUuid, String msg) {
        try {
            msgQueue.put(new SPair<>(toUuid, msg));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
