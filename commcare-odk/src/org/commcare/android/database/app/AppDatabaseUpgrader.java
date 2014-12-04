/**
 * 
 */
package org.commcare.android.database.app;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import net.sqlcipher.database.SQLiteDatabase;

import org.commcare.android.database.ConcreteDbHelper;
import org.commcare.android.database.DbHelper;
import org.commcare.android.database.SqlStorage;
import org.commcare.android.database.TableBuilder;
import org.commcare.android.database.app.models.ResourceModelUpdater;
import org.commcare.android.database.user.models.FormRecord;
import org.commcare.resources.model.Resource;
import org.commcare.resources.model.ResourceInstaller;
import org.commcare.resources.model.ResourceLocation;
import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import android.content.Context;

/**
 * @author ctsims
 *
 */
public class AppDatabaseUpgrader {
    private Context c;
    
    public AppDatabaseUpgrader(Context c) {
        this.c = c;
    }

    public void upgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1) {
            if(upgradeOneTwo(db, oldVersion, newVersion)) {
                oldVersion = 2;
            }
        }
        if(oldVersion == 2) {
            if(upgradeTwoThree(db)) {
                oldVersion = 3;
            }
        }
        if(oldVersion == 3) {
            if(upgradeThreeFour(db)) {
                oldVersion = 4;
            }
        }
        //NOTE: If metadata changes are made to the Resource model, they need to be
        //managed by changing the TwoThree updater to maintain that metadata.
    }
    
    private boolean upgradeTwoThree(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            TableBuilder builder = new TableBuilder("RECOVERY_RESOURCE_TABLE");
            builder.addData(new Resource());
            db.execSQL(builder.getTableCreateString());
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private boolean upgradeOneTwo(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.beginTransaction();
        try {
            TableBuilder builder = new TableBuilder("RECOVERY_RESOURCE_TABLE");
            builder.addData(new Resource());
            db.execSQL(builder.getTableCreateString());
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private  boolean upgradeThreeFour(SQLiteDatabase db) {
        
        DbHelper helper = new ConcreteDbHelper(c,db);
        
        db.beginTransaction();
        try {
            db.execSQL("CREATE INDEX global_index_id ON GLOBAL_RESOURCE_TABLE ( " + Resource.META_INDEX_PARENT_GUID + " )");
            db.execSQL("CREATE INDEX upgrade_index_id ON UPGRADE_RESOURCE_TABLE ( " + Resource.META_INDEX_PARENT_GUID + " )");
            db.execSQL("CREATE INDEX recovery_index_id ON RECOVERY_RESOURCE_TABLE ( " + Resource.META_INDEX_PARENT_GUID + " )");
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }
    
    /**
     * Reads and rewrites all of the records in a table, generally to adapt an old serialization format to a new
     * format
     *  
     * @param db
     * @param storage
     * @return
     */
    private <T extends Persistable> void updateModels(SqlStorage<T> storage) {
        for(T t : storage) {
            storage.write(t);
        }
    }
}
