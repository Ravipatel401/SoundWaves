package org.bottiger.podcast;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.bottiger.podcast.activities.openopml.OPMLImportExportActivity;
import org.bottiger.podcast.activities.pastelog.LogSubmitActivity;
import org.bottiger.podcast.utils.IntUtils;
import org.bottiger.podcast.utils.SDCardManager;

import java.util.LinkedList;
import java.util.List;

import static org.bottiger.podcast.activities.openopml.OPMLImportExportActivity.OPML_ACTIVITY_STATUS_CODE;

/**
 * Created by apl on 13-02-2015.
 */
public class SoundWavesPreferenceFragment extends PreferenceFragment {

    private static final String TAG = SoundWavesPreferenceFragment.class.getSimpleName();

    private Context mContext;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        addSearchEnginePreference();

        String key = getResources().getString(R.string.pref_submit_debug_logs_key);
        Preference submitDebugLog = this.findPreference(key);
        submitDebugLog.setOnPreferenceClickListener(new SubmitDebugLogListener());
        submitDebugLog.setSummary(getVersion(getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        String saveOnSdCardKey = getResources().getString(R.string.pref_store_on_sdcard_key);
        Preference saveOnSdCardPreference = this.findPreference(saveOnSdCardKey);
        if (saveOnSdCardPreference != null && !SDCardManager.hasSdCard(mContext)) {
            PreferenceCategory category = (PreferenceCategory) findPreference("pref_network_category_storage");
            category.removePreference(saveOnSdCardPreference);
        }


        String key = getResources().getString(R.string.pref_import_opml_key);
        Preference pref = findPreference(key);
        pref.setOnPreferenceClickListener(preference -> {
            startActivityForResult(preference.getIntent(), OPML_ACTIVITY_STATUS_CODE);
            return true;
        });

        String keyExport = getResources().getString(R.string.pref_export_opml_key);
        Preference prefExport = findPreference(keyExport);
        prefExport.setOnPreferenceClickListener(preference -> {
            startActivityForResult(preference.getIntent(), OPML_ACTIVITY_STATUS_CODE);
            return true;
        });
    }

    @Override
    public void onAttach (Context argContext) {
        mContext = argContext;
        super.onAttach(argContext);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        // On Android 4.3 onAttach() is not called
        if (mContext == null) {
            mContext = getActivity();
        }

        return view;
    }

    private class SubmitDebugLogListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            final Intent intent = new Intent(getActivity(), LogSubmitActivity.class);
            startActivity(intent);
            return true;
        }
    }

    public @NonNull
    static String getVersion(@NonNull Context context) {
        String packageName = context.getApplicationContext().getPackageName();
        String version = "Unknown";
        try {
            version = context.getApplicationContext().getPackageManager().getPackageInfo(packageName, 0).versionName;
            version += "." + context.getApplicationContext().getPackageManager().getPackageInfo(packageName, 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        version += "-" + BuildConfig.FLAVOR + "-" + BuildConfig.BUILD_TYPE;

        return version;
    }

    private void addSearchEnginePreference() {
        ListPreference searchEngineListPreference = new ListPreference(getActivity());

        String listPreferenceKey = getResources().getString(R.string.pref_webservices_discovery_engine_key);
        String targetCategoryKey = getResources().getString(R.string.pref_cloud_category_cloud_services_key);
        PreferenceCategory targetCategory = (PreferenceCategory) findPreference(targetCategoryKey);

        int length = 0;

        List<Integer> ids = new LinkedList<>();
        List<Integer> res = new LinkedList<>();

        for (int i = 0; i < DiscoveryFragment.ENGINE_IDS.length; i++) {
            @DiscoveryFragment.SearchEngine int id = DiscoveryFragment.ENGINE_IDS[i];
            if (DiscoveryFragment.isEnabled(id, getActivity())) {
                ids.add(id);
                res.add(DiscoveryFragment.ENGINE_RES[i]);
            }
        }

        int[] enabled_ids = IntUtils.toIntArray(ids);
        int[] enabled_res = IntUtils.toIntArray(res);

        length = enabled_ids.length;

        CharSequence[] ids_seq = new CharSequence[length];
        CharSequence[] res_seq = new CharSequence[length];

        for (int i = 0; i < length; i++) {
            ids_seq[i] = String.valueOf(enabled_ids[i]);
            res_seq[i] = getResources().getString(enabled_res[i]);
        }

        searchEngineListPreference.setEntries(res_seq);
        searchEngineListPreference.setEntryValues(ids_seq);
        searchEngineListPreference.setDialogTitle(R.string.pref_webservices_dialog_title);
        searchEngineListPreference.setKey(listPreferenceKey);
        searchEngineListPreference.setSummary(R.string.pref_webservices_discovery_description);
        searchEngineListPreference.setTitle(R.string.pref_webservices_discovery_title);

        targetCategory.addPreference(searchEngineListPreference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        OPMLImportExportActivity.handleResult(getActivity(), requestCode, resultCode, data);
    }
}
