package softart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.Charset;

public class Request implements RequestFeature {
    protected Document doc = null;

    /**
     * 新建一个任务请求，一般用于客户端对服务器端实用
     * @param uuid 识别id
     * @param _pwd_token 密码或token
     */
    public Request(String uuid, String _pwd_token) throws MsgException {
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            MsgException one = new MsgException("请求建立过程异常");
            one.setDetail("无法建立新请求");
            throw one;
        }





        this.doc = builder.newDocument();

        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element usrid = doc.createElement("uuid");
        usrid.setTextContent(uuid);
        root.appendChild(usrid);

        Element keynode = doc.createElement("pwd-token");
        keynode.setTextContent(_pwd_token);
        root.appendChild(keynode);
    }


    /**
     * 载入Socket中的内容，根据提交信息生成请求，用于服务器端解析
     * @param input socket端口
     * @throws MsgException 异常
     */
    public Request(InputStream input) throws MsgException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                input, Charset.forName("UTF-8")
        ));

        String content = "", temp = "";
        try {
            while (!temp.equals("MSG_SPLIT")) {
                content += temp;
                temp = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            MsgException one = new MsgException("请求获取过程异常");
            one.setDetail("读取提交的请求数据过程中出现io错误");
            throw one;
        }


        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            MsgException one = new MsgException("请求解析过程异常");
            one.setDetail("无法建立解析请求用DocumentBuilder");
            throw one;
        }





        try {
            doc = builder.parse(new ByteArrayInputStream(content.getBytes("UTF-8")));
        } catch (SAXException e) {
            e.printStackTrace();
            MsgException one = new MsgException("请求解析过程异常");
            one.setDetail("请求格式错误或其他");
            throw one;
        } catch (IOException e) {
            e.printStackTrace();
            MsgException one = new MsgException("请求解析过程异常");
            one.setDetail("接收到的请求编码错误或其他");
            throw one;
        }



        Element rootelm = doc.getDocumentElement();
        NodeList nodes = rootelm.getElementsByTagName("uuid");
        if (nodes.getLength()!=1){
            MsgException one = new MsgException("请求参数异常");
            one.setDetail("请求不存在uuid或多个uuid参数");
            throw one;
        }

        nodes = rootelm.getElementsByTagName("pwd-token");
        if (nodes.getLength()!=1){
            MsgException one = new MsgException("请求参数异常");
            one.setDetail("请求不存在pwd-token或多个pwd-token参数");
            throw one;
        }
    }

    @Override
    public String getUuidStr(){
        Element uuid = (Element) doc.
                getElementsByTagName("uuid").
                item(0);

        return uuid.getTextContent();
    }

    @Override
    public String getKeyString(){
        Element keyString = (Element) doc.
                getElementsByTagName("pwd-token").
                item(0);

        return keyString.getTextContent();
    }

    @Override
    public String toString(){
        String result = null;
        TransformerFactory tfac = TransformerFactory.newInstance();
        StringWriter strWtr = new StringWriter();
        StreamResult strResult = new StreamResult(strWtr);

        try {
            javax.xml.transform.Transformer t = tfac.newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.transform(new DOMSource(doc.getDocumentElement()), strResult);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("XML.toString(Document): " + e);
        }
        result = strResult.getWriter().toString();
        try {
            strWtr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 后代通过本接口获取document dom 模型
     * @return dom模型
     */
    @Override
    public Document getDoc(){
        return this.doc;
    }

    /**
     * 向socket端口发送request
     *
     * @param output 端口
     */
    @Override
    public void postRequest(OutputStream output) throws MsgException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                output, Charset.forName("UTF-8")
        ));

        try {
            writer.write(this.toString());
            writer.newLine();
            writer.write("MSG_SPLIT");
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            MsgException ex = new MsgException("回复过程异常");
            ex.setDetail("回复内容写入Socket的过程异常");
            throw ex;
        }
    }
}
