package softart;

public class MsgException extends Exception{
    private String type="";
    private String detail="";

    public MsgException(String ex_type){
        this.type = ex_type;
    }
    public void setDetail(String detail_msg){
        this.detail = detail_msg;
    }
    public String getDetail(){
        return this.detail;
    }
    public String type(){
        return this.type;
    }
}
