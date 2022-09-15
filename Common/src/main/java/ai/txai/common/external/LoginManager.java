package ai.txai.common.external;

import java.util.ArrayList;
import java.util.List;

import ai.txai.database.user.User;

/**
 * Time: 15/03/2022
 * Author Hay
 */
public class LoginManager implements LoginListener {
    private final List<LoginListener> sLoginListener = new ArrayList<>();

    @Override
    public void whenLogin(User user, boolean dbUserInit) {
        for (LoginListener loginListener : sLoginListener) {
            loginListener.whenLogin(user, dbUserInit);
        }
    }

    @Override
    public void whenLogout() {
        for (LoginListener loginListener : sLoginListener) {
            loginListener.whenLogout();
        }
    }

    private static class Holder {
        private static LoginManager instance = new LoginManager();
    }

    private LoginManager() {
    }

    public static LoginManager getInstance() {
        return Holder.instance;
    }


    public void registerLoginListener(LoginListener listener) {
        if (sLoginListener.contains(listener)) {
            return;
        }
        sLoginListener.add(listener);
    }

    public void unregisterLoginListener(LoginListener listener) {
        sLoginListener.remove(listener);
    }
}
