package ru.code22.mtrade;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class PrefNomenclatureGridFragment extends PreferenceFragmentCompat  {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_nomenclature_grid, rootKey);
    }

}
