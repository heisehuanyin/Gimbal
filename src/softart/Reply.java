package softart;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class Reply implements ReplyFeature {
    private boolean isSuccess = false;
    private String token = "";
    private Document doc = null;
    private String supplyMessage = "NO_SUPPLY";

    public Reply(String token, boolean result) throws MsgException {
        this.isSuccess = result;
        this.token = token;

        DocumentBuilder builder = null;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            MsgException ex = new MsgException("回复过程异常");
            ex.setDetail("无法建立DocumentBuilder实例");
            throw ex;
        }


        doc = builder.newDocument();

        Element root = doc.createElement("root");
        doc.appendChild(root);

        Element tokenEntry = doc.createElement("token");
        tokenEntry.setTextContent(this.token);
        root.appendChild(tokenEntry);

        Element resultEntry = doc.createElement("result");
        resultEntry.setTextContent(isSuccess?"TRUE":"FALSE");
        root.appendChild(resultEntry);


        Element supply = doc.createElement("server-said");
        supply.setTextContent(this.supplyMessage);
        root.appendChild(supply);
    }

    /**
     * 返回结果是否成功
     *
     * @return 成功与否
     */
    @Override
    public boolean result() {
        return isSuccess;
    }

    /**
     * 追加一条简短信息，返回自身
     *
     * @param shortmsg 简短信息
     * @return 返回自身
     */
    @Override
    public ReplyFeature supply(String shortmsg) {
        this.supplyMessage = shortmsg;
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
        return this.supplyMessage;
    }

    /**
     * 获取本回复中的token字串
     *
     * @return token
     */
    @Override
    public String token() {
        return this.token;
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
     * 简便的答复socket目标
     *
     * @param socket socket端口
     */
    @Override
    public void replyToSocket(Socket socket) throws MsgException {
        OutputStream out = null;
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            MsgException ex = new MsgException("回复过程异常");
            ex.setDetail("无法通过Socket建立OutputStream.");
            throw ex;
        }
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, Charset.forName("UTF-8")));
        try {
            writer.write(this.toString());
            writer.newLine();
            writer.write("MSG_SPLIT");
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException e) {
            MsgException ex = new MsgException("回复过程异常");
            ex.setDetail("回复内容写入Socket的过程异常");
            throw ex;
        }
    }
}
