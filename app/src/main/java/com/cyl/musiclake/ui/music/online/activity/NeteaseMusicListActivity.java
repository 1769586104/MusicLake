package com.cyl.musiclake.ui.music.online.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cyl.musiclake.R;
import com.cyl.musiclake.api.GlideApp;
import com.cyl.musiclake.api.netease.NeteaseList;
import com.cyl.musiclake.api.netease.NeteaseMusic;
import com.cyl.musiclake.base.BaseActivity;
import com.cyl.musiclake.bean.Music;
import com.cyl.musiclake.common.Extras;
import com.cyl.musiclake.service.PlayManager;
import com.cyl.musiclake.ui.music.online.adapter.NeteaseAdapter;
import com.cyl.musiclake.ui.music.online.contract.NeteaseListContract;
import com.cyl.musiclake.ui.music.online.presenter.NeteaseListPresenter;
import com.cyl.musiclake.utils.FormatUtil;
import com.cyl.musiclake.utils.LogUtil;
import com.cyl.musiclake.utils.SizeUtils;
import com.cyl.musiclake.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 作者：yonglong on 2016/8/24 10:43
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
@SuppressWarnings("ConstantConditions")
public class NeteaseMusicListActivity extends BaseActivity implements NeteaseListContract.View {

    private static final String TAG = "BaiduMusicListActivity";
    private List<NeteaseMusic> toplist = new ArrayList<>();
    private NeteaseAdapter mAdapter;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private View mViewHeader;
    ImageView mIvBackground;
    ImageView mIvCover;
    TextView mTvTitle;
    TextView mTvDate;
    TextView mTvDesc;

    private int idx;
    private String title;
    private NeteaseList neteaseList;
    private String type;
    private String desc;
    private long time;
    private String pic;
    private NeteaseListPresenter mPresenter;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_online;
    }

    @Override
    protected void initView() {
//        idx = getIntent().getIntExtra("id", 0);
//        title = getIntent().getStringExtra("netease");
        neteaseList = (NeteaseList) getIntent().getSerializableExtra("netease");
        title = neteaseList.getName();
        desc = neteaseList.getDescription();
        pic = neteaseList.getCoverImgUrl();
        time = neteaseList.getTrackUpdateTime();
        toplist = neteaseList.getTracks();

        mToolbar.setTitle(title + "(" + neteaseList.getTrackCount() + ")");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initHeaderView();
    }


    @Override
    protected void initData() {
        mPresenter = new NeteaseListPresenter(this);
        mPresenter.attachView(this);

        mAdapter = new NeteaseAdapter(neteaseList.getTracks());
        mAdapter.setEnableLoadMore(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.bindToRecyclerView(mRecyclerView);
        showHeaderInfo();

    }

    @Override
    protected void listener() {
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.iv_more) {
                LogUtil.e(TAG, toplist.get(position).toString());
                mPresenter.playCurrentMusic(toplist.get(position));
            }
        });
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            NeteaseMusic music = toplist.get(position);
            PopupMenu popupMenu = new PopupMenu(getApplicationContext(), view);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.popup_song_detail:
                        StringBuilder sb = new StringBuilder();
                        sb.append("艺术家：")
                                .append(music.getArtists().get(0).getName())
                                .append("\n")
                                .append("专辑：")
                                .append(music.getAlbum().getName())
                                .append("\n")
                                .append("播放时长：")
                                .append(FormatUtil.formatTime(music.getDuration()))
                                .append("\n")
                                .append("播放地址：")
                                .append(music.getMp3Url());

                        new MaterialDialog.Builder(NeteaseMusicListActivity.this)
                                .title("歌曲详情")
                                .content(sb.toString())
                                .positiveText("确定")
                                .build().show();
                        break;
                    case R.id.popup_song_goto_artist:
                        Log.e(TAG, music.toString());
                        Music music1 = new Music();
                        music1.setTitle(music.getName());
                        music1.setArtist(music.getArtists().get(0).getName());
                        Intent intent = new Intent(this, ArtistInfoActivity.class);
                        intent.putExtra(Extras.TING_UID, music1);
                        startActivity(intent);
                        break;
                    case R.id.popup_song_download:
                        break;
                }
                return false;
            });
            popupMenu.inflate(R.menu.popup_song_online);
            popupMenu.show();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }


    private void initHeaderView() {
        mViewHeader = LayoutInflater.from(this).inflate(R.layout.activity_online_header, null);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(this, 150));
        mViewHeader.setLayoutParams(params);

        mIvCover = (ImageView) mViewHeader.findViewById(R.id.iv_cover);
        mTvTitle = (TextView) mViewHeader.findViewById(R.id.tv_title);
        mTvDate = (TextView) mViewHeader.findViewById(R.id.tv_update_date);
        mTvDesc = (TextView) mViewHeader.findViewById(R.id.tv_comment);

    }

    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void showErrorInfo(String msg) {
        ToastUtils.show(this, msg);
        mAdapter.loadMoreFail();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void showHeaderInfo() {
        mTvTitle.setText(title);
        mTvDate.setText(getString(R.string.recent_update, FormatUtil.distime(time)));
        mTvDesc.setText(desc);
        GlideApp.with(this)
                .load(pic)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mIvCover);
        mAdapter.setHeaderView(mViewHeader, 0);
    }


    @Override
    public void showTopList(NeteaseList musicList) {
        desc = musicList.getDescription();
        pic = musicList.getCoverImgUrl();
        time = musicList.getUpdateTime();
        showHeaderInfo();
        toplist = musicList.getTracks();
        mAdapter.setNewData(musicList.getTracks());
    }

    @Override
    public void playNeteaseMusic(Music music) {
        PlayManager.playOnline(music);
    }
}
