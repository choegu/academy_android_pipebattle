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

import com.choegu.indiegame.pipebattle.vo.OptionValue;
import com.choegu.indiegame.pipebattle.vo.TileVO;

import java.util.List;

/**
 * Created by student on 2018-01-15.
 */

public class MainGameAdapter extends ArrayAdapter<TileVO>{
    private Activity context;
    private int layout;
    private List<TileVO> tileVOList;

    // 방 입장 task
    private final String CREATE = "create";
    private final String ENTER = "enter";

    // 게임 로직
    private final int EXPLOSION = 8;
    private final int FIRST_VALVE = 6;
    private final int END_VALVE = 7;

    public MainGameAdapter(@NonNull Context context, int resource, @NonNull List<TileVO> objects) {
        super(context, resource, objects);
        this.context = (Activity) context;
        this.layout = resource;
        this.tileVOList = objects;
    }

    class MainGameHolder {
        ImageView imageTile;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        MainGameHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            holder = new MainGameHolder();

            holder.imageTile = convertView.findViewById(R.id.image_main);

            convertView.setTag(holder);
        } else {
            holder = (MainGameHolder) convertView.getTag();
        }

        TileVO tile = tileVOList.get(position);

        switch(tile.getType()) {
            case -1:
                break;
            case 0:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe0);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe0);
                }
                break;
            case 1:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe1);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe1);
                }
                break;
            case 2:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe2);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe2);
                }
                break;
            case 3:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe3);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe3);
                }
                break;
            case 4:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe4);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe4);
                }
                break;
            case 5:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe5);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe5);
                }
                break;
            case FIRST_VALVE:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe_valve1);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe_valve1);
                }
                break;
            case END_VALVE:
                if (OptionValue.task.equals(CREATE)) {
                    holder.imageTile.setImageResource(R.drawable.angel_pipe_valve2);
                } else if (OptionValue.task.equals(ENTER)) {
                    holder.imageTile.setImageResource(R.drawable.devil_pipe_valve2);
                }
                break;
            case EXPLOSION:
                holder.imageTile.setImageResource(R.drawable.explosion);
                break;
        }

        return convertView;
    }
}
