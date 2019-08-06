package softart;

import java.io.BufferedWriter;

public interface ReplyFeature {
    /**
     * 返回结果是否成功
     * @return 成功与否
     */
    boolean result();

    /**
     * 追加一条简短信息，返回自身
     * @param shortmsg 简短信息
     * @return 返回自身
     */
    ReplyFeature supply(String shortmsg);

    /**
     * 返回补充的信息
     * @return 补充信息
     */
    String suppliedMessage();

    /**
     * 获取本回复中的token字串
     * @return token
     */
    String token();

    /**
     * 获取本回复的字符串表示
     * @return 字符串表示
     */
    String toString();

    /**
     * 简便的答复socket目标
     * @param writer socket端口
     */
    void postReplyToClient(BufferedWriter writer) throws MsgException;
}
