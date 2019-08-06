package softart.task;

import softart.EmpowerServiceFeature;
import softart.MsgException;
import softart.Reply;
import softart.ReplyFeature;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class TaskGroove implements Runnable {
    private HashMap<String, TaskServer> pStack = null;
    private EmpowerServiceFeature service = null;
    private Socket socket = null;
    private String token = "";

    public TaskGroove(HashMap<String, TaskServer> pStack, EmpowerServiceFeature servie,
                      String token, Socket socket) {
        this.pStack = pStack;
        this.service = servie;
        this.socket = socket;
        this.token = token;
    }

    @Override
    public void run() {

        while (true) {

            TaskRequestFeature request = null;
            try {
                request = new TaskRequest(socket);
            } catch (MsgException e) {
                System.out.println("任务分析过程异常+++++++++++");
                System.out.println(e.type() + "<" + e.getDetail() + ">.");
                e.printStackTrace();

                try {
                    ReplyFeature reply = new Reply(token, false);
                    reply.supply(e.type() + "<" + e.getDetail() + ">.").replyToSocket(socket);
                } catch (MsgException e1) {
                    System.out.println(e1.type()+"<"+e1.getDetail()+">.");
                    e1.printStackTrace();
                }

                break;
            }





            String temp = request.taskMark();
            EmpowerServiceFeature.Privileges type;
            try {
                type = EmpowerServiceFeature.Privileges.valueOf(temp);
            } catch (Exception e) {
                System.out.println("权限鉴别过程异常<未知权限:" + temp + ">.");
                e.printStackTrace();

                try {
                    ReplyFeature reply = new Reply(token, false);
                    reply.supply("权限鉴别过程异常<未知权限:" + temp + ">.").replyToSocket(socket);
                } catch (MsgException e1) {
                    System.out.println(e1.type()+"<"+e1.getDetail()+">.");
                    e1.printStackTrace();
                    break;
                }

                continue;
            }

            try {
                ReplyFeature reply = null;
                if (!service.privilegeCheck(request.getUuidStr(), request.getKeyString(), type)) {
                    reply = new Reply(token, false).supply("权限不足");
                    reply.replyToSocket(socket);
                    continue;
                } else {
                    reply = new Reply(token, true).supply("Please Continue");
                    reply.replyToSocket(socket);
                }
            } catch (MsgException e) {
                System.out.println(e.type()+"<"+e.getDetail()+">.");
                e.printStackTrace();
                break;
            }




            TaskServer processor = pStack.get(request.taskMark()).newEntities(request, socket);
            processor.taskProcess();
        }

        try {
            socket.close();
        } catch (IOException e) {
            System.out.println("Socket关闭异常+++++++++++++");
            e.printStackTrace();
        }
    }
}
