package com.example.filedetector;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;

public class First2Fragment extends Fragment implements KeyWordAdapter.ClickEventHandler {

    private ArrayList<String> fileList = new ArrayList<>();
    private static final String BUNDLE_FILE_LIST = "bundle_keyword_list";
    public static final String INTENT_FILE_LIST = "intent_keyword_list";
    public static final String INTENT_TYPE = "intent_keyword_type";
    private boolean isFileType;


    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private KeyWordAdapter fileListAdapter;

    private String curPackageName;

    Integer DELETE_APP = 12444;

    @Override
    public void onClickItemHandler(int codePosition) {
        if (isFileType) {
            File file = new File(fileList.get(codePosition));
            String path = file.getAbsolutePath();
            boolean isDeleted = false;
            if (file.exists()) {
                isDeleted = file.delete();
            }
            if (isDeleted) {
                fileList.remove(codePosition);
                fileListAdapter.notifyDataSetChanged();

                String filename = path.substring(path.lastIndexOf("/") + 1);
                Toast.makeText(getContext(), "Deleted " + filename, Toast.LENGTH_SHORT).show();
                if (fileList.size() == 0) {
                    getActivity().onBackPressed();
                }
            } else {
                Toast.makeText(getContext(), "Unable to delete file", Toast.LENGTH_SHORT).show();
            }

        } else {
            curPackageName = fileList.get(codePosition);
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + fileList.get(codePosition)));
            intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            startActivityForResult(intent, DELETE_APP);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DELETE_APP && resultCode == RESULT_OK) {
            Toast.makeText(getContext(), "Uninstall Success!", Toast.LENGTH_SHORT).show();
            for (String appPackage : fileList) {
                if (!isAppPresent(appPackage, getContext())) {
                    fileList.remove(appPackage);
                    fileListAdapter.notifyDataSetChanged();
                    if (fileList.size() == 0) {
                        getActivity().onBackPressed();
                    }
                }
            }
        }
    }

    private boolean isAppPresent(String packageName, Context context) {
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;

        } catch (PackageManager.NameNotFoundException e) {

            return false;
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first2, container, false);
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_FILE_LIST)) {
            fileList.addAll(savedInstanceState.getStringArrayList(BUNDLE_FILE_LIST));
            if (savedInstanceState.containsKey(INTENT_TYPE)) {
                isFileType = savedInstanceState.getBoolean(INTENT_TYPE);
            }
        } else {
            Intent intent = getActivity().getIntent();
            if (intent.hasExtra(INTENT_FILE_LIST)) {
                fileList = intent.getStringArrayListExtra(INTENT_FILE_LIST);
            }
            if (intent.hasExtra(INTENT_TYPE)) {
                isFileType = intent.getBooleanExtra(INTENT_TYPE, true);
            }
        }
        if (fileList != null && fileList.size() > 0) {
            recyclerView = view.findViewById(R.id.recyclerview_second);

            linearLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            fileListAdapter = new KeyWordAdapter(getContext(), this, fileList);
            recyclerView.setAdapter(fileListAdapter);
        } else {
            Toast.makeText(getContext(), "No Result Found", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (fileList.size() != 0) {
            outState.putStringArrayList(BUNDLE_FILE_LIST, fileList);
            outState.putString("PACKAGE", curPackageName);
            outState.putBoolean(INTENT_TYPE, isFileType);
        }
        super.onSaveInstanceState(outState);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(First2Fragment.this)
//                        .navigate(R.id.action_First2Fragment_to_Second2Fragment);
//            }
//        });
    }
}
