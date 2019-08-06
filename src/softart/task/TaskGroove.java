package softart.task;

import softart.*;

import java.io.*;

public class TaskGroove implements Runnable {
    private SyncWorksServer server = null;
    private String token = "";
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    public TaskGroove(SyncWorksServer server, String token, InputStream input, OutputStream output) {
        this.server = server;
        this.token = token;

        this.inputStream = input;
        this.outputStream = output;
    }

    @Override
    public void run() {

        while (true) {

            TaskRequestFeature request = null;
            try {
                request = new TaskRequest(inputStream);
            } catch (MsgException e) {
                System.out.println("任务分析过程异常+++++++++++");
                System.out.println(e.type() + "<" + e.getDetail() + ">.");
                e.printStackTrace();

                try {
                    ReplyFeature reply = new Reply(token, false);
                    reply.supply(e.type() + "<" + e.getDetail() + ">.").postReply(outputStream);
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
                    reply.supply("权限鉴别过程异常<未知权限:" + temp + ">.").postReply(outputStream);
                } catch (MsgException e1) {
                    System.out.println(e1.type()+"<"+e1.getDetail()+">.");
                    e1.printStackTrace();
                    break;
                }

                continue;
            }

            try {
                ReplyFeature reply = null;
                if (!server.getEmpower().privilegeCheck(request.getUuidStr(), request.getKeyString(), type)) {
                    reply = new Reply(token, false).supply("权限不足");
                    reply.postReply(outputStream);
                    continue;
                } else {
                    reply = new Reply(token, true).supply("Please Continue");
                    reply.postReply(outputStream);
                }
            } catch (MsgException e) {
                System.out.println(e.type()+"<"+e.getDetail()+">.");
                e.printStackTrace();
                break;
            }




            TaskServer processor = server.getpStack().get(request.taskMark())
                    .newEntities(request, inputStream, outputStream);
            processor.taskProcess();
        }

        try {
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Socket关闭异常+++++++++++++");
            e.printStackTrace();
        }
    }
}
