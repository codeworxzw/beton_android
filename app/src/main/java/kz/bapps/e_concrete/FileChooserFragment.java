package kz.bapps.e_concrete;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FileChooserFragment extends DialogFragment{
    private OnFileSelectedListener mCallback;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Create the AlertDialog object and return it
        final FileAdapter adapter = new FileAdapter(getActivity(), new ArrayList<ListEntry>());
        adapter.getFiles();
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing here to prevent dismiss after click
            }
        };
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setAdapter(adapter, clickListener)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        builder.setTitle("Выберите файл ЭЦП");
        final AlertDialog theDialog = builder.show();
        theDialog.getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path;
                SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (adapter.getItem(position).name.equals("..")){
                    //navigate back
                    path = options.getString("edspath", "/");
                    path=path.substring(0, path.length());
                    path=path.substring(0,path.lastIndexOf("/"));
                    path = !path.equals("")?path:("/");
                }else {
                    //get the Slashes right and navigate forward
                    path = options.getString("edspath", "");
                    path += ((path.equals("/"))?(""):("/"))+adapter.getItem(position).name;
                }
                Editor editor = options.edit();
                File dirTest = new File(path);
                if (dirTest.isDirectory()){
                    editor.putString("edspath", path);
                    editor.commit();
                    adapter.clear();
                    adapter.getFiles();
                }else{
                    mCallback.onFileSelected(path);
                    theDialog.dismiss();
                }

            }
        });
        return theDialog;
    }

    private boolean isBaseDir(String dir) {
        File folder = new File(dir);
        if (!folder.exists()){
            folder = new File("/");
            if (!folder.exists()){
                Log.wtf("FileBrowser","Something's really fishy");
            }
        }
        File baseDir = new File("/");
        if (folder.equals(baseDir)){
            return true;
        }else{
            return false;
        }
    }

    // Container Activity must implement this interface
    public interface OnFileSelectedListener {
        public void onFileSelected(String file);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnFileSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    class ListEntry {
        public String name;
        public Drawable item ;

        public ListEntry(String name, Drawable item) {
            this.name = name;
            this.item = item;
        }
    }

    class FileAdapter extends ArrayAdapter<ListEntry>{

        //show only files with the suffix FILE_SUFFIX, use "*" to show all files;
        private static final String FILE_SUFFIX = ".p12";

        public FileAdapter(Context context, ArrayList<ListEntry> fileEntry) {
            super(context, R.layout.filechooser_list_item,fileEntry);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            ListEntry entry = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.filechooser_list_item, parent, false);
            }
            // Lookup view for data population
            TextView filechooserEntry = (TextView) convertView.findViewById(R.id.filechooser_entry);
            // Populate the data into the template view using the data object
            filechooserEntry.setText(entry.name);
            filechooserEntry.setCompoundDrawablesWithIntrinsicBounds(entry.item, null, null, null);
            // Return the completed view to render on screen
            return convertView;
        }

        private FileAdapter getFiles() {
            SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getActivity());
            ArrayList<File> files = getFilesInDir(options.getString("edspath", ""));

            if (!isBaseDir(options.getString("edspath", ""))){
                this.add(new ListEntry("..", getResources().getDrawable( R.drawable.ic_folder_open_24dp)));
            }

            for (File file : files){
                if (file.isDirectory()){
                    this.add(new ListEntry(file.getName(),getResources().getDrawable(R.drawable.ic_folder_open_24dp)));
                }else{
                    if (file.getName().endsWith(FILE_SUFFIX)||FILE_SUFFIX.equals("*")){
                        this.add(new ListEntry(file.getName(),getResources().getDrawable(R.drawable.ic_insert_drive_file_24dp)));
                    }
                }
            }
            return this;
        }

        private ArrayList<File> getFilesInDir(String dir) {
            File folder = new File(dir);
            if (!folder.exists()){
                folder = new File("/");
                if (!folder.exists()){
                    Log.wtf("FileBrowser","Something's really fishy");
                }
            }
            ArrayList<File> fileList;
            if (folder.listFiles()!=null){
                fileList = new ArrayList<File>(Arrays.asList(folder.listFiles()));
            }else{
                fileList = new ArrayList<File>();
            }
            return fileList;
        }
    }
}