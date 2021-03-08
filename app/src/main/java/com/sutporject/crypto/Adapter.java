package com.sutporject.crypto;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import com.bumptech.glide.RequestBuilder;

import java.util.ArrayList;


public class Adapter extends BaseAdapter {

    Context context;
    private final ArrayList<String> symbols;
    private final ArrayList<String> names;
    private final ArrayList<String> prices;
    private final ArrayList<String> oneHour;
    private final ArrayList<String> oneDay;
    private final ArrayList<String> oneWeek;
    private final ArrayList<RequestBuilder> images;

    public Adapter(Context context, ArrayList<String> symbols, ArrayList<String> names, ArrayList<RequestBuilder> images, ArrayList<String> prices,
                   ArrayList<String> oneHour, ArrayList<String> oneDay, ArrayList<String> oneWeek){
        //super(context, R.layout.single_list_app_item, utilsArrayList);
        this.context = context;
        this.symbols = symbols;
        this.names = names;
        this.images = images;
        this.prices = prices;
        this.oneHour = oneHour;
        this.oneDay = oneDay;
        this.oneWeek = oneWeek;
    }

    public void addItems(ArrayList<String> symbols, ArrayList<String> names, ArrayList<RequestBuilder> images, ArrayList<String> prices,
                         ArrayList<String> oneHour, ArrayList<String> oneDay, ArrayList<String> oneWeek){
        this.symbols.addAll(symbols);
        this.names.addAll(names);
        this.images.addAll(images);
        this.prices.addAll(prices);
        this.oneHour.addAll(oneHour);
        this.oneDay.addAll(oneDay);
        this.oneWeek.addAll(oneWeek);
    }

    @Override
    public int getCount() {
        return symbols.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.singlerow, parent, false);
            viewHolder.txtSymbol = (TextView) convertView.findViewById(R.id.symbolTxt);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.nameTxt);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.appIconIV);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);
            viewHolder.hour = (TextView) convertView.findViewById(R.id.one);
            viewHolder.day = (TextView) convertView.findViewById(R.id.twofour);
            viewHolder.week = (TextView) convertView.findViewById(R.id.week);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.txtName.setText("|" +names.get(position));
        viewHolder.txtSymbol.setText(symbols.get(position));
        viewHolder.price.setText(prices.get(position));
        viewHolder.hour.setText(oneHour.get(position));
        if(Double.parseDouble(oneHour.get(position)) < 0)
            viewHolder.hour.setTextColor(Color.parseColor("#E34523"));

        viewHolder.day.setText(oneDay.get(position));
        if(Double.parseDouble(oneDay.get(position)) < 0)
            viewHolder.day.setTextColor(Color.parseColor("#E34523"));

        viewHolder.week.setText(oneWeek.get(position));
        if(Double.parseDouble(oneWeek.get(position)) < 0)
            viewHolder.week.setTextColor(Color.parseColor("#E34523"));

        images.get(position).into(viewHolder.icon);

        return convertView;
    }

    private static class ViewHolder {

        TextView txtSymbol;
        TextView txtName;
        TextView price;
        TextView hour;
        TextView day;
        TextView week;
        ImageView icon;

    }

}
