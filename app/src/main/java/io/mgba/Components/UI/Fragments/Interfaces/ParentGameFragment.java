package io.mgba.Components.UI.Fragments.Interfaces;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.mgba.Components.UI.Activities.Interfaces.ILibrary;
import io.mgba.Components.UI.Adapters.RecyclerView.LibraryAdapter;
import io.mgba.Data.DTOs.Game;
import io.mgba.R;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public abstract class ParentGameFragment extends Fragment implements ILibraryConsumer{

    @BindView(R.id.no_content_container)
    protected RelativeLayout mNoContentView;

    @BindView(R.id.content_container)
    protected RelativeLayout mMainView;

    @BindView(R.id.content_recyclerView)
    protected RecyclerView mRecyclerView;

    protected View mView;
    private LibraryAdapter adapter;
    private List<Game> games;
    private ILibrary onClickCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = prepareView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, mView);

        return mView;
    }

    protected void prepareRecyclerView(List<? extends Game> games) {
        if(games.size() == 0)
            return;

        mNoContentView.setVisibility(View.GONE);
        mMainView.setVisibility(View.VISIBLE);

        if(adapter == null) {
            FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
            layoutManager.setFlexWrap(FlexWrap.WRAP);
            layoutManager.setJustifyContent(JustifyContent.SPACE_BETWEEN);

            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new FadeInAnimator());
            mRecyclerView.setHasFixedSize(true);
            adapter = new LibraryAdapter(games, this, getContext(), this::onItemClick);
            mRecyclerView.setAdapter(adapter);
        }

        adapter.updateContent(games);
    }

    protected View prepareView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.games_fragment, container, false);
    }

    @Override
    public void consume(List<Game> list){
        if(isVisible()){
            setGameList(list);
            prepareRecyclerView(list);
        }else {

        }
    }

    @Override
    public void setLoading(){

    }

    @Override
    public void setOnClickCallback(ILibrary callback){
        onClickCallback = callback;
    }

    private Void onItemClick(Game game){
        if(onClickCallback != null)
            onClickCallback.showBottomSheet(game);

        return null;
    }

    protected void setGameList(List<Game> list){
        this.games = list;
    }
}