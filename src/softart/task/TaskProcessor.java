package softart.task;

import softart.MsgException;

import java.io.InputStream;
import java.io.OutputStream;

public interface TaskProcessor {

    /**
     * 本处理任务针对的任务类型
     * @return 功能名称、任务类型
     */
    String taskMask();


    /**
     * 新建可运行处理实体
     *
     * @param groove
     * @param request 消息头
     * @param input 端口
     * @param output
     * @return 返回一个可运行实体
     */
    TaskProcessor newEntities(Transaction groove, TaskStartRequestFeature request, InputStream input, OutputStream output);


    /**
     * 执行任务
     */
    void taskProcess() throws MsgException;
}
