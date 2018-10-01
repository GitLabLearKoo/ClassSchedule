package com.mnnyang.gzuclassschedule.utils.spec;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mnnyang.gzuclassschedule.R;
import com.mnnyang.gzuclassschedule.app.Constant;
import com.mnnyang.gzuclassschedule.custom.WheelView;
import com.mnnyang.gzuclassschedule.utils.DialogHelper;
import com.mnnyang.gzuclassschedule.utils.DialogListener;
import com.mnnyang.gzuclassschedule.utils.LogUtil;
import com.mnnyang.gzuclassschedule.utils.Preferences;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示详细信息
 * Created by mnnyang on 17-11-7.
 */

public class PopupWindowDialog implements View.OnClickListener {
    private int mWeek;
    private int mNodeStart;
    private int mNodeEnd;

    private List<View> mAllWeekTextView;
    private View mBtnWeekAll;
    private View mBtnWeekSingle;
    private View mBtnWeekDouble;
    private static final Integer MAX_WEEK = 25;


    public interface SelectTimeCallback {
        void onSelected(String location, List<Integer> selectWeeks, int week, int nodeStart, int nodeEnd);
    }

    public void showSelectTimeDialog(Activity activity, String location, final List<Integer> selectWeeks, final int week, int nodeStart,
                                     int nodeEnd, final SelectTimeCallback callback) {
        mWeek = week;
        mNodeStart = nodeStart;
        mNodeEnd = nodeEnd;

        DialogHelper helper = new DialogHelper();
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_select_week_and_node, null);

        initWeekSelect(view, selectWeeks);

        final ArrayList<String> weeks = new ArrayList<>();
        final ArrayList<String> nodes = new ArrayList<>();

        WheelView wvWeek = (WheelView) view.findViewById(R.id.wv_week);
        WheelView wvStart = (WheelView) view.findViewById(R.id.wv_start_node);
        final WheelView wvEnd = (WheelView) view.findViewById(R.id.wv_end_node);

        final TextInputEditText etLocation = view.findViewById(R.id.et_location);

        if (location != null) {
            etLocation.setText(location);
        }

        for (int i = 1; i <= 7; i++) {
            weeks.add(Constant.WEEK[i]);
        }

        int maxNode = 16;
        for (int i = 1; i <= maxNode; i++) {
            nodes.add("第" + i + "节");
        }

        wvWeek.setItems(weeks);
        wvStart.setItems(nodes);
        wvEnd.setItems(nodes);

        wvWeek.setSeletion(mWeek - 1);
        wvStart.setSeletion(mNodeStart - 1);
        wvEnd.setSeletion(mNodeEnd - 1);

        wvWeek.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                mWeek = selectedIndex;
            }
        });

        wvStart.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                mNodeStart = selectedIndex;
                if (mNodeStart > mNodeEnd) {
                    wvEnd.setSeletion(mNodeStart - 1);
                    return;
                }
            }
        });

        wvEnd.setOnWheelViewListener(new WheelView.OnWheelViewListener() {
            @Override
            public void onSelected(int selectedIndex, String item) {
                mNodeEnd = selectedIndex;

                if (mNodeStart > mNodeEnd) {
                    wvEnd.setSeletion(mNodeStart - 1);
                }
            }
        });

        helper.showCustomDialog(activity, view, null, new DialogListener() {
            @Override
            public void onPositive(DialogInterface dialog, int which) {
                super.onPositive(dialog, which);
                dialog.dismiss();

                List<Integer> selectWeeks = new ArrayList<>();

                for (int i = 0; i < mAllWeekTextView.size(); i++) {
                    if (mAllWeekTextView.get(i).isSelected()) {
                        selectWeeks.add(i + 1);
                    }
                }

                callback.onSelected(etLocation.getText().toString().trim(), selectWeeks,
                        mWeek, mNodeStart, mNodeEnd);
            }
        });
    }


    private void initWeekSelect(View view, List<Integer> selectWeeks) {
        View.OnClickListener weekTextViewCLickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
            }
        };


        mBtnWeekAll = view.findViewById(R.id.btn_week_all);
        mBtnWeekSingle = view.findViewById(R.id.btn_week_single);
        mBtnWeekDouble = view.findViewById(R.id.btn_week_double);
        ViewGroup layoutWeekContainer = view.findViewById(R.id.layout_week_container);

        mBtnWeekAll.setSelected(true);
        mBtnWeekAll.setOnClickListener(this);
        mBtnWeekSingle.setOnClickListener(this);
        mBtnWeekDouble.setOnClickListener(this);


        mAllWeekTextView = getAllChildViews(layoutWeekContainer);

        if (selectWeeks == null) {
            for (View view1 : mAllWeekTextView) {
                view1.setSelected(true);
                view1.setOnClickListener(weekTextViewCLickListener);
            }
        } else {
            for (View view1 : mAllWeekTextView) {
                view1.setOnClickListener(weekTextViewCLickListener);
            }

            for (Integer selectWeek : selectWeeks) {
                if (selectWeek <= MAX_WEEK && selectWeek >= 1) {
                    mAllWeekTextView.get(selectWeek - 1).setSelected(true);
                } else {
                    LogUtil.e(this, "周数超出范围");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_week_all:
                mBtnWeekAll.setSelected(true);
                mBtnWeekSingle.setSelected(false);
                mBtnWeekDouble.setSelected(false);

                for (View view : mAllWeekTextView) {
                    view.setSelected(true);
                }
                break;
            case R.id.btn_week_single:
                mBtnWeekAll.setSelected(false);
                mBtnWeekSingle.setSelected(true);
                mBtnWeekDouble.setSelected(false);

                for (int i = 0; i < mAllWeekTextView.size(); i++) {
                    mAllWeekTextView.get(i).setSelected(i % 2 == 0);
                }
                break;
            case R.id.btn_week_double:
                mBtnWeekAll.setSelected(false);
                mBtnWeekSingle.setSelected(false);
                mBtnWeekDouble.setSelected(true);

                for (int i = 0; i < mAllWeekTextView.size(); i++) {
                    mAllWeekTextView.get(i).setSelected(i % 2 == 1);
                }
                break;
        }
    }

    private List<View> getAllChildViews(View view) {
        List<View> allchildren = new ArrayList<View>();
        if (view instanceof ViewGroup) {
            ViewGroup vp = (ViewGroup) view;
            for (int i = 0; i < vp.getChildCount(); i++) {
                View viewchild = vp.getChildAt(i);
                //allchildren.add(viewchild);
                if (!(viewchild instanceof ViewGroup)) {
                    allchildren.add(viewchild);
                }
                //再次 调用本身（递归）
                allchildren.addAll(getAllChildViews(viewchild));
            }
        }
        return allchildren;
    }
}
