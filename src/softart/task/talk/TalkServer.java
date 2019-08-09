package softart.task.talk;

import softart.MsgException;
import softart.task.TaskProcessor;
import softart.task.TaskStartRequestFeature;
import softart.task.Transaction;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class TalkServer implements TaskProcessor {
    private static MsgRouterThread msg_service = new MsgRouterThread();

    private TaskStartRequestFeature request = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private LinkedBlockingQueue<String> msg_list = new LinkedBlockingQueue<>();


    public TalkServer() {
    }

    public TalkServer(Transaction groove, TaskStartRequestFeature requestFeature,
                      InputStream input, OutputStream output) {
        this.request = requestFeature;
        this.inputStream = input;
        this.outputStream = output;

        msg_service = (MsgRouterThread) groove.registerDaemons(msg_service);
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
     * @param groove
     * @param request 消息头XML的根节点
     * @param input   socket端口
     * @param output
     * @return 返回一个可运行实体
     */
    @Override
    public TaskProcessor newEntities(Transaction groove, TaskStartRequestFeature request,
                                     InputStream input, OutputStream output) {
        TalkServer rtn = new TalkServer(groove, request, input, output);
        return rtn;
    }

    /**
     * 执行任务
     */
    @Override
    public void taskProcess() throws MsgException {
        try {
            MsgPostRequest req = new MsgPostRequest(inputStream);

            ArrayList<String> list = req.getTargetsUser();
            for (String usr : list) {
                msg_service.postMsg(usr, req.getMessage());
            }
        } catch (MsgException e) {
            System.out.println(e.type() + "<" + e.getDetail() + ">.");
            e.printStackTrace();
            msg_service.disconnect(request.getUuidStr());

            throw e;
        }
    }
}


