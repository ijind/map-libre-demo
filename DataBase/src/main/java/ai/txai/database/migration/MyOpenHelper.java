package ai.txai.database.migration;

import android.content.Context;

import org.greenrobot.greendao.database.Database;

import ai.txai.greendao.DaoMaster;
import ai.txai.greendao.OrderDao;

/**
 * Time: 14/06/2022
 * Author Hay
 */
public class MyOpenHelper extends DaoMaster.OpenHelper {
    public MyOpenHelper(Context context, String name) {
        super(context, name);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        OrderDao.dropTable(db, true);
        OrderDao.createTable(db, false);
    }
}
