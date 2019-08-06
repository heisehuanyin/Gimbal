package softart.task;

import java.net.Socket;

public interface TaskServer {

    /**
     * 本处理任务针对的任务类型
     */
    String taskMask();


    /**
     * 新建可运行处理实体
     *
     * @param request 消息头XML的根节点
     * @param socket socket端口
     * @return 返回一个可运行实体
     */
    TaskServer newEntities(TaskRequestFeature request, Socket socket);


    /**
     * 执行任务
     */
    void taskProcess();
}
