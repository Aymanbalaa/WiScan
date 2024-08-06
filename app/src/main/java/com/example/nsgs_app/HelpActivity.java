package com.example.nsgs_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HelpActivity extends AppCompatActivity {

    private ExpandableListView faqListView;
    private List<String> listDataHeader;
    private Map<String, String> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        String currentTheme = ThemeSelection.themeInitializer(findViewById(R.id.help_Layout), this, this);

        faqListView = findViewById(R.id.faqListView);

        TextView textView = findViewById(R.id.tutorial_vid_text);

        String text = "How can I get started? Click here for a video tutorial.";
        SpannableString spannableString = new SpannableString(text);

        int start = text.indexOf("Click here");
        int end = text.length();
        spannableString.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        prepareListData();

        FaqExpandableListAdapter listAdapter = new FaqExpandableListAdapter(this, listDataHeader, listDataChild);
        faqListView.setAdapter(listAdapter);

        setListViewHeightBasedOnChildren(faqListView); // Adjust height based on children

        faqListView.setOnGroupExpandListener(groupPosition -> setListViewHeightBasedOnChildren(faqListView));

        faqListView.setOnGroupCollapseListener(groupPosition -> setListViewHeightBasedOnChildren(faqListView));

        getSupportActionBar().setTitle(getString(R.string.help_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        switch (currentTheme) {
            case "Light":
            case "Clair":
            case "Свет":
                Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)));
                break;
        }
    }

    public void openTutorialVideo(View view) {
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.test);
        Intent intent = new Intent(HelpActivity.this, FullscreenVideoActivity.class);
        intent.putExtra("videoUri", videoUri);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void prepareListData() {
        listDataHeader = List.of(
                getString(R.string.faq_what_is_nsgs),
                getString(R.string.faq_start_wifi_scan),
                getString(R.string.faq_configure_wifi_settings),
                getString(R.string.faq_view_scanned_data)
        );

        listDataChild = Map.of(
                getString(R.string.faq_what_is_nsgs), getString(R.string.answer_what_is_nsgs),
                getString(R.string.faq_start_wifi_scan), getString(R.string.answer_start_wifi_scan),
                getString(R.string.faq_configure_wifi_settings), getString(R.string.answer_configure_wifi_settings),
                getString(R.string.faq_view_scanned_data), getString(R.string.answer_view_scanned_data)
        );
    }

    private void setListViewHeightBasedOnChildren(ExpandableListView listView) {
        FaqExpandableListAdapter listAdapter = (FaqExpandableListAdapter) listView.getExpandableListAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(0, 0);
            totalHeight += groupItem.getMeasuredHeight();

            if (listView.isGroupExpanded(i)) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null, listView);
                    listItem.measure(0, 0);
                    totalHeight += listItem.getMeasuredHeight();
                }
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public class FaqExpandableListAdapter extends BaseExpandableListAdapter {

        private final Context context;
        private final List<String> listDataHeader;
        private final Map<String, String> listDataChild;

        public FaqExpandableListAdapter(Context context, List<String> listDataHeader, Map<String, String> listDataChild) {
            this.context = context;
            this.listDataHeader = listDataHeader;
            this.listDataChild = listDataChild;
        }

        @Override
        public int getGroupCount() {
            return this.listDataHeader.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;  // Each group has one child (answer)
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.listDataHeader.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return this.listDataChild.get(this.listDataHeader.get(groupPosition));
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.faq_group_item, null);
            }

            TextView lblListHeader = convertView.findViewById(R.id.faq_group_title);
            lblListHeader.setText((String) getGroup(groupPosition));

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.faq_child_item, null);
            }

            TextView txtListChild = convertView.findViewById(R.id.faq_child_text);
            txtListChild.setText((String) getChild(groupPosition, childPosition));

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
