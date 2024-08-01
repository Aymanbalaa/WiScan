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
        ThemeSelection.themeInitializer(findViewById(R.id.help_Layout), this);

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
