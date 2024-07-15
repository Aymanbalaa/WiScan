package com.example.nsgs_app;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    ExpandableListView faqListView;
    List<String> listDataHeader;
    Map<String, String> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        faqListView = findViewById(R.id.faqListView);
        prepareListData();

        FaqExpandableListAdapter listAdapter = new FaqExpandableListAdapter(this, listDataHeader, listDataChild);
        faqListView.setAdapter(listAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public class FaqExpandableListAdapter extends BaseExpandableListAdapter {

        private Context context;
        private List<String> listDataHeader;
        private Map<String, String> listDataChild;

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
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_1, null);
            }

            TextView lblListHeader = convertView.findViewById(android.R.id.text1);
            lblListHeader.setText(headerTitle);
            lblListHeader.setTextSize(20);  // Increase text size for group items
            lblListHeader.setPadding(0, 20, 50, 20);  // Increase padding for group items

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final String childText = (String) getChild(groupPosition, childPosition);
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(android.R.layout.simple_expandable_list_item_2, null);
            }

            TextView txtListChild = convertView.findViewById(android.R.id.text2);
            txtListChild.setText(childText);
            txtListChild.setTextSize(18);  // Set text size for child items
            txtListChild.setPadding(0, 0, 0, 5);  // Increase padding for child items

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
