package softart.task.talk;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import softart.EmpowerServiceFeature;
import softart.MsgException;
import softart.task.TaskRequest;

import java.io.InputStream;
import java.util.ArrayList;

public class TalkRequest extends TaskRequest {
    private ArrayList<String> post2 = new ArrayList<>();

    public TalkRequest(String uuid, String token) throws MsgException {
        super(uuid,token, EmpowerServiceFeature.Privileges.TalkService.toString());

        Document doc = this.getDoc();
        Element usrList = doc.createElement("usr-list");
        doc.getDocumentElement().appendChild(usrList);
    }

    public void appendUser(String uuid){
        if (post2.contains(uuid))
            return;

        post2.add(uuid);

        NodeList nodeList = getDoc().getElementsByTagName("usr-list");
        Element elm = (Element) nodeList.item(0);

        Element usr = getDoc().createElement("user");
        usr.setAttribute("userid", uuid);
        elm.appendChild(usr);
    }

    /**
     * 载入Socket中的内容，根据提交信息生成请求，用于服务器端解析
     *
     * @param input socket端口
     * @throws MsgException 异常
     */
    public TalkRequest(InputStream input) throws MsgException {
        super(input);
        this.post2.clear();



        NodeList nodeList = getDoc().getElementsByTagName("usr-list");
        if (nodeList.getLength()!=1){
            MsgException one = new MsgException("参数错误");
            one.setDetail("用户清单节点不存在或多于一个");
            throw one;
        }
        Node one = nodeList.item(0);


        NodeList childs = one.getChildNodes();
        for (int i=0; i<childs.getLength(); ++i){
            Element elm = (Element) childs.item(i);
            post2.add(elm.getAttribute("userid"));
        }
    }

}
