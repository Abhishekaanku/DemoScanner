package com.example.filedetector;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class FirstFragment extends Fragment implements KeyWordAdapter.ClickEventHandler {
    private ArrayList<String> keywordList = new ArrayList<>();
    private static final String BUNDLE_KEYWORD_LIST = "bundle_keyword_list";
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    KeyWordAdapter keyWordAdapter;
    FloatingActionButton fab;
    ProgressBar progressBar;
    Button appScanButton;
    Button fileScanButton;


    @Override
    public void onClickItemHandler(int moviePosition) {
        keywordList.remove(moviePosition);
        if (keywordList.size() == 0) {
            keywordList.add("NO KEYWORD");
        }
        keyWordAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEYWORD_LIST)) {
            keywordList.addAll(savedInstanceState.getStringArrayList(BUNDLE_KEYWORD_LIST));
        }

        appScanButton = view.findViewById(R.id.button_first);
        fileScanButton = view.findViewById(R.id.button_third);


        progressBar = view.findViewById(R.id.scan_progress);
        recyclerView = view.findViewById(R.id.recyclerview_first);

        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        keyWordAdapter = new KeyWordAdapter(getContext(), this, keywordList);
        recyclerView.setAdapter(keyWordAdapter);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View promptView = LayoutInflater.from(getContext()).inflate(R.layout.prompt_dialog, null);
                final EditText editText = promptView.findViewById(R.id.edit_keyword_query);
                new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setView(promptView)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String text = editText.getText().toString();
                                if (text.length() != 0) {
                                    if (keywordList.size() > 0 && keywordList.get(0).equals("NO KEYWORD")) {
                                        keywordList.remove(0);
                                        keywordList.add(text);
                                        keyWordAdapter.notifyDataSetChanged();
                                    } else {
                                        keywordList.add(text);
                                    }
                                    Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .create()
                        .show();
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (keywordList.size() != 0) {
            outState.putStringArrayList(BUNDLE_KEYWORD_LIST, keywordList);
        }
        super.onSaveInstanceState(outState);
    }

    private ArrayList<String> listApplications() {
        final PackageManager pm = getActivity().getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        ArrayList<String> appList = new ArrayList<>();

        for (ApplicationInfo packageInfo : packages) {
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null &&

                    !pm.getLaunchIntentForPackage(packageInfo.packageName).equals("")) {
                String appName = packageInfo.packageName;
                for (String keyword : keywordList) {
                    if (appName.toLowerCase().contains(keyword.toLowerCase())) {
                        appList.add(packageInfo.packageName);
                    }
                }

//                System.out.println("Launch Intent For Package :" +
//                        pm.getLaunchIntentForPackage(packageInfo.packageName));
//
//                System.out.println("Application Label :" + pm.getApplicationLabel(packageInfo));

            }
        }
        return appList;
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        appScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keywordList.get(0).equals("NO KEYWORD")) {
                    Toast.makeText(getContext(), "Add At Least One Keyword", Toast.LENGTH_LONG).show();
                } else {
                    new ScanFileAsyncTask().execute(Pair.create(Environment.getExternalStorageDirectory(), false));
                }
            }
        });

        fileScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (keywordList.get(0).equals("NO KEYWORD")) {
                    Toast.makeText(getContext(), "Add At Least One Keyword", Toast.LENGTH_LONG).show();
                } else {
                    new ScanFileAsyncTask().execute(Pair.create(Environment.getExternalStorageDirectory(), true));
                    ;
                }
            }
        });
    }

    private void populateAllRequiredFiles(File file, List<String> filteredFile) {
        File[] inFiles = file.listFiles();
        if (inFiles != null) {

            for (File inFile : file.listFiles()) {
                if (inFile.isDirectory()) {
                    populateAllRequiredFiles(inFile, filteredFile);
                } else {
                    String type = null;
                    String extension = MimeTypeMap.getFileExtensionFromUrl(inFile.getAbsolutePath());
                    if (extension != null) {
                        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                    }
                    if (type == null) {
                        extension = inFile.getAbsolutePath().substring(inFile.getAbsolutePath().lastIndexOf(".") + 1).trim();
                        if (extension.equalsIgnoreCase("txt")) {
                            type = "text/plain";
                        } else if (extension.equalsIgnoreCase("pdf")) {
                            type = "application/pdf";
                        }
                    }
                    Log.d("FILE", "isFilterable: " + type);
                    if (isFilterable(inFile, type)) {
                        filteredFile.add(inFile.getAbsolutePath());
                    }
                }
            }
        }
    }

//    private void populateAllRequiredFiles(File file, List<String> filteredFile) {
//        filteredFile.addAll(Arrays.asList("TTTTTTT", "PPPPPPPP"));
//    }

    private boolean isFilterable(File file, String type) {
        if (type != null) {
            if (type.equalsIgnoreCase("text/plain")) {
                return isFilterableTxt(file);
            }
//            else if (type.equalsIgnoreCase("application/pdf")) {
//                return isFilterablePdf(file);
//            }
        }
        return false;
    }

    private boolean isFilterablePdf(File file) {
        try {
            PdfReader reader = new PdfReader(file.getAbsolutePath());
            int n = reader.getNumberOfPages();
            StringBuilder extractedText = new StringBuilder();

            for (int i = 0; i < n; i++) {
                extractedText.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim());// to extract the PDF content from the different pages
            }

            String content = extractedText.toString();
            Log.d("FILExtractor", content);
            for (String keyword : keywordList) {
                if (content.contains(keyword)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean isFilterableTxt(File file) {
        if (!file.isDirectory()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    for (String keyword : keywordList) {
                        if (line.contains(keyword)) {
                            return true;
                        }
                    }
                }
            } catch (IOException e) {
                Toast.makeText(getContext(), "Error while reading file", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        return false;
    }

    class ScanFileAsyncTask extends AsyncTask<Pair<File, Boolean>, Void, Pair<ArrayList<String>, Boolean>> {
        @Override
        protected void onPreExecute() {
            Toast.makeText(getContext(), "Started Scanning", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            appScanButton.setEnabled(false);
            fileScanButton.setEnabled(false);

            super.onPreExecute();
        }

        @Override
        protected Pair<ArrayList<String>, Boolean> doInBackground(Pair<File, Boolean>... request) {
            ArrayList<String> resultList = new ArrayList<>(1);
            if (request[0].second) {
                populateAllRequiredFiles(request[0].first, resultList);
            } else {
                resultList = listApplications();
            }
            return Pair.create(resultList, request[0].second);
        }

        @Override
        protected void onPostExecute(Pair<ArrayList<String>, Boolean> fetchedData) {
            progressBar.setVisibility(View.INVISIBLE);
            appScanButton.setEnabled(true);
            fileScanButton.setEnabled(true);
            Toast.makeText(getContext(), "Done Scanning", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), FileDetailActivity.class);
            intent.putStringArrayListExtra(First2Fragment.INTENT_FILE_LIST, fetchedData.first);
            intent.putExtra(First2Fragment.INTENT_TYPE, fetchedData.second);
            startActivity(intent);
            super.onPostExecute(fetchedData);
        }
    }
}
