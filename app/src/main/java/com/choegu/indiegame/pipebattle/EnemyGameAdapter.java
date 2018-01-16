package com.choegu.indiegame.pipebattle;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.choegu.indiegame.pipebattle.vo.TileVO;

import java.util.List;

/**
 * Created by student on 2018-01-15.
 */

public class EnemyGameAdapter extends ArrayAdapter<TileVO>{
    private Activity context;
    private int layout;
    private List<TileVO> tileVOList;

    // 게임 로직
    private final int EXPLOSION = 8;

    public EnemyGameAdapter(@NonNull Context context, int resource, @NonNull List<TileVO> objects) {
        super(context, resource, objects);
        this.context = (Activity) context;
        this.layout = resource;
        this.tileVOList = objects;
    }

    class EnemyGameHolder {
        ImageView imageTile;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        EnemyGameHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            holder = new EnemyGameHolder();

            holder.imageTile = convertView.findViewById(R.id.image_enemy);

            convertView.setTag(holder);
        } else {
            holder = (EnemyGameHolder) convertView.getTag();
        }

        TileVO tile = tileVOList.get(position);

        if (position==0 || position==48) {
            holder.imageTile.setImageResource(R.drawable.pipe_valve);
        } else {
            switch(tile.getType()) {
                case -1:
                    break;
                case 0:
                    holder.imageTile.setImageResource(R.drawable.pipe0);
                    break;
                case 1:
                    holder.imageTile.setImageResource(R.drawable.pipe1);
                    break;
                case 2:
                    holder.imageTile.setImageResource(R.drawable.pipe2);
                    break;
                case 3:
                    holder.imageTile.setImageResource(R.drawable.pipe3);
                    break;
                case 4:
                    holder.imageTile.setImageResource(R.drawable.pipe4);
                    break;
                case 5:
                    holder.imageTile.setImageResource(R.drawable.pipe5);
                    break;
                case EXPLOSION:
                    holder.imageTile.setImageResource(R.drawable.explosion);
                    break;
            }
        }

        return convertView;
    }
}
