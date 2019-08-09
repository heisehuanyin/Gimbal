package softart.task;

import softart.*;

import java.io.*;

public class Transaction implements Runnable {
    private SyncWorksServer server = null;
    private String token = "";
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private TaskStartRequestFeature request = null;

    public Transaction(SyncWorksServer server, String token,
                       InputStream input, OutputStream output) {
        this.server = server;
        this.token = token;

        this.inputStream = input;
        this.outputStream = output;
    }

    @Override
    public void run() {
        boolean checkpass = false;

        // 鉴权服务
        while (true) {
            try {
                request = new TaskStartRequest(inputStream);
            } catch (MsgException e) {
                System.out.println("任务分析过程异常+++++++++++");
                System.out.println(e.type() + "<" + e.getDetail() + ">.");
                e.printStackTrace();

                try {
                    ReplyFeature reply = new Reply(token, false);
                    reply.supply(e.type() + "<" + e.getDetail() + ">.").postReply(outputStream);
                } catch (MsgException e1) {
                    System.out.println(e1.type() + "<" + e1.getDetail() + ">.");
                    e1.printStackTrace();
                }

                break;
            }


            String taskType = request.taskMark();

            try {

                ReplyFeature reply = null;
                if (!this.server.getpStack().containsKey(taskType)) {
                    reply = new Reply(token, false).supply("未知权限<" + taskType + ">.");
                    reply.postReply(outputStream);
                    continue;
                }


                if (!server.getAuthSrv().authCheck(request.getUuidStr(), request.getKeyString(), taskType)) {
                    reply = new Reply(token, false).supply("权限不足<" + taskType + ">.");
                    reply.postReply(outputStream);
                    continue;
                } else {
                    reply = new Reply(token, true).supply("Please Continue");
                    reply.postReply(outputStream);
                    checkpass = true;
                    break;
                }

            } catch (MsgException e) {
                System.out.println(e.type() + "<" + e.getDetail() + ">.");
                e.printStackTrace();
                break;
            }
        }

        // 陷入工作处理过程
        if (checkpass) {
            while (true) {
                TaskProcessor processor = server.getpStack().get(request.taskMark())
                        .newEntities(this, request, inputStream, outputStream);

                try {
                    processor.taskProcess();
                } catch (MsgException e) {
                    System.out.println(e.type() + "<" + e.getDetail() + ">.");
                    e.printStackTrace();
                    break;
                }
            }
        }

        try {
            this.closeDaemons();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            System.out.println("Socket关闭异常+++++++++++++");
            e.printStackTrace();
        }
    }

    public Thread registerDaemons(Thread daemon) {
        return server.registerDaemons(request, daemon);
    }

    private void closeDaemons() {
        server.closeDaemons(request);
    }
}
