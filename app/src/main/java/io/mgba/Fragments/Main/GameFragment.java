package io.mgba.Fragments.Main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.lucasr.twowayview.layout.TwoWayView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mgba.Adapters.GameAdapter;
import io.mgba.Constants;
import io.mgba.Data.DTOs.Game;
import io.mgba.Data.Platform;
import io.mgba.Model.Interfaces.ILibrary;
import io.mgba.R;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GameFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "BaseFragment";

    @BindView(R.id.no_content_container)
    protected RelativeLayout mNoContentView;

    @BindView(R.id.content_recyclerView)
    protected TwoWayView mRecyclerView;

    @BindView(R.id.no_content_image)
    protected ImageView noContentImage;

    @BindView(R.id.no_content_message)
    protected TextView noContentMessage;
    protected View mView;
    protected Platform platform;
    protected GameAdapter adapter;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        platform = (Platform) getArguments().getSerializable(Constants.ARG_PLATFORM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = prepareView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, mView);

        prepareDrawables();
        prepareRecyclerView();

        showContent(false);
        loadGames();

        return mView;
    }

    protected void prepareDrawables() {
        noContentImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_videogame_asset_grey_500_48dp));
        noContentMessage.setText(R.string.no_games);
    }

    protected void prepareRecyclerView() {
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.pink_accent_color),
                                                 getResources().getColor(R.color.colorPrimary),
                                                 getResources().getColor(R.color.green_accent_color),
                                                 getResources().getColor(R.color.yellow_accent_color),
                                                 getResources().getColor(R.color.cyan_accent_color));
        mRecyclerView.setHasFixedSize(true);
        adapter = new GameAdapter(this, getContext(), this::onClick);
        mRecyclerView.setAdapter(adapter);
    }

    protected void showContent(boolean v) {
        mNoContentView.setVisibility(v ? View.GONE : View.VISIBLE);
        mRecyclerView.setVisibility(v ? View.VISIBLE : View.GONE);
    }

    protected View prepareView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_fragment, container, false);
    }

    protected void loadGames() {
        ILibrary databaseHelper = ((io.mgba.Activities.Interfaces.ILibrary)getActivity()).getLibraryService();

        databaseHelper.prepareGames(platform)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(games -> {
                    adapter.swap(games);
                    showContent(games.size() > 0);
                });
    }

    private void onClick(Game game) {
        ((io.mgba.Activities.Interfaces.ILibrary) getActivity()).showBottomSheet(game);
    }

    @Override
    public void onRefresh() {
        ILibrary databaseHelper = ((io.mgba.Activities.Interfaces.ILibrary)getActivity()).getLibraryService();

        databaseHelper.reloadGames(platform)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(games -> {
                    adapter.swap(games);
                    mSwipeRefreshLayout.setRefreshing(false);
                });
    }
}