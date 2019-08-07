package softart;

public interface AuthServiceFeature {

    /**
     * 校对用户名和密码，通过则获取最新token，否则返回空
     * @param uuidStr 用户id
     * @param password 密码
     * @return 新token字串
     */
    String newToken(String uuidStr, String password);

    /**
     * 校对用户是否拥有某种功能的操作权限
     * @param uuidStr 用户id
     * @param token token
     * @param privilege 功能名称
     * @return 校验结果
     */
    boolean authCheck(String uuidStr, String token, String privilege);
}
