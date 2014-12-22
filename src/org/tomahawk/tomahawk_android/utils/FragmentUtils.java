/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2014, Enno Gottschalk <mrmaffen@googlemail.com>
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
package org.tomahawk.tomahawk_android.utils;

import org.tomahawk.libtomahawk.collection.Album;
import org.tomahawk.libtomahawk.collection.Artist;
import org.tomahawk.libtomahawk.collection.CollectionManager;
import org.tomahawk.libtomahawk.collection.Playlist;
import org.tomahawk.libtomahawk.collection.PlaylistEntry;
import org.tomahawk.libtomahawk.infosystem.SocialAction;
import org.tomahawk.libtomahawk.infosystem.User;
import org.tomahawk.libtomahawk.resolver.Query;
import org.tomahawk.tomahawk_android.R;
import org.tomahawk.tomahawk_android.TomahawkApp;
import org.tomahawk.tomahawk_android.activities.TomahawkMainActivity;
import org.tomahawk.tomahawk_android.fragments.CollectionPagerFragment;
import org.tomahawk.tomahawk_android.fragments.ContentHeaderFragment;
import org.tomahawk.tomahawk_android.fragments.ContextMenuFragment;
import org.tomahawk.tomahawk_android.fragments.SocialActionsFragment;
import org.tomahawk.tomahawk_android.fragments.TomahawkFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

/**
 * This class wraps all functionality that handles the switching of {@link Fragment}s, whenever the
 * user navigates to a new {@link Fragment}.
 */
public class FragmentUtils {

    public static final String FRAGMENT_TAG = "the_ultimate_tag";

<<<<<<< HEAD
    public static void addRootFragment(TomahawkMainActivity activity,
<<<<<<< HEAD
            FragmentManager fragmentManager) {
        HatchetAuthenticatorUtils hatchetAuthUtils =
                (HatchetAuthenticatorUtils) AuthenticatorManager.getInstance()
                        .getAuthenticatorUtils(TomahawkApp.PLUGINNAME_HATCHET);
        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (hatchetAuthUtils.isLoggedIn()) {
            Bundle bundle = new Bundle();
            bundle.putString(TomahawkFragment.TOMAHAWK_USER_ID,
                    hatchetAuthUtils.getLoggedInUser().getId());
=======
            FragmentManager fragmentManager, User loggedInUser) {
        FragmentTransaction ft = fragmentManager.beginTransaction();
=======
    /**
     * Add a root fragment as the first fragment the user is seeing after opening the app.
     *
     * @param activity     TomahawkMainActivity needed as a context object and to make sure the
     *                     SlidingLayoutPanel is collapsed
     * @param loggedInUser the currently logged-in user object. determines whether to show the feed
     *                     fragment or collection fragment
     */
    public static void addRootFragment(TomahawkMainActivity activity, User loggedInUser) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
>>>>>>> c0156f1bb5707f858c9a6d0484b770841453ba54
        if (loggedInUser != null) {
            Bundle bundle = new Bundle();
            bundle.putString(TomahawkFragment.TOMAHAWK_USER_ID, loggedInUser.getId());
>>>>>>> upstream/master
            bundle.putInt(TomahawkFragment.SHOW_MODE, SocialActionsFragment.SHOW_MODE_DASHBOARD);
            bundle.putInt(ContentHeaderFragment.MODE, ContentHeaderFragment.MODE_ACTIONBAR_FILLED);
            ft.add(R.id.content_viewer_frame,
                    Fragment.instantiate(activity, SocialActionsFragment.class.getName(), bundle),
                    FRAGMENT_TAG);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString(CollectionManager.COLLECTION_ID,
                    TomahawkApp.PLUGINNAME_USERCOLLECTION);
            bundle.putInt(ContentHeaderFragment.MODE, ContentHeaderFragment.MODE_HEADER_STATIC);
            ft.add(R.id.content_viewer_frame,
                    Fragment.instantiate(activity, CollectionPagerFragment.class.getName(), bundle),
                    FRAGMENT_TAG);
        }
        ft.commit();
    }

    /**
     * Replaces the current {@link Fragment}. This method also automatically collapses the
     * SlidingPanel.
     *
     * @param activity TomahawkMainActivity needed as a context object and to make sure the
     *                 SlidingLayoutPanel is collapsed
     * @param clss     Class of the Fragment to instantiate
     * @param bundle   Bundle which contains arguments (can be null)
     */
    public static void replace(TomahawkMainActivity activity, Class clss, Bundle bundle) {
        replace(activity, clss, bundle, R.id.content_viewer_frame);
    }

    /**
     * Replaces the current {@link Fragment}. This method also automatically collapses the
     * SlidingPanel.
     *
     * @param activity   TomahawkMainActivity needed as a context object and to make sure the
     *                   SlidingLayoutPanel is collapsed
     * @param clss       Class of the Fragment to instantiate
     * @param bundle     Bundle which contains arguments (can be null)
     * @param frameResId the resource id of the ViewGroup in which the Fragment will be replaced
     */
    public static void replace(TomahawkMainActivity activity, Class clss, Bundle bundle,
            int frameResId) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.replace(frameResId, Fragment.instantiate(activity, clss.getName(), bundle),
                FRAGMENT_TAG);
        ft.addToBackStack(FRAGMENT_TAG);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
        activity.collapsePanel();
    }

    /**
     * Add the given {@link Fragment}
     *
     * @param activity   TomahawkMainActivity needed as a context object and to make sure the
     *                   SlidingLayoutPanel is collapsed
     * @param clss       Class of the Fragment to instantiate
     * @param bundle     Bundle which contains arguments (can be null)
     * @param frameResId the resource id of the ViewGroup in which the Fragment will be replaced
     */
    public static void add(TomahawkMainActivity activity, Class clss, Bundle bundle,
            int frameResId) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(frameResId, Fragment.instantiate(activity, clss.getName(), bundle), FRAGMENT_TAG);
        ft.addToBackStack(FRAGMENT_TAG);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    /**
     * Show the context menu for the given item in the given context. This method also automatically
     * collapses the SlidingPanel.
     *
     * @param activity    TomahawkMainActivity needed as a context object and to make sure the
     *                    SlidingLayoutPanel is collapsed
     * @param item        The Object for which to show the context menu
     * @param contextItem The TomahawkListItem which indicates the context of the given item. E.g. a
     *                    Track (item) in an Album (contextItem)
     */
    public static boolean showContextMenu(TomahawkMainActivity activity, Object item,
            TomahawkListItem contextItem) {
        activity.collapsePanel();
        return showContextMenu(activity, item, contextItem, R.id.content_viewer_frame);
    }

    /**
     * Show the context menu for the given item in the given context.
     *
     * @param activity    TomahawkMainActivity needed as a context object and to make sure the
     *                    SlidingLayoutPanel is collapsed
     * @param item        The TomahawkListItem for which to show the context menu
     * @param contextItem The TomahawkListItem which indicates the context of the given item. E.g. a
     *                    Track (item) in an Album (contextItem)
     * @param frameResId  the resource id of the ViewGroup in which the Fragment will be replaced
     */
    public static boolean showContextMenu(TomahawkMainActivity activity, Object item,
            TomahawkListItem contextItem, int frameResId) {
        if (item == null
                || (item instanceof SocialAction
                && (((SocialAction) item).getTargetObject() instanceof User
                || ((SocialAction) item).getTargetObject() instanceof Playlist))
                || item instanceof User
                || item instanceof Playlist) {
            return false;
        }

        Bundle args = new Bundle();
        if (contextItem instanceof Album) {
            args.putString(TomahawkFragment.TOMAHAWK_ALBUM_KEY, contextItem.getCacheKey());
        } else if (contextItem instanceof Playlist) {
            args.putString(TomahawkFragment.TOMAHAWK_PLAYLIST_KEY,
                    ((Playlist) contextItem).getId());
        } else if (contextItem instanceof Artist) {
            args.putString(TomahawkFragment.TOMAHAWK_ARTIST_KEY, contextItem.getCacheKey());
        }
        if (item instanceof Query) {
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_KEY,
                    ((Query) item).getCacheKey());
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_TYPE,
                    TomahawkFragment.TOMAHAWK_QUERY_KEY);
        } else if (item instanceof Album) {
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_KEY,
                    ((Album) item).getCacheKey());
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_TYPE,
                    TomahawkFragment.TOMAHAWK_ALBUM_KEY);
        } else if (item instanceof Artist) {
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_KEY,
                    ((Artist) item).getCacheKey());
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_TYPE,
                    TomahawkFragment.TOMAHAWK_ARTIST_KEY);
        } else if (item instanceof SocialAction) {
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_KEY,
                    ((SocialAction) item).getId());
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_TYPE,
                    TomahawkFragment.TOMAHAWK_SOCIALACTION_ID);
        } else if (item instanceof PlaylistEntry) {
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_KEY,
                    ((PlaylistEntry) item).getCacheKey());
            args.putString(TomahawkFragment.TOMAHAWK_TOMAHAWKLISTITEM_TYPE,
                    TomahawkFragment.TOMAHAWK_PLAYLISTENTRY_ID);
        }
        add(activity, ContextMenuFragment.class, args, frameResId);
        return true;
    }
}
