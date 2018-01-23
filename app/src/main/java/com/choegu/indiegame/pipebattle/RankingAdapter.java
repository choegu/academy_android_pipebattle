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
        TextView tvRankingTier;
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
            holder.tvRankingTier = convertView.findViewById(R.id.text_ranking_tier);
            holder.tvRankingRating = convertView.findViewById(R.id.text_ranking_rating);

            convertView.setTag(holder);
        } else {
            holder = (RankingHolder) convertView.getTag();
        }

        MemberVO member = memberVOList.get(position);

        holder.tvRankingNum.setText(member.getMemberNum()+"");
        holder.tvRankingId.setText(member.getMemberId()+"");
        holder.tvRankingRating.setText(member.getRating()+"");
        holder.tvRankingTier.setText(member.getTier()+"");

        return convertView;
    }
}
