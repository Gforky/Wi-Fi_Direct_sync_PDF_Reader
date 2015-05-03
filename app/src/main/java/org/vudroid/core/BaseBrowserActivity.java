package org.vudroid.core;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TabHost;

import org.vudroid.R;
import org.vudroid.core.presentation.BrowserAdapter;
//import org.vudroid.core.presentation.UriBrowserAdapter;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

public class BaseBrowserActivity extends Activity
{
    private BrowserAdapter adapter;
    private static final String CURRENT_DIRECTORY = "currentDirectory";
    private String host;
    private Boolean sORc;
    
    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
		@SuppressWarnings({"unchecked"})
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            final File file = ((AdapterView<BrowserAdapter>)adapterView).getAdapter().getItem(i);
            if (file.isDirectory())
            {
                setCurrentDir(file);
            }
            else
            {
                showDocument(file);
            }
        }
    };
    //private UriBrowserAdapter recentAdapter;
    protected final FileFilter filter;

    public BaseBrowserActivity()
    {
        this.filter = createFileFilter();
    }

    protected FileFilter createFileFilter()
    {
        return new FileFilter()
        {
            @Override
			public boolean accept(File pathname)
            {
            	if (pathname.getName().endsWith("." + "pdf")) return true; //filer the files with the name corresponding to the key "pdf", "djvu" and "djv"
                return pathname.isDirectory();
            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Bundle bundle = getIntent().getExtras();
        host = bundle.getString("host");
        sORc = bundle.getBoolean("sORc");
        System.out.println("THIS IS !!!!!!!!!!!!!!!!"+host);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        final ListView browseList = initBrowserListView();
        TabHost tabHost = (TabHost) findViewById(R.id.browserTabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("Browse").setIndicator("Browse").setContent(new TabHost.TabContentFactory()
        {
            @Override
			public View createTabContent(String s)
            {
                return browseList;
            }
        }));
    
        final File sdcardPath = new File(Environment.getExternalStorageDirectory().getPath());
        if (sdcardPath.exists())
        {
            setCurrentDir(sdcardPath);
        }
        else
        {
            setCurrentDir(new File("/"));
        }
        if (savedInstanceState != null)
        {
            final String absolutePath = savedInstanceState.getString(CURRENT_DIRECTORY);
            if (absolutePath != null)
            {
                setCurrentDir(new File(absolutePath));
            }
        }
    }

/******************************************************************************************/
    //onPostCreate is for framework use, no need here
    /*protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        final File sdcardPath = new File(Environment.getExternalStorageDirectory().getPath());
        if (sdcardPath.exists())
        {
            setCurrentDir(sdcardPath);
        }
        else
        {
            setCurrentDir(new File("/"));
        }
        if (savedInstanceState != null)
        {
            final String absolutePath = savedInstanceState.getString(CURRENT_DIRECTORY);
            if (absolutePath != null)
            {
                setCurrentDir(new File(absolutePath));
            }
        }
    }*/
/*********************************************************************************************/
    private ListView initBrowserListView()
    {
        final ListView listView = new ListView(this);
        adapter = new BrowserAdapter(this, filter);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener); //the ItemClickListener which will firstly check the type of the item
        //set the height and the width of the listView
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return listView;
    }

    private void showDocument(File file)
    {
        showDocument(Uri.fromFile(file)); //view file with the uri from a file.
    }

    protected void showDocument(Uri uri)
    {	
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Bundle bundle = new Bundle();
        bundle.putString("host", host);
        bundle.putBoolean("sORc", sORc);
        intent.putExtras(bundle);
        intent.setClass(this, BaseViewerActivity.class);
        startActivity(intent);//start to view the file
    }
/* change the path displayed in the on the title*/
    private void setCurrentDir(File newDir)
    {
        adapter.setCurrentDirectory(newDir);
        getWindow().setTitle(newDir.getAbsolutePath());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_DIRECTORY, adapter.getCurrentDirectory().getAbsolutePath());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }
}
