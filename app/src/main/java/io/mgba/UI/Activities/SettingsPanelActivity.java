package io.mgba.UI.Activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import java.util.Objects;
import io.mgba.Constants;
import io.mgba.Model.System.PermissionManager;
import io.mgba.Presenter.Interfaces.ISettingsPanelPresenter;
import io.mgba.Presenter.SettingsPanelPresenter;
import io.mgba.R;
import io.mgba.UI.Activities.Interfaces.ISettings;
import io.mgba.UI.Activities.Interfaces.ISettingsPanelView;
import io.mgba.Utils.IResourcesManager;
import io.mgba.mgba;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class SettingsPanelActivity extends AppCompatActivity implements ISettings, ISettingsPanelView {

    private static final String TAG = "Storage_Fragment";
    private ISettingsPanelPresenter controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_panel);

        String id = savedInstanceState == null
                                        ? Objects.requireNonNull(getIntent().getExtras()).getString(Constants.ARG_SETTINGS_ID)
                                        : savedInstanceState.getString(Constants.ARG_SETTINGS_ID);


        controller = new SettingsPanelPresenter(new PermissionManager(this), this,
                                                id, (IResourcesManager) getApplication());

        setupFragment();
        setupToolbar();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        controller.onSaveInstance(outState);
        super.onSaveInstanceState(outState);
    }

    private void setupToolbar() {
        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(controller.getTitle());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupFragment() {
        controller.setupFragment();
    }

    @Override
    public String requestPreferencesValue(String key, String defaultValue) {
        return controller.requestPreferencesValue(key, defaultValue);
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showFilePicker() {
        controller.showFilePicker();
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForStorage(final PermissionRequest request) {
        controller.showRationaleForStorage(request);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        controller.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void requestStoragePermission() {
        SettingsPanelActivityPermissionsDispatcher.showFilePickerWithPermissionCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        SettingsPanelActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public PreferenceFragmentCompat findFragment(String id) {
        return (PreferenceFragmentCompat) getSupportFragmentManager().findFragmentByTag(TAG + id);
    }

    @Override
    public void switchFragment(PreferenceFragmentCompat fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, fragment, tag)
                .commit();

    }

    @Override
    public String getPreference(String key, String defaultValue) {
        return ((mgba)getApplication()).getPreference(key, defaultValue);
    }
}