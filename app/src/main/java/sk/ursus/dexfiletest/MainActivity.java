package sk.ursus.dexfiletest;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		foo();
	}

	private void foo() {
		// Before the secondary dex file can be processed by the DexClassLoader,
		// it has to be first copied from asset resource to a storage location.
		File dexInternalStorageFile = writeDexFileFromAssetsToInternal();

		// Internal storage where the DexClassLoader writes the optimized dex file to
		final File optimizedDexOutputFile = getDir("outdex", Context.MODE_PRIVATE);

		DexClassLoader cl = new DexClassLoader(dexInternalStorageFile.getAbsolutePath(),
				optimizedDexOutputFile.getAbsolutePath(),
				null,
				getClassLoader());
		try {
			// Staticka metoda
			Class<?> libProviderClazz = cl.loadClass("HelloWorld");
			Method method = libProviderClazz.getMethod("main", String[].class);
			method.invoke(null, new String[1]);

			// Nestaticka metoda
			Class<?> fooBarClazz = cl.loadClass("FooBar");
			Object fooBar = fooBarClazz.getConstructor().newInstance();
			Method executeMethod = fooBarClazz.getMethod("execute");
			executeMethod.invoke(fooBar);

			// Nestaticka metoda cez interface hook
			Class<?> InjectionHookImplClazz = cl.loadClass("InjectionHookImpl");
			InjectionHook hook = (InjectionHook) InjectionHookImplClazz.newInstance();
			hook.execute();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private File writeDexFileFromAssetsToInternal() {
		final String externalDexFilename = "lib.dex";
		File dexInternalStoragePath = new File(getDir("dex", Context.MODE_PRIVATE), externalDexFilename);

		BufferedInputStream bis = null;
		OutputStream dexWriter = null;

		final int BUF_SIZE = 8 * 1024;
		try {
			bis = new BufferedInputStream(getAssets().open(externalDexFilename));
			dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
			byte[] buf = new byte[BUF_SIZE];
			int len;
			while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
				dexWriter.write(buf, 0, len);
			}
			dexWriter.close();
			bis.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return dexInternalStoragePath;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
