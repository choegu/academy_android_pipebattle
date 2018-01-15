package com.choegu.indiegame.pipebattle;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.choegu.indiegame.pipebattle.vo.RoomVO;

import java.util.List;

/**
 * Created by student on 2018-01-10.
 */

public class RoomAdapter extends ArrayAdapter<RoomVO>{
    private Activity context;
    private int layout;
    private List<RoomVO> roomVOList;

    public RoomAdapter(@NonNull Context context, int resource, @NonNull List<RoomVO> objects) {
        super(context, resource, objects);
        this.context = (Activity) context;
        this.layout = resource;
        this.roomVOList = objects;
    }

    class RoomHolder {
        TextView tvRoomNum;
        TextView tvTitle;
        TextView tvCreateId;
        TextView tvPlayerNum;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RoomHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            holder = new RoomHolder();

            holder.tvRoomNum = convertView.findViewById(R.id.room_num);
            holder.tvTitle = convertView.findViewById(R.id.room_title);
            holder.tvCreateId = convertView.findViewById(R.id.room_creator);
            holder.tvPlayerNum = convertView.findViewById(R.id.room_player_num);

            convertView.setTag(holder);
        } else {
            holder = (RoomHolder) convertView.getTag();
        }

        RoomVO room = roomVOList.get(position);

        holder.tvRoomNum.setText(room.getRoomNum()+"");
        holder.tvTitle.setText(room.getTitle());
        holder.tvCreateId.setText(room.getCreateId());
        holder.tvPlayerNum.setText(room.getPlayerNum()+"");

        return convertView;
    }
}
