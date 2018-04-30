package com.apps.onlinemp3;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adapter.AdapterPlayListSongList;
import com.apps.interfaces.ClickListenerPlayList;
import com.apps.item.ItemPlayList;
import com.apps.item.ItemSong;
import com.apps.utils.Constant;
import com.apps.utils.DBHelper;
import com.apps.utils.JsonUtils;
import com.apps.utils.ZProgressHUD;
import com.google.android.gms.ads.AdListener;

import java.util.ArrayList;

public class FragmentSongByPlaylist extends Fragment {

    DBHelper dbHelper;
    RecyclerView recyclerView;
    ItemPlayList itemPlayList;
    ArrayList<ItemSong> arrayList;
    public static AdapterPlayListSongList adapterSongList;
    ZProgressHUD progressHUD;
    LinearLayoutManager linearLayoutManager;
    TextView textView_empty;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_song_by_cat, container, false);

        dbHelper = new DBHelper(getActivity());
        progressHUD = ZProgressHUD.getInstance(getActivity());
        progressHUD.setMessage(getResources().getString(R.string.loading));
        progressHUD.setSpinnerType(ZProgressHUD.FADED_ROUND_SPINNER);

        itemPlayList = (ItemPlayList) getArguments().getSerializable("playlist");
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(itemPlayList.getName());

        textView_empty = (TextView) rootView.findViewById(R.id.textView_empty_artist);

        arrayList = new ArrayList<>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_songbycat);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        arrayList.addAll(dbHelper.loadDataPlaylist(itemPlayList.getId()));
        adapterSongList = new AdapterPlayListSongList(getActivity(), arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                if (JsonUtils.isNetworkAvailable(getActivity())) {
                    showInter(position);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_not_conn), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemZero() {
                setEmpty();
            }
        });
        recyclerView.setAdapter(adapterSongList);
        setEmpty();

        return rootView;
    }

    private void setEmpty() {
        if (arrayList.size() == 0) {
            textView_empty.setVisibility(View.VISIBLE);
        } else {
            textView_empty.setVisibility(View.GONE);
        }
    }

    private void showInter(final int pos) {
        Constant.adCount = Constant.adCount + 1;
        if (Constant.adCount % Constant.adDisplay == 0) {
            ((MainActivity) getActivity()).mInterstitial.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    playIntent(pos);
                    super.onAdClosed();
                }
            });
            if (((MainActivity) getActivity()).mInterstitial.isLoaded()) {
                ((MainActivity) getActivity()).mInterstitial.show();
                ((MainActivity) getActivity()).loadInter();
            } else {
                playIntent(pos);
            }
        } else {
            playIntent(pos);
        }
    }

    private void playIntent(int position) {
        Constant.isOnline = true;
        Constant.frag = "play";
        Constant.arrayList_play.clear();
        Constant.arrayList_play.addAll(arrayList);
        Constant.playPos = position;
        ((MainActivity) getActivity()).changeText(arrayList.get(position).getMp3Name(), arrayList.get(position).getCategoryName(), position + 1, arrayList.size(), arrayList.get(position).getDuration(), arrayList.get(position).getImageBig(), arrayList.get(position).getAverageRating(), "cat");

        Constant.context = getActivity();
        Intent intent = new Intent(getActivity(), PlayerService.class);
        intent.setAction(PlayerService.ACTION_FIRST_PLAY);
        getActivity().startService(intent);
    }
}