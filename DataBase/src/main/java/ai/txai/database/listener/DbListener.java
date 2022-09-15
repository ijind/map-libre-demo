package ai.txai.database.listener;

import ai.txai.database.user.User;

/**
 * Time: 12/05/2022
 * Author Hay
 */
public interface DbListener {
    void userDBInit(User user);
}
