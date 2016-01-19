package model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.toure.demahom.whattowatch.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Demahom on 13/12/2015.
 */
public class MoviesCategoryAdapter extends ArrayAdapter {
    public MoviesCategoryAdapter(Context context, int resource, ArrayList<AllCategoriesModel> allCategoriesModels) {
        super(context, resource, allCategoriesModels);
        this.resource = resource;
        this.allCategoriesModels = allCategoriesModels;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder = null;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = inflater.inflate(resource, null);
            mHolder.setCategory((TextView)convertView.findViewById(R.id.tvCategoryItemID));
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder)convertView.getTag();
        }
        mHolder.getCategory().setText(allCategoriesModels.get(position).getName());

        return convertView;
    }

    private int resource;
    private ArrayList<AllCategoriesModel> allCategoriesModels;
    private LayoutInflater inflater;
}
