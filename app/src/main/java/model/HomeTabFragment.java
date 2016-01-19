package model;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;

import com.toure.demahom.whattowatch.R;

/**
 * Created by Demahom on 10/12/2015.
 */
public class HomeTabFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_tabbed,container,false); // The returned object is a View instance
    }
}
