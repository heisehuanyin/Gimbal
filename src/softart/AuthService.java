package softart;

import softart.database.DatabasePort;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class AuthService implements AuthServiceFeature {
    private final DatabasePort port;
    private HashMap<String,String> tokenStack = new HashMap<>();

    public AuthService(String dbAddr, String dbPort, String dbName, String dbUsr, String dbPswd){
        port = new DatabasePort("jdbc:mysql://"+dbAddr+":"+dbPort+"/"+dbName, dbUsr, dbPswd);
        port.initTables();
    }

    /**
     * 校对用户名和密码，通过则获取最新token，否则返回空
     *
     * @param uuidStr  用户id
     * @param password 密码
     * @return 新token字串
     */
    @Override
    public String newToken(String uuidStr, String password) {
        String token = "";
        Statement statement = null;

        try {
            statement = port.getEmptyStatement();
        } catch (SQLException e) {
            System.out.println("申请Statement失败+++++++++++++++++++");
            e.printStackTrace();
            return token;
        }





        ResultSet rs = null;
        try {
            String exec = "select password from user_account where uuid=\'" + uuidStr + "\';";
            rs = statement.executeQuery(exec);

            if (rs.next()){
                String pswd = rs.getString("password");
                if (password.equals(pswd)){
                    token = "token-"+uuidStr;
                    this.tokenStack.put(uuidStr, token);
                }
            }

            rs.close();
            statement.close();

        } catch (SQLException e) {
            System.out.println("检索数据库失败+++++++++++++++++++++++");
            e.printStackTrace();
        }


        return token;
    }

    /**
     * 校对用户是否拥有某种功能的操作权限
     *
     * @param uuidStr   用户id
     * @param token     token
     * @param privilege 功能名称
     * @return 校验结果
     */
    @Override
    public boolean authCheck(String uuidStr, String token, String privilege) {
        return tokenStack.containsKey(uuidStr) && tokenStack.get(uuidStr).equals(token);
    }
}
