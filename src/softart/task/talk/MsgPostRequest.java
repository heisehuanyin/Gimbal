package softart.task.talk;

import softart.MsgException;
import softart.task.TaskStartRequest;

import java.io.InputStream;
import java.util.ArrayList;

public class MsgPostRequest extends TaskStartRequest {
    public MsgPostRequest(String uuid, String token) throws MsgException {
        super(uuid, token, MsgPostRequest.class.getSimpleName());
    }
    public MsgPostRequest(InputStream input) throws MsgException {
        super(input);
    }

    public void appendTargetUser(String uuid){
        ArrayList<String> list = getList("post-target-usrs");
        list.add(uuid);
        setList("post-target-usrs", list);
    }
    public ArrayList<String> getTargetsUser(){
        return getList("post-target-usrs");
    }

    public void setMessage(String msg){
        setValue("post-msg-item", msg);
    }

    public String getMessage(){
        return getValue("post-msg-item");
    }
}
