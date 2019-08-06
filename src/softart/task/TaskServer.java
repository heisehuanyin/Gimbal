package softart.task;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public interface TaskServer {

    /**
     * 本处理任务针对的任务类型
     */
    String taskMask();


    /**
     * 新建可运行处理实体
     *
     * @param request 消息头XML的根节点
     * @param reader socket端口
     * @param writer
     * @return 返回一个可运行实体
     */
    TaskServer newEntities(TaskRequestFeature request, BufferedReader reader, BufferedWriter writer);


    /**
     * 执行任务
     */
    void taskProcess();
}
