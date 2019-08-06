package softart.task;

import softart.Request;
import softart.MsgException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TaskRequest extends Request implements TaskRequestFeature {
    private String taskMask = "";

    public TaskRequest(String uuid, String token, String taskMask) throws MsgException {
        super(uuid, token);
        this.taskMask = taskMask;

        Document doc = this.getDoc();
        Element taskmask = doc.createElement("task-mask");
        taskmask.setTextContent(taskMask);

        doc.getDocumentElement().appendChild(taskmask);
    }

    /**
     * 载入内容，根据提交信息生成请求，用于服务器端解析
     *
     * @param input 读取端口
     * @throws MsgException 异常
     */
    public TaskRequest(InputStream input) throws MsgException {
        super(input);

        Document doc = this.getDoc();
        NodeList taskMasks = doc.getElementsByTagName("task-mask");
        if (taskMasks.getLength() != 1){
            MsgException ex = new MsgException("参数错误");
            ex.setDetail("不存在task-mask节点或存在多个task-mask节点");
            throw ex;
        }

        this.taskMask = taskMasks.item(0).getTextContent();
    }

    /**
     * 本处理任务针对的任务类型
     */
    @Override
    public String taskMark() {
        return this.taskMask;
    }


    public void setValue(String key, String value){
        Element root = getDoc().getDocumentElement();
        NodeList args = root.getElementsByTagName("args");

        for (int i=0; i<args.getLength(); ++i){
            Element one = (Element) args.item(i);
            if(one.getAttribute("key").equals(key)){
                one.setAttribute("value", value);
                return;
            }
        }

        Element arg = getDoc().createElement("args");
        arg.setAttribute("key",key);
        arg.setAttribute("value",value);

        root.appendChild(arg);
    }

    /**
     * 根据键名获取键值
     *
     * @param key 键名
     * @return 键值
     */
    @Override
    public String getValue(String key) {
        NodeList args = getDoc().getDocumentElement().getElementsByTagName("args");

        for (int i=0; i<args.getLength(); ++i){
            Element one = (Element) args.item(i);
            if(one.getAttribute("key").equals(key)){
                return one.getAttribute("value");
            }
        }

        return "";
    }

    @Override
    public void setList(String key, ArrayList<String> strlist) {
        Element root = getDoc().getDocumentElement();
        NodeList llist=root.getElementsByTagName("list");

        for(int i=0; i<llist.getLength(); ++i){
            Element one = (Element) llist.item(i);
            if (one.getAttribute("key").equals(key)){
                root.removeChild(one);
            }
        }

        Element one = getDoc().createElement("list");
        one.setAttribute("key", key);
        root.appendChild(one);

        for (String item : strlist){
            Element ins = getDoc().createElement("item");
            ins.setAttribute("value", item);
            one.appendChild(ins);
        }
    }

    /**
     * 根据键名获取列表
     *
     * @param key 键名
     * @return 列表
     */
    @Override
    public ArrayList<String> getList(String key) {
        ArrayList<String> list = new ArrayList<>();
        Element root = getDoc().getDocumentElement();
        NodeList llist=root.getElementsByTagName("list");

        for(int i=0; i<llist.getLength(); ++i){
            Element one = (Element) llist.item(i);
            if (one.getAttribute("key").equals(key)){
                NodeList items = one.getElementsByTagName("item");
                for (int c=0; c<items.getLength(); ++c){
                    Element item_elm = (Element) items.item(c);
                    list.add(item_elm.getAttribute("value"));
                }
                break;
            }
        }

        return list;
    }

    @Override
    public void setDict(String name, HashMap<String, String> map) {
        Element root = getDoc().getDocumentElement();
        NodeList dictlist = root.getElementsByTagName("dict");

        for (int i=0; i<dictlist.getLength(); ++i){
            Element one = (Element) dictlist.item(i);
            if (one.getAttribute("key").equals(name)){
                root.removeChild(one);
            }
        }

        Element one = getDoc().createElement("dict");
        one.setAttribute("key", name);
        root.appendChild(one);

        Set<String> keys = map.keySet();
        for (String item : keys){
            Element ins = getDoc().createElement("pair");
            ins.setAttribute("key", item);
            ins.setAttribute("value", map.get(item));
            one.appendChild(ins);
        }
    }

    /**
     * 根据名称获取字典
     *
     * @param name 字典名
     * @return 字典
     */
    @Override
    public HashMap<String, String> getDict(String name) {
        HashMap<String, String> map = new HashMap<>();
        Element root = getDoc().getDocumentElement();
        NodeList dictlist = root.getElementsByTagName("dict");

        for (int i=0; i<dictlist.getLength(); ++i){
            Element one = (Element) dictlist.item(i);
            if (one.getAttribute("key").equals(name)){
                NodeList pairs = one.getElementsByTagName("pair");
                for (int jjj=0; jjj<pairs.getLength(); ++jjj){
                    Element pair = (Element) pairs.item(jjj);
                    map.put(pair.getAttribute("key"),
                            pair.getAttribute("value"));
                }
            }
        }

        return map;
    }

    public static void main(String[] args){
        try {
            TaskRequest o = new TaskRequest("uuid","token", "mask");

            System.out.println(o.taskMark());
            System.out.println(o.toString());

            o.setValue("a-key","a-value");
            System.out.println("======");
            System.out.println(o.toString());

            o.setValue("a-key","b-value");
            System.out.println("======");
            System.out.println(o.toString());

            o.setValue("some-key","some-value");
            System.out.println("======");
            System.out.println(o.toString());

            ArrayList<String> lista = new ArrayList<>();
            lista.add("instance_a");
            lista.add("instance_b");
            o.setList("list_example", lista);
            System.out.println("===========");
            System.out.println(o.toString());

            HashMap<String,String> map = new HashMap<>();
            map.put("hash1","hash2");
            map.put("map1","map2");
            o.setDict("adict",map);
            System.out.println("===========");
            System.out.println(o.toString());

        } catch (MsgException e) {
            e.printStackTrace();
        }


    }

}
