package name.boyle.chris.sgtpuzzles;

import android.app.backup.BackupManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.MessageFormat;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	static final String BACKEND_EXTRA = "backend";
	private BackupManager backupManager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		getDelegate().installViewFactory();
		getDelegate().onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		backupManager = new BackupManager(this);
		addPreferencesFromResource(R.xml.preferences);
		@Nullable final BackendName whichBackend = BackendName.byLowerCase(getIntent().getStringExtra(BACKEND_EXTRA));
		final PreferenceCategory chooserCategory = (PreferenceCategory) findPreference("gameChooser");
		final PreferenceCategory thisGameCategory = (PreferenceCategory) findPreference("thisGame");
		if (whichBackend == null) {
			getPreferenceScreen().removePreference(thisGameCategory);
			updateSummary((ListPreference) findPreference(GameChooser.CHOOSER_STYLE_KEY));
		} else {
			getPreferenceScreen().removePreference(chooserCategory);
			final int nameId = getResources().getIdentifier("name_" + whichBackend, "string", getPackageName());
			thisGameCategory.setTitle(nameId);
			if (whichBackend != BackendName.BRIDGES) thisGameCategory.removePreference(findPreference("bridgesShowH"));
			if (whichBackend != BackendName.UNEQUAL) thisGameCategory.removePreference(findPreference("unequalShowH"));
			final Preference unavailablePref = findPreference("arrowKeysUnavailable");
			final int capabilityId = getResources().getIdentifier(
					whichBackend + "_arrows_capable", "bool", getPackageName());
			if (capabilityId <= 0 || getResources().getBoolean(capabilityId)) {
				thisGameCategory.removePreference(unavailablePref);
				final Configuration configuration = getResources().getConfiguration();
				final CheckBoxPreference arrowKeysPref = new CheckBoxPreference(this);
				arrowKeysPref.setOrder(-1);
				arrowKeysPref.setKey(GamePlay.getArrowKeysPrefName(whichBackend, configuration));
				arrowKeysPref.setDefaultValue(GamePlay.getArrowKeysDefault(whichBackend, getResources(), getPackageName()));
				arrowKeysPref.setTitle(MessageFormat.format(getString(R.string.arrowKeysIn), getString(nameId)));
				thisGameCategory.addPreference(arrowKeysPref);
			} else {
				unavailablePref.setSummary(MessageFormat.format(getString(R.string.arrowKeysUnavailableIn), getString(nameId)));
			}
		}
		updateSummary((ListPreference) findPreference(GamePlay.ORIENTATION_KEY));
		updateSummary((ListPreference) findPreference(NightModeHelper.NIGHT_MODE_KEY));
		updateSummary((ListPreference) findPreference(GamePlay.LIMIT_DPI_KEY));
		updateSummary((ListPreference) findPreference(GamePlay.MOUSE_LONG_PRESS_KEY));
		final Preference aboutPref = findPreference("about_content");
		aboutPref.setSummary(
				String.format(getString(R.string.about_content), BuildConfig.VERSION_NAME));
		aboutPref.setOnPreferenceClickListener(preference -> {
			final ClipData data = ClipData.newPlainText(getString(R.string.version_copied_label), MessageFormat.format(getString(R.string.version_for_clipboard), BuildConfig.VERSION_NAME));
			((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(data);
			Toast.makeText(getApplicationContext(), R.string.version_copied, Toast.LENGTH_SHORT).show();
			return true;
		});
		findPreference("send_feedback").setOnPreferenceClickListener(p -> { Utils.sendFeedbackDialog(this); return true; });
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		backupManager.dataChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key)
	{
		Preference p = findPreference(key);
		if (p instanceof ListPreference) updateSummary((ListPreference)p);
	}

	private void updateSummary(ListPreference lp)
	{
		lp.setSummary(lp.getEntry());
		getListView().postInvalidate();
	}

	private AppCompatDelegate mDelegate;

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		getDelegate().onPostCreate(savedInstanceState);
	}

	private ActionBar getSupportActionBar() {
		return getDelegate().getSupportActionBar();
	}

	@NonNull
	@Override
	public MenuInflater getMenuInflater() {
		return getDelegate().getMenuInflater();
	}

	@Override
	public void setContentView(@LayoutRes int layoutResID) {
		getDelegate().setContentView(layoutResID);
	}

	@Override
	public void setContentView(View view) {
		getDelegate().setContentView(view);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		getDelegate().setContentView(view, params);
	}

	@Override
	public void addContentView(View view, ViewGroup.LayoutParams params) {
		getDelegate().addContentView(view, params);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
		getDelegate().onPostResume();
	}

	@Override
	protected void onTitleChanged(CharSequence title, int color) {
		super.onTitleChanged(title, color);
		getDelegate().setTitle(title);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getDelegate().onConfigurationChanged(newConfig);
	}

	@Override
	protected void onStop() {
		super.onStop();
		getDelegate().onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getDelegate().onDestroy();
	}

	public void invalidateOptionsMenu() {
		getDelegate().invalidateOptionsMenu();
	}

	private AppCompatDelegate getDelegate() {
		if (mDelegate == null) {
			mDelegate = AppCompatDelegate.create(this, null);
		}
		return mDelegate;
	}
}
