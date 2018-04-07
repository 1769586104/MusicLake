package com.cyl.musiclake.ui.music.local.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cyl.musiclake.R;
import com.cyl.musiclake.bean.Music;
import com.cyl.musiclake.service.PlayManager;
import com.cyl.musiclake.ui.music.local.adapter.PlayQueueAdapter;
import com.cyl.musiclake.ui.music.local.contract.PlayQueueContract;
import com.cyl.musiclake.ui.music.local.presenter.PlayQueuePresenter;
import com.cyl.musiclake.utils.ColorUtil;
import com.cyl.musiclake.utils.SPUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hefuyi on 2016/12/27.
 */

public class PlayQueueDialog extends DialogFragment implements PlayQueueContract.View {

    @BindView(R.id.tv_play_mode)
    TextView tvPlayMode;
    @BindView(R.id.iv_play_mode)
    ImageView ivPlayMode;
    @BindView(R.id.clear_all)
    ImageView clearAll;
    @BindView(R.id.recycler_view_songs)
    RecyclerView recyclerView;
    @BindView(R.id.sheet)
    LinearLayout root;

    private PlayQueueAdapter mAdapter;
    //播放模式：0顺序播放、1随机播放、2单曲循环
    private final int PLAY_MODE_RANDOM = 0;
    private final int PLAY_MODE_LOOP = 1;
    private final int PLAY_MODE_REPEAT = 2;
    private PlayQueuePresenter mPresenter;
    private Palette.Swatch mSwatch;

    public static PlayQueueDialog newInstance() {

        Bundle args = new Bundle();

        PlayQueueDialog fragment = new PlayQueueDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new PlayQueueAdapter(null);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_playqueue, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (mSwatch != null) {
            root.setBackgroundColor(mSwatch.getRgb());
            mAdapter.setPaletteSwatch(mSwatch);
            int blackWhiteColor = ColorUtil.getBlackWhiteColor(mSwatch.getRgb());
            tvPlayMode.setTextColor(blackWhiteColor);
            ivPlayMode.setColorFilter(blackWhiteColor);
            clearAll.setColorFilter(blackWhiteColor);
        }

        mPresenter = new PlayQueuePresenter(getContext());
        mPresenter.attachView(this);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        mAdapter.bindToRecyclerView(recyclerView);

        recyclerView.scrollToPosition(PlayManager.getCurrentPosition());

        updatePlayMode();

        initListener();
        mPresenter.loadSongs();
    }

    private void initListener() {
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.iv_love && view.getId() != R.id.iv_clear) {
                PlayManager.play(position);
                mAdapter.setNewData(PlayManager.getPlayList());
            }
        });
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.iv_clear:
                    PlayManager.removeFromQueue(position);
                    mAdapter.setNewData(PlayManager.getPlayList());
                    break;
            }
        });
    }

    public void setPaletteSwatch(Palette.Swatch swatch) {
        if (swatch == null) {
            return;
        }
        mSwatch = swatch;
        if (root != null) {
            root.setBackgroundColor(mSwatch.getRgb());
            int blackWhiteColor = ColorUtil.getBlackWhiteColor(mSwatch.getRgb());
            tvPlayMode.setTextColor(blackWhiteColor);
            ivPlayMode.setColorFilter(blackWhiteColor);
            clearAll.setColorFilter(blackWhiteColor);
            mAdapter.setPaletteSwatch(mSwatch);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void dismiss() {
        getDialog().dismiss();
    }

    @OnClick(R.id.iv_play_mode)
    public void onPlayModeClick() {
        SPUtils.savePlayMode(SPUtils.getPlayMode());
        updatePlayMode();
        PlayManager.refresh();
    }

    public void updatePlayMode() {
        switch (SPUtils.getPlayMode()) {
            case PLAY_MODE_LOOP:
                ivPlayMode.setImageResource(R.drawable.ic_repeat);
                tvPlayMode.setText("循环播放");
                break;
            case PLAY_MODE_RANDOM:
                ivPlayMode.setImageResource(R.drawable.ic_shuffle);
                tvPlayMode.setText("随机播放");
                break;
            case PLAY_MODE_REPEAT:
                ivPlayMode.setImageResource(R.drawable.ic_repeat_one);
                tvPlayMode.setText("单曲播放");
                break;
        }

    }

    @OnClick(R.id.clear_all)
    public void onClearAllClick() {
        new MaterialDialog.Builder(getActivity())
                .title("清除所有?")
                .positiveText(R.string.sure)
                .negativeText(R.string.cancel)
                .onPositive((dialog, which) -> {
                    mPresenter.clearQueue();
                    mPresenter.loadSongs();
                    dismiss();
                })
                .onNegative((dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showSongs(List<Music> songs) {
        mAdapter.setNewData(songs);
    }

    @Override
    public void showEmptyView() {
        mAdapter.setNewData(null);
        mAdapter.setEmptyView(R.layout.view_queue_empty);
    }
}
