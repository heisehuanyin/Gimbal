package softart.task;

import softart.RequestFeature;

import java.util.ArrayList;
import java.util.HashMap;

public interface TaskRequestFeature extends RequestFeature {

    /**
     * 本处理任务针对的任务类型
     */
    String taskMark();

    void setValue(String key, String value);

    /**
     * 根据键名获取键值
     * @param key 键名
     * @return 键值
     */
    String getValue(String key);

    void setList(String key, ArrayList<String> list);

    /**
     * 根据键名获取列表
     * @param key 键名
     * @return 列表
     */
    ArrayList<String> getList(String key);

    void setDict(String name, HashMap<String,String> map);
    /**
     * 根据名称获取字典
     * @param name 字典名
     * @return 字典
     */
    HashMap<String, String> getDict(String name);
}
