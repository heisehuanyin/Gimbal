package softart.task.talk;

import softart.EmpowerServiceFeature;
import softart.task.TaskServer;
import softart.task.TaskRequestFeature;

import java.io.InputStream;
import java.io.OutputStream;

public class TalkServer implements TaskServer {
    private TaskRequestFeature request = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    public TalkServer(){}

    public TalkServer(TaskRequestFeature requestFeature, InputStream input, OutputStream output){
        this.request = requestFeature;
        this.inputStream = input;
        this.outputStream = output;
    }

    /**
     * 本处理任务针对的任务类型
     */
    @Override
    public String taskMask() {
        return EmpowerServiceFeature.Privileges.TalkService.toString();
    }

    /**
     * 新建可运行处理实体
     *
     * @param request 消息头XML的根节点
     * @param input  socket端口
     * @param output
     * @return 返回一个可运行实体
     */
    @Override
    public TaskServer newEntities(TaskRequestFeature request, InputStream input, OutputStream output) {
        TalkServer rtn = new TalkServer(request, input, output);
        return rtn;
    }

    /**
     * 执行任务
     */
    @Override
    public void taskProcess() {
        System.out.println("I am working.");
        System.out.println("I am done.");
    }
}
