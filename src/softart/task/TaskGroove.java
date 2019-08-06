package softart.task;

import softart.EmpowerServiceFeature;
import softart.MsgException;
import softart.Reply;
import softart.ReplyFeature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class TaskGroove implements Runnable {
    private HashMap<String, TaskServer> pStack = null;
    private EmpowerServiceFeature service = null;
    private String token = "";
    private BufferedWriter writer = null;
    private BufferedReader reader = null;

    public TaskGroove(HashMap<String, TaskServer> pStack, EmpowerServiceFeature servie,
                      String token, BufferedReader reader, BufferedWriter writer) {
        this.pStack = pStack;
        this.service = servie;
        this.token = token;

        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void run() {

        while (true) {

            TaskRequestFeature request = null;
            try {
                request = new TaskRequest(reader);
            } catch (MsgException e) {
                System.out.println("任务分析过程异常+++++++++++");
                System.out.println(e.type() + "<" + e.getDetail() + ">.");
                e.printStackTrace();

                try {
                    ReplyFeature reply = new Reply(token, false);
                    reply.supply(e.type() + "<" + e.getDetail() + ">.").postReplyToClient(writer);
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
                    reply.supply("权限鉴别过程异常<未知权限:" + temp + ">.").postReplyToClient(writer);
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
                    reply.postReplyToClient(writer);
                    continue;
                } else {
                    reply = new Reply(token, true).supply("Please Continue");
                    reply.postReplyToClient(writer);
                }
            } catch (MsgException e) {
                System.out.println(e.type()+"<"+e.getDetail()+">.");
                e.printStackTrace();
                break;
            }




            TaskServer processor = pStack.get(request.taskMark()).newEntities(request, reader, writer);
            processor.taskProcess();
        }

        try {
            writer.close();
            reader.close();
        } catch (IOException e) {
            System.out.println("Socket关闭异常+++++++++++++");
            e.printStackTrace();
        }
    }
}
