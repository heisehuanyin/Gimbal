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

public class Reply implements ReplyFeature {
    private Document doc = null;

    public Reply(String token, boolean result) throws MsgException {
        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            MsgException ex = new MsgException("回复过程异常");
            ex.setDetail("无法建立DocumentBuilder实例");
            throw ex;
        }


        doc = builder.newDocument();

        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element tokenEntry = doc.createElement("token");
        tokenEntry.setTextContent(token);
        root.appendChild(tokenEntry);

        Element resultEntry = doc.createElement("result");
        resultEntry.setTextContent(result ? "TRUE" : "FALSE");
        root.appendChild(resultEntry);


        Element supply = doc.createElement("server-said");
        supply.setTextContent("NO_MESSAGE.");
        root.appendChild(supply);
    }

    public Reply(InputStream input) throws MsgException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                input, Charset.forName("UTF-8")
        ));

        String content = "", temp = "";
        try {
            while (!temp.equals("MSG_SPLIT")) {
                content += temp;
                temp = reader.readLine();

                if (temp == null) {
                    MsgException e = new MsgException("断开连接");
                    e.setDetail("socket连接已断开。");
                    throw e;
                }
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


        NodeList target = doc.getElementsByTagName("token");
        if (target.getLength() != 1) {
            MsgException one = new MsgException("回复参数异常");
            one.setDetail("请求不存在token或多个token参数");
            throw one;
        }

        target = doc.getElementsByTagName("result");
        if (target.getLength() != 1) {
            MsgException one = new MsgException("回复参数异常");
            one.setDetail("请求不存在result或多个result参数");
            throw one;
        }

        target = doc.getElementsByTagName("server-said");
        if (target.getLength() != 1) {
            MsgException one = new MsgException("回复参数异常");
            one.setDetail("请求不存在server-said或多个server-said参数");
            throw one;
        }

    }

    /**
     * 返回结果是否成功
     *
     * @return 成功与否
     */
    @Override
    public boolean result() {
        Element result = (Element) doc.
                getElementsByTagName("result").
                item(0);

        return result.getTextContent().equals("TRUE");
    }

    /**
     * 追加一条简短信息，返回自身
     *
     * @param shortmsg 简短信息
     * @return 返回自身
     */
    @Override
    public ReplyFeature supply(String shortmsg) {
        this.doc.getElementsByTagName("server-said")
                .item(0).setTextContent(shortmsg);

        return this;
    }

    /**
     * 返回补充的信息
     *
     * @return 补充信息
     */
    @Override
    public String suppliedMessage() {
        Element msgNode = (Element) this.doc.
                getElementsByTagName("server-said").
                item(0);

        return msgNode.getTextContent();
    }

    /**
     * 获取本回复中的token字串
     *
     * @return token
     */
    @Override
    public String token() {
        Element tokenNode = (Element) this.doc.
                getElementsByTagName("token").
                item(0);

        return tokenNode.getTextContent();
    }

    @Override
    public String toString() {
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
     * 简便的答复
     *
     * @param output 输出端口
     */
    @Override
    public void postReply(OutputStream output) throws MsgException {
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
