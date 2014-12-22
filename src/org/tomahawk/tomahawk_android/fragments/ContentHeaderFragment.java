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
package org.tomahawk.tomahawk_android.fragments;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;
import com.nineoldandroids.animation.ValueAnimator;

import org.tomahawk.libtomahawk.authentication.AuthenticatorManager;
import org.tomahawk.libtomahawk.authentication.HatchetAuthenticatorUtils;
import org.tomahawk.libtomahawk.collection.Album;
import org.tomahawk.libtomahawk.collection.Artist;
import org.tomahawk.libtomahawk.collection.Image;
import org.tomahawk.libtomahawk.collection.Playlist;
import org.tomahawk.libtomahawk.infosystem.User;
import org.tomahawk.libtomahawk.resolver.Query;
import org.tomahawk.libtomahawk.utils.TomahawkUtils;
import org.tomahawk.tomahawk_android.R;
import org.tomahawk.tomahawk_android.TomahawkApp;
import org.tomahawk.tomahawk_android.activities.TomahawkMainActivity;
import org.tomahawk.tomahawk_android.adapters.TomahawkListAdapter;
import org.tomahawk.tomahawk_android.adapters.ViewHolder;
import org.tomahawk.tomahawk_android.events.AnimatePagerEvent;
import org.tomahawk.tomahawk_android.utils.FragmentUtils;
import org.tomahawk.tomahawk_android.views.FancyDropDown;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ContentHeaderFragment extends Fragment {

    private static final String TAG = ContentHeaderFragment.class.getSimpleName();

    public static final String MODE = "org.tomahawk.tomahawk_android.mode";

    public static final int MODE_HEADER_DYNAMIC = 0;

    public static final int MODE_HEADER_DYNAMIC_PAGER = 1;

    public static final int MODE_HEADER_STATIC = 2;

    public static final int MODE_HEADER_STATIC_USER = 3;

    public static final int MODE_ACTIONBAR_FILLED = 4;

    public static final int MODE_HEADER_STATIC_SMALL = 5;

    public static final String CONTAINER_FRAGMENT_ID
            = "org.tomahawk.tomahawk_android.container_fragment_id";

    public static final String CONTAINER_FRAGMENT_PAGE
            = "org.tomahawk.tomahawk_android.container_fragment_page";

    private ValueAnimator mTextViewAnim;

    private ValueAnimator mButtonAnim;

    private ValueAnimator mImageViewAnim;

    private ValueAnimator mPageIndicatorAnim;

    protected boolean mShowFakeFollowing = false;

    protected boolean mShowFakeNotFollowing = false;

    private int mHeaderScrollableHeight = 0;

    private int mHeaderNonscrollableHeight = 0;

    private int mCurrentMode = -1;

    protected View.OnClickListener mFollowButtonListener;

    private int mLastPlayTime;

    protected long mContainerFragmentId = -1;

    protected int mContainerFragmentPage = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Resources res = getResources();
        if (getArguments() != null) {
            mCurrentMode = getArguments().getInt(MODE, -1);

            switch (mCurrentMode) {
                case MODE_HEADER_DYNAMIC:
                    mHeaderScrollableHeight = res.getDimensionPixelSize(
                            R.dimen.header_clear_space_scrollable);
                    mHeaderNonscrollableHeight = res.getDimensionPixelSize(
                            R.dimen.header_clear_space_nonscrollable);
                    break;
                case MODE_HEADER_DYNAMIC_PAGER:
                    mHeaderScrollableHeight = res.getDimensionPixelSize(
                            R.dimen.header_clear_space_scrollable);
                    mHeaderNonscrollableHeight =
                            res.getDimensionPixelSize(R.dimen.header_clear_space_nonscrollable)
                                    + res.getDimensionPixelSize(R.dimen.pager_indicator_height);
                    break;
                case MODE_HEADER_STATIC:
                    mHeaderNonscrollableHeight = res.getDimensionPixelSize(
                            R.dimen.header_clear_space_nonscrollable_static);
                    break;
                case MODE_HEADER_STATIC_USER:
                    mHeaderNonscrollableHeight = res.getDimensionPixelSize(
                            R.dimen.header_clear_space_nonscrollable_static_user);
                    break;
                case MODE_HEADER_STATIC_SMALL:
                    mHeaderNonscrollableHeight = res.getDimensionPixelSize(
                            R.dimen.abc_action_bar_default_height_material)
                            + res.getDimensionPixelSize(R.dimen.pager_indicator_height);
                    break;
                case MODE_ACTIONBAR_FILLED:
                    mHeaderNonscrollableHeight = res.getDimensionPixelSize(
                            R.dimen.abc_action_bar_default_height_material);
                    break;
                default:
                    throw new RuntimeException("Missing or invalid ContentHeaderFragment mode");
            }
            if (getArguments().containsKey(CONTAINER_FRAGMENT_ID)) {
                mContainerFragmentId = getArguments().getLong(CONTAINER_FRAGMENT_ID);
            }
            if (getArguments().containsKey(CONTAINER_FRAGMENT_PAGE)) {
                mContainerFragmentPage = getArguments().getInt(CONTAINER_FRAGMENT_PAGE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCurrentMode == MODE_ACTIONBAR_FILLED) {
            ((TomahawkMainActivity) getActivity()).showFilledActionBar();
        } else if (mCurrentMode == MODE_HEADER_STATIC_SMALL) {
            ((TomahawkMainActivity) getActivity()).showGradientActionBar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        ((TomahawkMainActivity) getActivity()).showGradientActionBar();
    }

    public boolean isDynamicHeader() {
        return mHeaderScrollableHeight > 0;
    }

    protected void showFancyDropDown(FrameLayout headerFrame, String text) {
        ViewHolder viewHolder = new ViewHolder(null, headerFrame, R.layout.content_header);
        viewHolder.setupFancyDropDown(text);
    }

    protected void showFancyDropDown(FrameLayout headerFrame, int initialSelection, String text,
            List<FancyDropDown.DropDownItemInfo> dropDownItemInfos,
            FancyDropDown.DropDownListener dropDownListener) {
        ViewHolder viewHolder = new ViewHolder(null, headerFrame, R.layout.content_header);
        viewHolder.setupFancyDropDown(initialSelection, text, dropDownItemInfos, dropDownListener);
    }

    /**
     * Show a content header. A content header provides information about the current {@link
     * org.tomahawk.tomahawk_android.utils.TomahawkListItem} that the user has navigated to. Like an
     * AlbumArt image with the {@link org.tomahawk.libtomahawk.collection.Album}s name, which is
     * shown at the top of the listview, if the user browses to a particular {@link
     * org.tomahawk.libtomahawk.collection.Album} in his {@link org.tomahawk.libtomahawk.collection.UserCollection}.
     *
     * @param item the {@link org.tomahawk.tomahawk_android.utils.TomahawkListItem}'s information to
     *             show in the header view
     */
    protected void showContentHeader(FrameLayout imageFrame, FrameLayout headerFrame,
            final Object item) {
        //Inflate views and add them into our frames
        LayoutInflater inflater = (LayoutInflater)
                TomahawkApp.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ArrayList<Image> artistImages = new ArrayList<Image>();
        if (item instanceof Playlist) {
            synchronized (item) {
                ArrayList<Artist> artists = ((Playlist) item).getContentHeaderArtists();
                for (Artist artist : artists) {
                    if (artist.getImage() != null) {
                        artistImages.add(artist.getImage());
                    }
                }
            }
        }
        int layoutId;
        int viewId;
        if (artistImages.size() > 3) {
            if (mHeaderScrollableHeight > 0) {
                layoutId = R.layout.content_header_imagegrid;
                viewId = R.id.content_header_imagegrid;
            } else {
                layoutId = R.layout.content_header_imagegrid_static;
                viewId = R.id.content_header_imagegrid_static;
            }
        } else {
            if (mHeaderScrollableHeight > 0) {
                layoutId = R.layout.content_header_imagesingle;
                viewId = R.id.content_header_imagesingle;
            } else {
                layoutId = R.layout.content_header_imagesingle_static;
                viewId = R.id.content_header_imagesingle_static;
            }
        }
        if (imageFrame.findViewById(viewId) == null) {
            imageFrame.removeAllViews();
            View headerImage = inflater.inflate(layoutId, imageFrame, false);
            if (mHeaderScrollableHeight <= 0) {
                headerImage.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mHeaderNonscrollableHeight));
            }
            imageFrame.addView(headerImage);
        }

        if (item instanceof User) {
            layoutId = R.layout.content_header_user;
            viewId = R.id.content_header_user;
        } else {
            if (mHeaderScrollableHeight > 0) {
                layoutId = R.layout.content_header;
                viewId = R.id.content_header;
            } else {
                layoutId = R.layout.content_header_static;
                viewId = R.id.content_header_static;
            }
        }
        if (headerFrame.findViewById(viewId) == null) {
            headerFrame.removeAllViews();
            View header = inflater.inflate(layoutId, headerFrame, false);
            headerFrame.addView(header);
            header.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    mHeaderScrollableHeight + mHeaderNonscrollableHeight));
        }

        //Now we fill the added views with data
        ViewHolder viewHolder = new ViewHolder(imageFrame, headerFrame, layoutId);
        if (item instanceof Integer) {
            viewHolder.fillContentHeader((Integer) item);
        } else if (item instanceof ColorDrawable) {
            viewHolder.fillContentHeader((ColorDrawable) item);
        } else if (mHeaderScrollableHeight > 0) {
            View.OnClickListener moreButtonListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentUtils.showContextMenu((TomahawkMainActivity) getActivity(), item, null);
                }
            };
            if (item instanceof Album) {
                viewHolder.fillContentHeader((Album) item, moreButtonListener);
            } else if (item instanceof Artist) {
                viewHolder.fillContentHeader((Artist) item, moreButtonListener);
            } else if (item instanceof Playlist) {
                viewHolder.fillContentHeader((Playlist) item, artistImages);
            } else if (item instanceof Query) {
                viewHolder.fillContentHeader((Query) item, moreButtonListener);
            }
        } else {
            if (item instanceof Image) {
                viewHolder.fillContentHeader((Image) item);
            } else if (item instanceof User) {
                HatchetAuthenticatorUtils authUtils =
                        (HatchetAuthenticatorUtils) AuthenticatorManager.getInstance()
                                .getAuthenticatorUtils(TomahawkApp.PLUGINNAME_HATCHET);
                boolean showFollowing, showNotFollowing;
                if (mShowFakeFollowing || mShowFakeNotFollowing) {
                    showFollowing = mShowFakeFollowing;
                    showNotFollowing = mShowFakeNotFollowing;
                } else {
                    showFollowing = item != authUtils.getLoggedInUser()
                            && authUtils.getLoggedInUser().getFollowings().containsKey(item);
                    showNotFollowing = item != authUtils.getLoggedInUser()
                            && !authUtils.getLoggedInUser().getFollowings().containsKey(item);
                }
                viewHolder.fillContentHeader((User) item, showFollowing, showNotFollowing,
                        mFollowButtonListener);
            }
        }
    }

    /**
     * Add a non-scrollable spacer to the top of the given view
     */
    protected void setupNonScrollableSpacer(View view) {
        //Add a non-scrollable spacer to the top of the given view
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(params.leftMargin, mHeaderNonscrollableHeight, params.rightMargin,
                params.bottomMargin);
        view.setLayoutParams(params);
    }

    protected void setupScrollableSpacer(TomahawkListAdapter adapter,
            StickyListHeadersListView listView) {
        if (adapter != null) {
            adapter.setShowContentHeaderSpacer(mHeaderScrollableHeight, listView);
        } else {
            Log.d(TAG, "setupScrollableSpacer - Can't call setShowContentHeaderSpacer,"
                    + " Adapter is null");
        }
    }

    protected void setupAnimations(FrameLayout imageFrame, FrameLayout headerFrame) {
        View header = headerFrame.findViewById(R.id.content_header);
        if (header == null) {
            header = headerFrame.findViewById(R.id.content_header_user);
        }
        setupFancyDropDownAnimation(header);
        setupButtonAnimation(header);
        setupPageIndicatorAnimation(header);

        View headerImage = imageFrame.findViewById(R.id.content_header_imagegrid);
        if (headerImage == null) {
            headerImage = imageFrame.findViewById(R.id.content_header_imagesingle);
        }
        setupImageViewAnimation(headerImage);

        if (mContainerFragmentId >= 0) {
            AnimatePagerEvent event = new AnimatePagerEvent();
            event.mContainerFragmentId = mContainerFragmentId;
            event.mContainerFragmentPage = mContainerFragmentPage;
            event.mPlayTime = mLastPlayTime;
            EventBus.getDefault().post(event);
        } else {
            animate(mLastPlayTime);
        }
    }

    private void setupFancyDropDownAnimation(final View view) {
        if (view != null) {
            View fancyDropDown = view.findViewById(R.id.fancydropdown);
            if (fancyDropDown != null) {
                TomahawkUtils.afterViewGlobalLayout(new TomahawkUtils.ViewRunnable(fancyDropDown) {
                    @Override
                    public void run() {
                        // correctly position fancyDropDown first
                        int dropDownHeight = TomahawkApp.getContext().getResources()
                                .getDimensionPixelSize(
                                        R.dimen.show_context_menu_icon_height);
                        getView().setY(view.getHeight() / 2 - dropDownHeight / 2);
                        getView().setX(view.getWidth() / 2 - getView().getWidth() / 2);

                        // now calculate the animation goal and instantiate the animation
                        Resources resources = TomahawkApp.getContext().getResources();
                        int smallPadding = resources
                                .getDimensionPixelSize(R.dimen.padding_small);
                        int x = resources.getDimensionPixelSize(R.dimen.padding_superlarge);
                        int actionBarHeight = resources.getDimensionPixelSize(
                                R.dimen.abc_action_bar_default_height_material);
                        int y = actionBarHeight + smallPadding;
                        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat("x", x);
                        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat("y", y);
                        mTextViewAnim = ObjectAnimator
                                .ofPropertyValuesHolder(getView(), pvhX, pvhY)
                                .setDuration(10000);
                        mTextViewAnim.setInterpolator(new LinearInterpolator());
                    }
                });
            }
        }
    }

    private void setupButtonAnimation(final View view) {
        if (view != null) {
            View buttonView = view.findViewById(R.id.morebutton1);
            if (buttonView != null) {
                TomahawkUtils.afterViewGlobalLayout(new TomahawkUtils.ViewRunnable(buttonView) {
                    @Override
                    public void run() {
                        // correctly position button first
                        Resources resources = TomahawkApp.getContext().getResources();
                        int buttonHeight = TomahawkApp.getContext().getResources()
                                .getDimensionPixelSize(
                                        R.dimen.show_context_menu_icon_height);
                        int largePadding = TomahawkApp.getContext().getResources()
                                .getDimensionPixelSize(R.dimen.padding_large);
                        int pageIndicatorHeight = 0;
                        View pageIndicator =
                                view.findViewById(R.id.page_indicator_container);
                        if (pageIndicator != null
                                && pageIndicator.getVisibility() == View.VISIBLE) {
                            pageIndicatorHeight = TomahawkApp.getContext().getResources()
                                    .getDimensionPixelSize(R.dimen.pager_indicator_height);
                        }
                        getView().setY(view.getHeight() - buttonHeight - largePadding
                                - pageIndicatorHeight);

                        // now calculate the animation goal and instantiate the animation
                        int smallPadding = resources
                                .getDimensionPixelSize(R.dimen.padding_small);
                        int actionBarHeight = resources.getDimensionPixelSize(
                                R.dimen.abc_action_bar_default_height_material);
                        int y = actionBarHeight + smallPadding;
                        mButtonAnim = ObjectAnimator.ofFloat(getView(), "y", y)
                                .setDuration(10000);
                        mButtonAnim.setInterpolator(new LinearInterpolator());
                    }
                });
            }
        }
    }

    private void setupImageViewAnimation(final View view) {
        if (view != null) {
            View imageView = view.findViewById(R.id.content_header_imagesingle);
            if (imageView == null) {
                imageView = view.findViewById(R.id.content_header_imagegrid);
            }
            if (imageView == null) {
                imageView = view.findViewById(R.id.content_header_imagesingle_static);
            }
            if (imageView != null) {
                TomahawkUtils.afterViewGlobalLayout(new TomahawkUtils.ViewRunnable(imageView) {
                    @Override
                    public void run() {
                        // correctly position imageview first
                        getView().setY(0);
                        getView().setX(0);

                        // now calculate the animation goal and instantiate the animation
                        mImageViewAnim = ObjectAnimator
                                .ofFloat(getView(), "y", view.getHeight() / -3)
                                .setDuration(10000);
                        mImageViewAnim.setInterpolator(new LinearInterpolator());
                    }
                });
            }
        }
    }

    private void setupPageIndicatorAnimation(final View view) {
        if (view != null) {
            View indicatorView = view.findViewById(R.id.page_indicator_container);
            if (indicatorView != null) {
                TomahawkUtils.afterViewGlobalLayout(new TomahawkUtils.ViewRunnable(indicatorView) {
                    @Override
                    public void run() {
                        // correctly position indicator first
                        getView().setY(view.getHeight() - getView().getHeight());

                        // now calculate the animation goal and instantiate the animation
                        int y = mHeaderNonscrollableHeight - getView().getHeight();
                        mPageIndicatorAnim = ObjectAnimator
                                .ofFloat(getView(), "y", y)
                                .setDuration(10000);
                        mPageIndicatorAnim.setInterpolator(new LinearInterpolator());
                    }
                });
            }
        }
    }

    public void animate(int position) {
        mLastPlayTime = position;
        if (mTextViewAnim != null && position != mTextViewAnim.getCurrentPlayTime()) {
            mTextViewAnim.setCurrentPlayTime(position);
        }
        if (mButtonAnim != null && position != mButtonAnim.getCurrentPlayTime()) {
            mButtonAnim.setCurrentPlayTime(position);
        }
        if (mImageViewAnim != null && position != mImageViewAnim.getCurrentPlayTime()) {
            mImageViewAnim.setCurrentPlayTime(position);
        }
        if (mPageIndicatorAnim != null && position != mPageIndicatorAnim.getCurrentPlayTime()) {
            mPageIndicatorAnim.setCurrentPlayTime(position);
        }
    }
}
