package ai.txai.common.external;

import ai.txai.database.user.User;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public interface LoginListener {
    /**
     * @param user
     * @param  dbUserInit -> 用户数据库初始化完成， false-> 输入账号验证码登录
     */
    void whenLogin(User user, boolean dbUserInit);

    void whenLogout();
}
