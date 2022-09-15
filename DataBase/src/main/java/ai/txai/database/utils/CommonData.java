package ai.txai.database.utils;

import java.util.List;

import ai.txai.database.GreenDaoHelper;
import ai.txai.database.user.User;
import ai.txai.greendao.DaoSession;
import ai.txai.greendao.UserDao;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class CommonData extends DataController {
    private static class Holder {
        private static CommonData instance = new CommonData();
    }

    private CommonData() {
        super();
    }

    @Override
    public DaoSession getDaoSession() {
        GreenDaoHelper.getInstance().waitCommonDb();
        return GreenDaoHelper.getInstance().commonDaoSession();
    }

    public static CommonData getInstance() {
        return Holder.instance;
    }

    private User user;

    public User currentUser() {
        if (user == null) {
            UserDao userDao = getDaoSession().getUserDao();
            List<User> users = userDao.loadAll();
            if (users != null && !users.isEmpty()) {
                user = users.get(0);
            }
        }
        return user;
    }

    /**
     * 验证码登录，重新保存数据
     *
     * @param user
     */
    public void whenLogin(User user) {
        this.user = user;
        UserDao userDao = getDaoSession().getUserDao();
        userDao.deleteAll();
        getDaoSession().insertOrReplace(user);
    }

    public void whenLogout() {
        this.user = null;
        UserDao userDao = getDaoSession().getUserDao();
        userDao.deleteAll();
    }
}
