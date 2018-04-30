package com.apps.onlinemp3;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.adapter.AdapterArtistLatest;
import com.apps.adapter.AdapterRecent;
import com.apps.item.ItemArtist;
import com.apps.item.ItemSong;
import com.apps.utils.Constant;
import com.apps.utils.DBHelper;
import com.apps.utils.JsonUtils;
import com.apps.utils.RecyclerItemClickListener;
import com.apps.utils.ZProgressHUD;
import com.google.android.gms.ads.AdListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FragmentHome extends Fragment {

    DBHelper dbHelper;
    RecyclerView recyclerView, recyclerView_artist;
    ArrayList<ItemSong> arrayList, arrayList_recent;
    ArrayList<ItemArtist> arrayList_artist;
    AdapterRecent adapterRecent;
    AdapterArtistLatest adapterArtistLatest;
    ZProgressHUD progressHUD;
    LinearLayoutManager linearLayoutManager, llm_artist;
    public ViewPager viewpager;
    ImagePagerAdapter adapter;
    TextView textView_empty, textView_empty_artist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);

        dbHelper = new DBHelper(getActivity());

        progressHUD = ZProgressHUD.getInstance(getActivity());
        progressHUD.setMessage(getActivity().getResources().getString(R.string.loading));
        progressHUD.setSpinnerType(ZProgressHUD.FADED_ROUND_SPINNER);

        textView_empty = rootView.findViewById(R.id.textView_recent_empty);
        textView_empty_artist = rootView.findViewById(R.id.textView_artist_empty);

        adapter = new ImagePagerAdapter();
        viewpager = rootView.findViewById(R.id.viewPager_home);
        viewpager.setPadding(40, 10, 40, 10);
        viewpager.setClipToPadding(false);
        viewpager.setPageMargin(20);
        viewpager.setClipChildren(false);

        arrayList = new ArrayList<>();
        arrayList_recent = new ArrayList<>();
        arrayList_artist = new ArrayList<>();

        recyclerView = rootView.findViewById(R.id.recyclerView_home_recent);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        recyclerView_artist = rootView.findViewById(R.id.recyclerView_home_artist);
        llm_artist = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView_artist.setLayoutManager(llm_artist);
        recyclerView_artist.setItemAnimator(new DefaultItemAnimator());
        recyclerView_artist.setHasFixedSize(true);

        if (JsonUtils.isNetworkAvailable(getActivity())) {
            new LoadLatestNews().execute(Constant.URL_LATEST);
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.internet_not_conn), Toast.LENGTH_SHORT).show();
        }

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (JsonUtils.isNetworkAvailable(getActivity())) {
                    Constant.isOnline = true;
                    Constant.arrayList_play.clear();
                    Constant.arrayList_play.addAll(arrayList_recent);
                    Constant.playPos = position;
                    ((MainActivity) getActivity()).changeText(arrayList_recent.get(position).getMp3Name(), arrayList_recent.get(position).getCategoryName(), position + 1, arrayList_recent.size(), arrayList_recent.get(position).getDuration(), arrayList_recent.get(position).getImageBig(), arrayList_recent.get(position).getAverageRating(), "home");

                    Constant.context = getActivity();
                    if (position == 0) {
                        Intent intent = new Intent(getActivity(), PlayerService.class);
                        intent.setAction(PlayerService.ACTION_FIRST_PLAY);
                        getActivity().startService(intent);
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_not_conn), Toast.LENGTH_SHORT).show();
                }
            }
        }));

        recyclerView_artist.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (JsonUtils.isNetworkAvailable(getActivity())) {
                    FragmentManager fm = getFragmentManager();
                    FragmentSongByArtist f1 = new FragmentSongByArtist();
                    FragmentTransaction ft = fm.beginTransaction();

                    Bundle bundl = new Bundle();
                    bundl.putString("artist", arrayList_artist.get(position).getName());
                    bundl.putString("image", arrayList_artist.get(position).getImage());
                    f1.setArguments(bundl);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.hide(getFragmentManager().findFragmentByTag(getResources().getString(R.string.home)));
                    ft.add(R.id.fragment, f1, arrayList_artist.get(position).getName());
                    ft.addToBackStack(arrayList_artist.get(position).getName());
                    ft.commit();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.internet_not_conn), Toast.LENGTH_SHORT).show();
                }
            }
        }));

        return rootView;
    }

    private class LoadLatestNews extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressHUD.show();
            arrayList.clear();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String json = JsonUtils.getJSONString(strings[0]);

                JSONObject mainJson = new JSONObject(json);
                JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);
                JSONObject objJson = null;
                for (int i = 0; i < jsonArray.length(); i++) {
                    objJson = jsonArray.getJSONObject(i);

                    String id = objJson.getString(Constant.TAG_ID);
                    String cid = objJson.getString(Constant.TAG_CAT_ID);
                    String cname = objJson.getString(Constant.TAG_CAT_NAME);
                    String artist = objJson.getString(Constant.TAG_ARTIST);
                    String name = objJson.getString(Constant.TAG_SONG_NAME);
                    String url = objJson.getString(Constant.TAG_MP3_URL);
                    String desc = objJson.getString(Constant.TAG_DESC);
                    String duration = objJson.getString(Constant.TAG_DURATION);
                    String total_rate = objJson.getString(Constant.TAG_TOTAL_RATE);
                    String avg_rate = objJson.getString(Constant.TAG_AVG_RATE);
                    String image = objJson.getString(Constant.TAG_THUMB_B).replace(" ", "%20");
                    String image_small = objJson.getString(Constant.TAG_THUMB_S).replace(" ", "%20");

                    ItemSong objItem = new ItemSong(id, cid, cname, artist, url, image, image_small, name, duration, desc, total_rate, avg_rate);
                    arrayList.add(objItem);
                }

                return "1";
            } catch (JSONException e) {
                e.printStackTrace();
                return "0";
            } catch (Exception ee) {
                ee.printStackTrace();
                return "0";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity() != null) {
                if (s.equals("1")) {
                    progressHUD.dismissWithSuccess(getResources().getString(R.string.success));
                    if (Constant.isAppFirst) {
                        if (arrayList.size() > 0) {
                            Constant.isAppFirst = false;
                            Constant.arrayList_play.addAll(arrayList);
                            ((MainActivity) getActivity()).changeText(arrayList.get(0).getMp3Name(), arrayList.get(0).getCategoryName(), 1, arrayList.size(), arrayList.get(0).getDuration(), arrayList.get(0).getImageBig(), arrayList.get(0).getAverageRating(), "home");
                            Constant.context = getActivity();
                        }
                    }
                    viewpager.setAdapter(adapter);
                } else {
                    progressHUD.dismissWithFailure(getResources().getString(R.string.error));
                    Toast.makeText(getActivity(), getResources().getString(R.string.server_no_conn), Toast.LENGTH_SHORT).show();
                }

                new LoadArtist().execute(Constant.URL_LATEST_ARTIST);
                super.onPostExecute(s);
            }
        }
    }

    private class LoadArtist extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressHUD.show();
            arrayList_artist.clear();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                String json = JsonUtils.getJSONString(strings[0]);
                JSONObject mainJson = new JSONObject(json);
                JSONArray jsonArray = mainJson.getJSONArray(Constant.TAG_ROOT);
                JSONObject objJson;
                for (int i = 0; i < jsonArray.length(); i++) {
                    objJson = jsonArray.getJSONObject(i);

                    String id = objJson.getString(Constant.TAG_ID);
                    String name = objJson.getString(Constant.TAG_ARTIST_NAME);
                    String image = objJson.getString(Constant.TAG_ARTIST_IMAGE);
                    String thumb = objJson.getString(Constant.TAG_ARTIST_THUMB);

                    ItemArtist objItem = new ItemArtist(id, name, image, thumb);
                    arrayList_artist.add(objItem);
                }
                return "1";
            } catch (JSONException e) {
                e.printStackTrace();
                return "0";
            } catch (Exception ee) {
                ee.printStackTrace();
                return "0";
            }

        }

        @Override
        protected void onPostExecute(String s) {
            if (getActivity() != null) {
                if (s.equals("1")) {
                    progressHUD.dismissWithSuccess(getResources().getString(R.string.success));
                    adapterArtistLatest = new AdapterArtistLatest(getActivity(), arrayList_artist);
                    recyclerView_artist.setAdapter(adapterArtistLatest);
                } else {
                    progressHUD.dismissWithFailure(getResources().getString(R.string.error));
                    Toast.makeText(getActivity(), getResources().getString(R.string.server_no_conn), Toast.LENGTH_SHORT).show();
                }
                loadRecent();
                super.onPostExecute(s);
            }
        }
    }

    private void loadRecent() {
        arrayList_recent = dbHelper.loadDataRecent();
        adapterRecent = new AdapterRecent(getActivity(), arrayList_recent);
        recyclerView.setAdapter(adapterRecent);

        if (arrayList_recent.size() == 0) {
            recyclerView.setVisibility(View.GONE);
            textView_empty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            textView_empty.setVisibility(View.GONE);
        }

        if (arrayList_artist.size() == 0) {
            recyclerView_artist.setVisibility(View.GONE);
            textView_empty_artist.setVisibility(View.VISIBLE);
        } else {
            recyclerView_artist.setVisibility(View.VISIBLE);
            textView_empty_artist.setVisibility(View.GONE);
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {

        private LayoutInflater inflater;

        ImagePagerAdapter() {
            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view.equals(object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {

            View imageLayout = inflater.inflate(R.layout.viewpager_home, container, false);
            assert imageLayout != null;
            ImageView imageView = imageLayout.findViewById(R.id.imageView_pager_home);
            final ProgressBar spinner = imageLayout.findViewById(R.id.loading_home);
            TextView title = imageLayout.findViewById(R.id.textView_pager_home_title);
            TextView cat = imageLayout.findViewById(R.id.textView_pager_home_cat);
            RelativeLayout rl = imageLayout.findViewById(R.id.rl_homepager);

            title.setText(arrayList.get(position).getMp3Name());
            cat.setText(arrayList.get(position).getCategoryName());

            Picasso.with(getActivity())
                    .load(arrayList.get(position).getImageBig())
                    .placeholder(R.mipmap.app_icon)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            spinner.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            spinner.setVisibility(View.GONE);
                        }
                    });

            rl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (JsonUtils.isNetworkAvailable(getActivity())) {
                        showInter();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.internet_not_conn), Toast.LENGTH_SHORT).show();
                    }
                }
            });

            container.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private void showInter() {
        Constant.adCount = Constant.adCount + 1;
        if (Constant.adCount % Constant.adDisplay == 0) {
            ((MainActivity) getActivity()).mInterstitial.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    playIntent();
                    super.onAdClosed();
                }
            });
            if (((MainActivity) getActivity()).mInterstitial.isLoaded()) {
                ((MainActivity) getActivity()).mInterstitial.show();
                ((MainActivity) getActivity()).loadInter();
            } else {
                playIntent();
            }
        } else {
            playIntent();
        }
    }

    private void playIntent() {
        Constant.isOnline = true;
        int pos = viewpager.getCurrentItem();
        Constant.arrayList_play.clear();
        Constant.arrayList_play.addAll(arrayList);
        Constant.playPos = pos;
        ((MainActivity) getActivity()).changeText(arrayList.get(pos).getMp3Name(), arrayList.get(pos).getCategoryName(), pos + 1, arrayList.size(), arrayList.get(pos).getDuration(), arrayList.get(pos).getImageBig(), arrayList.get(pos).getAverageRating(), "home");

        Constant.context = getActivity();
        if (pos == 0) {
            Intent intent = new Intent(getActivity(), PlayerService.class);
            intent.setAction(PlayerService.ACTION_FIRST_PLAY);
            getActivity().startService(intent);
        }
    }
}