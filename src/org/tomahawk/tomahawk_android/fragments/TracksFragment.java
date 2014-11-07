/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2012, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package org.tomahawk.tomahawk_android.fragments;

import org.tomahawk.libtomahawk.collection.Collection;
import org.tomahawk.libtomahawk.collection.CollectionManager;
import org.tomahawk.libtomahawk.collection.CollectionUtils;
import org.tomahawk.libtomahawk.collection.Playlist;
import org.tomahawk.libtomahawk.collection.Track;
import org.tomahawk.libtomahawk.database.DatabaseHelper;
import org.tomahawk.libtomahawk.resolver.Query;
import org.tomahawk.tomahawk_android.R;
import org.tomahawk.tomahawk_android.TomahawkApp;
import org.tomahawk.tomahawk_android.activities.TomahawkMainActivity;
import org.tomahawk.tomahawk_android.adapters.Segment;
import org.tomahawk.tomahawk_android.adapters.TomahawkListAdapter;
import org.tomahawk.tomahawk_android.services.PlaybackService;
import org.tomahawk.tomahawk_android.utils.TomahawkListItem;

import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;

/**
 * {@link TomahawkFragment} which shows a set of {@link Track}s inside its {@link
 * se.emilsjolander.stickylistheaders.StickyListHeadersListView}
 */
public class TracksFragment extends TomahawkFragment {

    @Override
    public void onResume() {
        super.onResume();

        if (mContainerFragmentClass == null) {
            getActivity().setTitle("");
        }
        updateAdapter();
    }

    /**
     * Called every time an item inside a ListView or GridView is clicked
     *
     * @param view the clicked view
     * @param item the TomahawkListItem which corresponds to the click
     */
    @Override
    public void onItemClick(View view, TomahawkListItem item) {
        if (item instanceof Query) {
            Query query = (Query) item;
            if (query.isPlayable()) {
                ArrayList<Query> queries = new ArrayList<Query>();
                TomahawkMainActivity activity = (TomahawkMainActivity) getActivity();
                if (mAlbum != null) {
                    queries = CollectionUtils.getAlbumTracks(mAlbum, mCollection);
                } else if (mArtist != null) {
                    queries = CollectionUtils.getArtistTracks(mArtist, mCollection);
                } else if (mQuery != null) {
                    queries.add(mQuery);
                } else if (mSearchSongs != null) {
                    queries = mSearchSongs;
                } else {
                    Collection userCollection = CollectionManager.getInstance()
                            .getCollection(TomahawkApp.PLUGINNAME_USERCOLLECTION);
                    queries.addAll(userCollection.getQueries());
                }
                PlaybackService playbackService = activity.getPlaybackService();
                if (playbackService != null && playbackService.getCurrentQuery() == query) {
                    playbackService.playPause();
                } else {
                    Playlist playlist = Playlist.fromQueryList(DatabaseHelper.CACHED_PLAYLIST_NAME,
                            queries);
                    playlist.setId(DatabaseHelper.CACHED_PLAYLIST_ID);
                    if (playbackService != null) {
                        playbackService.setPlaylist(playlist, playlist.getEntryWithQuery(query));
                        Class clss = mContainerFragmentClass != null ? mContainerFragmentClass
                                : ((Object) this).getClass();
                        playbackService.setReturnFragment(clss, getArguments());
                        playbackService.start();
                    }
                }
            }
        }
    }

    /**
     * Update this {@link TomahawkFragment}'s {@link org.tomahawk.tomahawk_android.adapters.TomahawkListAdapter}
     * content
     */
    @Override
    protected void updateAdapter() {
        if (!mIsResumed) {
            return;
        }

        mResolveQueriesHandler.removeCallbacksAndMessages(null);
        mResolveQueriesHandler.sendEmptyMessage(RESOLVE_QUERIES_REPORTER_MSG);
        ArrayList<TomahawkListItem> queries = new ArrayList<TomahawkListItem>();
        TomahawkListAdapter tomahawkListAdapter;
        TomahawkMainActivity activity = (TomahawkMainActivity) getActivity();
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        if (mAlbum != null) {
            queries.addAll(CollectionUtils.getAlbumTracks(mAlbum, mCollection));
            Segment segment;
            if (mCollection != null) {
                segment = new Segment(mCollection.getName() + " " + getString(R.string.tracks),
                        queries);
            } else {
                segment = new Segment(R.string.album_details, queries);
            }
            if (getListAdapter() == null) {
                tomahawkListAdapter = new TomahawkListAdapter(activity, layoutInflater, segment,
                        this);
                if (CollectionUtils.allFromOneArtist(queries)) {
                    tomahawkListAdapter.setHideArtistName(true);
                    tomahawkListAdapter.setShowDuration(true);
                }
                tomahawkListAdapter.setShowNumeration(true);
                tomahawkListAdapter.setShowContentHeaderSpacerResId(
                        R.dimen.header_clear_space_scrollable, getListView());
                setListAdapter(tomahawkListAdapter);
            } else {
                getListAdapter().setSegments(segment, getListView());
            }
            showContentHeader(mAlbum, R.dimen.header_clear_space_nonscrollable);
        } else if (mArtist != null) {
            queries.addAll(CollectionUtils.getArtistTracks(mArtist, mCollection));
            Segment segment = new Segment(queries);
            if (getListAdapter() == null) {
                tomahawkListAdapter = new TomahawkListAdapter(activity, layoutInflater, segment,
                        this);
                tomahawkListAdapter.setShowDuration(true);
                tomahawkListAdapter.setShowContentHeaderSpacerResId(
                        R.dimen.header_clear_space_scrollable, getListView());
                setListAdapter(tomahawkListAdapter);
            } else {
                getListAdapter().setSegments(segment, getListView());
            }
            showContentHeader(mArtist, R.dimen.header_clear_space_nonscrollable);
        } else if (mQuery != null) {
            queries.add(mQuery);
            Segment segment = new Segment(queries);
            if (getListAdapter() == null) {
                tomahawkListAdapter = new TomahawkListAdapter(activity, layoutInflater, segment,
                        this);
                tomahawkListAdapter.setShowDuration(true);
                tomahawkListAdapter.setShowContentHeaderSpacerResId(
                        R.dimen.header_clear_space_scrollable, getListView());
                setListAdapter(tomahawkListAdapter);
            } else {
                getListAdapter().setSegments(segment, getListView());
            }
            showContentHeader(mQuery, R.dimen.header_clear_space_nonscrollable);
        } else if (mSearchSongs != null) {
            queries.addAll(mSearchSongs);
            if (getListAdapter() == null) {
                tomahawkListAdapter = new TomahawkListAdapter((TomahawkMainActivity) getActivity(),
                        layoutInflater, new Segment(queries), this);
                tomahawkListAdapter.setShowDuration(true);
                setListAdapter(tomahawkListAdapter);
            } else {
                getListAdapter().setSegments(new Segment(queries), getListView());
            }
        } else {
            queries.addAll(mCollection.getQueries());
            Segment segment = new Segment(mCollection.getName() + " " + getString(R.string.tracks),
                    queries);
            if (getListAdapter() == null) {
                tomahawkListAdapter = new TomahawkListAdapter(activity, layoutInflater, segment,
                        this);
                setListAdapter(tomahawkListAdapter);
            } else {
                getListAdapter().setSegments(segment, getListView());
            }
        }

        mShownQueries.clear();
        for (TomahawkListItem query : queries) {
            mShownQueries.add((Query) query);
        }

        updateShowPlaystate();
    }
}
