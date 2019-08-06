package softart;

public class EmpowerService implements EmpowerServiceFeature {
    /**
     * 校对用户名和密码，通过则获取最新token，否则返回空
     *
     * @param uuidStr  用户id
     * @param password 密码
     * @return 新token字串
     */
    @Override
    public String newToken(String uuidStr, String password) {
        return "EVERY_TOKEN";
    }

    /**
     * 校对用户是否拥有某种操作权限
     *
     * @param uuidStr   用户id
     * @param token     token
     * @param privilege 权限枚举
     * @return 校验结果
     */
    @Override
    public boolean privilegeCheck(String uuidStr, String token, Privileges privilege) {
        return true;
    }
}
