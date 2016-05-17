package org.commcare.preferences;

import android.os.Bundle;
import android.preference.PreferenceManager;

import org.commcare.CommCareApplication;
import org.commcare.activities.SessionAwarePreferenceActivity;
import org.commcare.dalvik.R;
import org.commcare.logging.analytics.GoogleAnalyticsFields;
import org.commcare.logging.analytics.GoogleAnalyticsUtils;
import org.javarosa.core.services.locale.Localization;

import java.util.HashMap;
import java.util.Map;

/**
 * Sub-menu for managing server addresses
 * @author Phillip Mates (pmates@dimagi.com)
 */
public class CommCareServerPreferences
        extends SessionAwarePreferenceActivity {

    public final static String PREFS_APP_SERVER_KEY = "default_app_server";
    public final static String PREFS_DATA_SERVER_KEY = "ota-restore-url";
    public final static String PREFS_SUBMISSION_URL_KEY = "PostURL";
    private final static String PREFS_KEY_SERVER_KEY = "default_key_server";
    public final static String PREFS_FORM_RECORD_KEY = "form-record-url";

    private static final Map<String, String> prefKeyToAnalyticsEvent = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName((CommCareApplication._().getCurrentApp().getPreferencesFilename()));
        addPreferencesFromResource(R.xml.server_preferences);

        GoogleAnalyticsUtils.reportPrefActivityEntry(GoogleAnalyticsFields.CATEGORY_CC_PREFS);

        setTitle(Localization.get("settings.server.title"));
        CommCarePreferences.addBackButtonToActionBar(this);

        populatePrefKeyToEventLabelMapping();
        GoogleAnalyticsUtils.createPreferenceOnClickListeners(prefMgr, prefKeyToAnalyticsEvent,
                GoogleAnalyticsFields.CATEGORY_CC_PREFS);
    }

    private static void populatePrefKeyToEventLabelMapping() {
        prefKeyToAnalyticsEvent.put(PREFS_APP_SERVER_KEY, GoogleAnalyticsFields.LABEL_APP_SERVER);
        prefKeyToAnalyticsEvent.put(PREFS_DATA_SERVER_KEY, GoogleAnalyticsFields.LABEL_DATA_SERVER);
        prefKeyToAnalyticsEvent.put(PREFS_SUBMISSION_URL_KEY, GoogleAnalyticsFields.LABEL_SUBMISSION_SERVER);
        prefKeyToAnalyticsEvent.put(PREFS_KEY_SERVER_KEY, GoogleAnalyticsFields.LABEL_KEY_SERVER);
        prefKeyToAnalyticsEvent.put(PREFS_FORM_RECORD_KEY, GoogleAnalyticsFields.LABEL_FORM_RECORD_SERVER);
    }
}
