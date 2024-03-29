package com.akfa.apsproject.classes_serving_other_classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.akfa.apsproject.R;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

//--------ВСПОМОГАТЕЛЬНЫЙ КЛАСС ДЛЯ ЗАПОЛНЕНИЯ EXPANDABLE LIST VIEW В QUEST MAIN ACTIVITY---------//
//--------*DISCLAIMER: ЗДЕСЬ МНОГИЕ ФУНКЦИИ ПЕРЕПИСАНЫ, СТОИТ ОБРАЩАТЬ ВНИМАНИЕ НА getGroupView(), getChildView
public class ExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listGroup;
    private HashMap<String, List<String>> listItem;

    public ExpandableListViewAdapter(Context context, List<String> listGroup, HashMap<String, List<String>> listItem)
    { //конструктор адаптера
        this.context = context;
        this.listGroup = listGroup;
        this.listItem = listItem;
    }

    @SuppressLint("InflateParams")
    @Override public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
    { //инициализирует названия цехов с их стилем
        String group = (String) getGroup(groupPosition); //название цеха
        if(convertView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.list_group, null); //1-Й ПАРАМЕТР - XML ДИЗАЙН НАЗВАНИЙ ЦЕХОВ
        }
        TextView textView = convertView.findViewById(R.id.list_parent);
        textView.setText(group);
        textView.setTextColor(context.getColor(R.color.borders)); //цвет текста названия цеха
        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
    { //инициализирует названия линий с их стилем
        String child = (String) getChild(groupPosition, childPosition);
        if(convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            convertView = layoutInflater.inflate(R.layout.list_item, null); //1-й ПАРАМЕТР - XML ДИЗАЙН НАЗВАНИЙ ЛИНИЙ
        }

        TextView textView = convertView.findViewById(R.id.list_child);
        textView.setText(child);
        textView.setTextColor(Color.BLACK); //ЦВЕТ НАЗВАНИЙ ЛИНИЙ
        return convertView;
    }

    @Override public int getGroupCount()  {
        return listGroup.size(); //кол-во цехов
    }

    @Override public int getChildrenCount(int groupPosition) {
        try {
            return Objects.requireNonNull(this.listItem.get(this.listGroup.get(groupPosition))).size(); //кол-во линий в цехе
        }
        catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe);
            return 0;
        }
    }

    @Override public Object getGroup(int groupPosition) {
        return this.listGroup.get(groupPosition); //объект с названием цеха
    }

    @Nullable
    @Override public Object getChild(int groupPosition, int childPosition) {
        try {
            return Objects.requireNonNull(this.listItem.get(this.listGroup.get(groupPosition))).get(childPosition); //объект с названием линии
        } catch (NullPointerException npe) {
            ExceptionProcessing.processException(npe);
            return null;
        }

    }

    @Override public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override public boolean hasStableIds() {
        return false;
    }

    @Override public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }
}
