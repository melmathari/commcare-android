package org.commcare.heartbeat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Base64;

import org.commcare.CommCareApplication;
import org.commcare.util.LogTypes;
import org.commcare.utils.SerializationUtil;
import org.javarosa.core.services.Logger;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by amstone326 on 4/13/17.
 */

public class UpdateToPrompt implements Externalizable {

    private static final String KEY_CCZ_UPDATE_TO_PROMPT = "ccz-update-to-prompt";
    private static final String KEY_APK_UPDATE_TO_PROMPT = "apk-update-to-prompt";

    private static final String KEY_NUM_VIEWS_BEFORE_REDUCING_FREQ = "num-views-before-reducing-frequency";
    public static final int NUM_VIEWS_BEFORE_REDUCING_FREQ_DEFAULT_VALUE = 3;

    // Both of these settings will be int values N that represent the directive "Show a given
    // update prompt to the user every N logins"
    private static final String KEY_REGULAR_SHOW_FREQ = "regular-show-frequency";
    private static final String KEY_REDUCED_SHOW_FREQ = "reduced-show-frequency";
    private static final int REGULAR_SHOW_FREQ_DEFAULT_VALUE = 1;
    public static final int REDUCED_SHOW_FREQ_DEFAULT_VALUE = 4;


    private String versionString;
    private int cczVersion;
    private ApkVersion apkVersion;
    private boolean isForced;
    private Type updateType;

    private int numTimesSeen;
    private UpdatePromptShowHistory showHistory;

    public enum Type {
        APK_UPDATE(KEY_APK_UPDATE_TO_PROMPT),
        CCZ_UPDATE(KEY_CCZ_UPDATE_TO_PROMPT);

        private String prefsKey;

        Type(String s) {
            this.prefsKey = s;
        }

        protected String getPrefsKey() {
            return this.prefsKey;
        }

    }

    public UpdateToPrompt(String version, String forceString, Type type) {
        if (forceString != null) {
            this.isForced = "true".equals(forceString);
        }
        this.updateType = type;
        this.versionString = version;
        this.showHistory = new UpdatePromptShowHistory();
        buildFromVersionString();
    }

    public UpdateToPrompt() {
        // for deserialization
    }

    private void buildFromVersionString() {
        if (this.updateType == Type.APK_UPDATE) {
            this.apkVersion = new ApkVersion(versionString);
        } else {
            this.cczVersion = Integer.parseInt(versionString);
        }
    }

    public void registerWithSystem() {
        if (isNewerThanCurrentVersion()) {
            printDebugStatement();
            writeToPrefsObject(CommCareApplication.instance().getCurrentApp().getAppPreferences());
        } else {
            // If the latest signal we're getting is that our current version is up-to-date,
            // then we should wipe any update prompt for this type that was previously stored
            UpdatePromptHelper.wipeStoredUpdate(this.updateType);
        }
    }

    private void printDebugStatement() {
        if (this.updateType == Type.APK_UPDATE) {
            System.out.println(".apk version to prompt for update set to " + apkVersion);
        } else {
            System.out.println(".ccz version to prompt for update set to " + cczVersion);
        }
        System.out.println("Is forced?: " + isForced);
    }

    public boolean isForced() {
        return isForced;
    }

    public boolean isNewerThanCurrentVersion() {
        if (this.updateType == Type.APK_UPDATE) {
            try {
                Context c = CommCareApplication.instance();
                PackageInfo pi = c.getPackageManager().getPackageInfo(c.getPackageName(), 0);
                ApkVersion currentVersion = new ApkVersion(pi.versionName);
                return currentVersion.compareTo(this.apkVersion) < 0;
            } catch (PackageManager.NameNotFoundException e) {
                // This shouldn't happen, but it if it does, there's no way for us to know if the
                // update version is newer, so don't prompt
                Logger.log(LogTypes.TYPE_ERROR_WORKFLOW,
                        "Couldn't get current .apk version to compare with in UpdateToPrompt: "
                                + e.getMessage());
                return false;
            }
        } else {
            int currentVersion = CommCareApplication.instance().getCommCarePlatform().getCurrentProfile().getVersion();
            return currentVersion < this.cczVersion;
        }
    }

    private void writeToPrefsObject(SharedPreferences prefs) {
        try {
            byte[] serializedBytes = SerializationUtil.serialize(this);
            String serializedString = Base64.encodeToString(serializedBytes, Base64.DEFAULT);
            prefs.edit().putString(this.updateType.getPrefsKey(), serializedString).commit();
        } catch (Exception e) {
            Logger.log(LogTypes.TYPE_ERROR_WORKFLOW,
                    "Error encountered while serializing UpdateToPrompt: " + e.getMessage());
        }
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        this.versionString = ExtUtil.readString(in);
        this.updateType = Type.valueOf(ExtUtil.readString(in));
        this.isForced = ExtUtil.readBool(in);
        this.numTimesSeen = ExtUtil.readInt(in);
        this.showHistory = (UpdatePromptShowHistory)ExtUtil.read(in, UpdatePromptShowHistory.class, pf);
        buildFromVersionString();
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeString(out, versionString);
        ExtUtil.writeString(out, updateType.name());
        ExtUtil.writeBool(out, isForced);
        ExtUtil.writeNumeric(out, numTimesSeen);
        ExtUtil.write(out, showHistory);
    }

    public int getCczVersion() {
        return cczVersion;
    }

    public void incrementTimesSeen() {
        numTimesSeen++;
    }

    public boolean shouldShowOnThisLogin() {
        int showFrequency = useRegularFrequency() ? getRegularShowFrequency() : getReducedShowFrequency();
        return showHistory.shouldShowOnThisLogin(showFrequency);
    }

    private boolean useRegularFrequency() {
        int viewsThresholdForRegularFrequency =
                CommCareApplication.instance().getCurrentApp().getAppPreferences()
                        .getInt(KEY_NUM_VIEWS_BEFORE_REDUCING_FREQ,
                                NUM_VIEWS_BEFORE_REDUCING_FREQ_DEFAULT_VALUE);
        return numTimesSeen < viewsThresholdForRegularFrequency;
    }

    private static int getRegularShowFrequency() {
        return CommCareApplication.instance().getCurrentApp().getAppPreferences()
                .getInt(KEY_REGULAR_SHOW_FREQ, REGULAR_SHOW_FREQ_DEFAULT_VALUE);
    }

    static int getReducedShowFrequency() {
        return CommCareApplication.instance().getCurrentApp().getAppPreferences()
                .getInt(KEY_REDUCED_SHOW_FREQ, REDUCED_SHOW_FREQ_DEFAULT_VALUE);
    }

}
