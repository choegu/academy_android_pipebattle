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
import android.widget.TextView;

import com.choegu.indiegame.pipebattle.vo.MemberVO;

import java.util.List;

/**
 * Created by student on 2018-01-22.
 */

public class RankingAdapter extends ArrayAdapter<MemberVO> {
    private Activity context;
    private int layout;
    private List<MemberVO> memberVOList;

    public RankingAdapter(@NonNull Context context, int resource, @NonNull List<MemberVO> objects) {
        super(context, resource, objects);
        this.context = (Activity) context;
        this.layout = resource;
        this.memberVOList = objects;
    }

    class RankingHolder {
        TextView tvRankingNum;
        TextView tvRankingId;
        ImageView imgRankingTier;
        TextView tvRankingRating;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RankingHolder holder = null;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(layout, parent, false);

            holder = new RankingHolder();

            holder.tvRankingNum = convertView.findViewById(R.id.text_ranking_num);
            holder.tvRankingId = convertView.findViewById(R.id.text_ranking_id);
            holder.imgRankingTier = convertView.findViewById(R.id.img_ranking_tier);
            holder.tvRankingRating = convertView.findViewById(R.id.text_ranking_rating);

            convertView.setTag(holder);
        } else {
            holder = (RankingHolder) convertView.getTag();
        }

        MemberVO member = memberVOList.get(position);

        holder.tvRankingNum.setText(member.getMemberNum()+"");
        holder.tvRankingId.setText(member.getMemberId()+"");
        holder.tvRankingRating.setText(member.getRating()+"");

        if (member.getRating() < 1000) {
            holder.imgRankingTier.setImageResource(R.drawable.icon_bronze);
        } else if (member.getRating() < 1100) {
            holder.imgRankingTier.setImageResource(R.drawable.icon_silver);
        } else if (member.getRating() < 1200) {
            holder.imgRankingTier.setImageResource(R.drawable.icon_gold);
        } else if (member.getRating() < 1300) {
            holder.imgRankingTier.setImageResource(R.drawable.icon_platinum);
        } else if (member.getRating() < 1400) {
            holder.imgRankingTier.setImageResource(R.drawable.icon_diamond);
        } else if (member.getRating() < 1500) {
            holder.imgRankingTier.setImageResource(R.drawable.icon_master);
        } else {
            holder.imgRankingTier.setImageResource(R.drawable.icon_grand_master);
        }

        return convertView;
    }
}
