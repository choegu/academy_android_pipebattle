package com.choegu.indiegame.pipebattle;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.choegu.indiegame.pipebattle.vo.TileVO;

import java.util.List;

/**
 * Created by student on 2018-01-15.
 */

public class AttackGameAdapter extends ArrayAdapter <TileVO>{
    private Activity context;
    private int layout;
    private List<TileVO> tileVOList;
    
    public AttackGameAdapter(@NonNull Context context, int resource, @NonNull List<TileVO> objects) {
        super(context, resource, objects);

        this.context = (Activity) context;
        this.layout = resource;
        this.tileVOList = objects;
    }

    class AttackGameHolder {

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AttackGameHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            holder = new AttackGameHolder();

            convertView.setTag(holder);
        } else {
            holder = (AttackGameHolder) convertView.getTag();
        }

        TileVO tile = tileVOList.get(position);

        return convertView;
    }
}
