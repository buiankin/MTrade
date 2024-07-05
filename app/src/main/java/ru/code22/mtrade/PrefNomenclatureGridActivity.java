package ru.code22.mtrade;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class PrefNomenclatureGridActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MySingleton g=MySingleton.getInstance();
		g.checkInitByDataAndSetTheme(this);
		super.onCreate(savedInstanceState);
		// Можно было бы и не использовать совсем, но нам нужен ActionBar (для красоты)
		setContentView(R.layout.default_for_fragment);

		getSupportFragmentManager()
				.beginTransaction()
				//.replace(android.R.id.content, new PrefNomenclatureGridFragment())
				.replace(R.id.content, new PrefNomenclatureGridFragment())
				.commit();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	}

}
