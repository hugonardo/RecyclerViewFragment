package net.hugonardo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import net.hugonardo.recyclerviewfragment.R;

public class RecyclerViewFragment extends Fragment {

    static final int INTERNAL_EMPTY_ID = android.R.id.empty;
    static final int INTERNAL_PROGRESS_CONTAINER_ID = R.id.progress_container;
    static final int INTERNAL_LIST_CONTAINER_ID = R.id.list_container;

    final private Handler mHandler = new Handler();

    final private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };

    protected View mListContainer;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView mList;

    protected TextView mStandardEmptyView;
    protected View mProgressContainer;
    protected CharSequence mEmptyText;
    protected boolean mListShown;

    public RecyclerViewFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recyclerview_fragment, container, false);
    }

    /**
     * Attach to list view once the view hierarchy has been created.
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        mHandler.removeCallbacks(mRequestFocus);
        mList = null;
        mListShown = false;
        mProgressContainer = mListContainer = null;
        mStandardEmptyView = null;
        super.onDestroyView();
    }

    /**
     * Provide the cursor for the list view.
     *
     * @param adapter the adapter
     */
    public void setListAdapter(RecyclerView.Adapter adapter) {
        boolean hadAdapter = mAdapter != null;
        mAdapter = adapter;
        if (mList != null) {
            mList.setAdapter(adapter);
            if (!mListShown && !hadAdapter) {
                // The list was hidden, and previously didn't have an
                // adapter.  It is now time to show it.
                setListShown(true, getView().getWindowToken() != null);
            }
        }
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        ensureList();
        mList.setLayoutManager(layoutManager);
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mList.getLayoutManager();
    }

    /**
     * Get the activity's list view widget.
     *
     * @return the RecyclerView
     */
    public RecyclerView getListView() {
        ensureList();
        return mList;
    }

    /**
     * The default content for a ListFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     *
     * @param textResId
     */
    public void setEmptyText(int textResId) {
        setEmptyText(getContext().getString(textResId));
    }

    /**
     * The default content for a ListFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     *
     * @param textResId
     * @param args
     */
    public void setEmptyText(int textResId, Object... args) {
        setEmptyText(getContext().getString(textResId, args));
    }

    /**
     * The default content for a ListFragment has a TextView that can
     * be shown when the list is empty.  If you would like to have it
     * shown, call this method to supply the text it should use.
     *
     * @param text
     */
    public void setEmptyText(CharSequence text) {
        ensureList();
        if (mStandardEmptyView == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        mStandardEmptyView.setText(text);
        mEmptyText = text;
    }

    /**
     * Set Compound Drawables on EmptyTextView
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param padding
     * @see TextView#setCompoundDrawablesWithIntrinsicBounds(int, int, int, int)
     * @see TextView#setCompoundDrawablePadding(int)
     */
    public void setEmptyDrawable(int left, int top, int right, int bottom, int padding) {
        ensureList();
        mStandardEmptyView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
        mStandardEmptyView.setCompoundDrawablePadding(padding);
    }

    public TextView getEmptyTextView() {
        ensureList();
        if (mStandardEmptyView == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        return mStandardEmptyView;
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * Applications do not normally need to use this themselves.  The default
     * behavior of ListFragment is to start with the list not being shown, only
     * showing it once an adapter is given with {@link #setListAdapter(RecyclerView.Adapter)}.
     * If the list at that point had not been shown, when it does get shown
     * it will be do without the user ever seeing the hidden state.
     *
     * @param shown If true, the list view is shown; if false, the progress
     *              indicator.  The initial value is true.
     */
    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    /**
     * Like {@link #setListShown(boolean)}, but no animation is used when
     * transitioning from the previous state.
     */
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    /**
     * Control whether the list is being displayed.  You can make it not
     * displayed if you are waiting for the initial data to show in it.  During
     * this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown   If true, the list view is shown; if false, the progress
     *                indicator.  The initial value is true.
     * @param animate If true, an animation will be used to transition to the
     *                new state.
     */
    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        if (mListShown == shown) {
            return;
        }
        mListShown = shown;
        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            checkDatasetToShowList();

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        } else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(
                        getActivity(), android.R.anim.fade_out));
            } else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }
            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }

    private void checkDatasetToShowList() {
        if (hasDatasetItens()) {
            mStandardEmptyView.setVisibility(View.GONE);
            mList.setVisibility(View.VISIBLE);
        } else {
            mStandardEmptyView.setVisibility(View.VISIBLE);
            mList.setVisibility(View.GONE);
        }
    }

    private boolean hasDatasetItens() {
        return mAdapter != null && mAdapter.getItemCount() > 0;
    }

    /**
     * Get the RecyclerView.Adapter associated with this RecyclerView.
     *
     * @return
     */
    public RecyclerView.Adapter getListAdapter() {
        return mAdapter;
    }

    private void ensureList() {
        if (mList != null) {
            return;
        }
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("Content view not yet created");
        }
        if (root instanceof RecyclerView) {
            mList = (RecyclerView) root;
        } else {
            mStandardEmptyView = (TextView) root.findViewById(INTERNAL_EMPTY_ID);
            mStandardEmptyView.setVisibility(View.GONE);

            mProgressContainer = root.findViewById(INTERNAL_PROGRESS_CONTAINER_ID);
            mListContainer = root.findViewById(INTERNAL_LIST_CONTAINER_ID);
            View rawListView = root.findViewById(android.R.id.list);
            if (!(rawListView instanceof RecyclerView)) {
                if (rawListView == null) {
                    throw new RuntimeException(
                            "Your content must have a RecyclerView whose id attribute is " +
                                    "'android.R.id.list'");
                }
                throw new RuntimeException(
                        "Content has view with id attribute 'android.R.id.list' "
                                + "that is not a RecyclerView class");
            }
            mList = (RecyclerView) rawListView;
            if (mEmptyText != null) {
                mStandardEmptyView.setText(mEmptyText);
            }
        }
        mListShown = true;
        if (mAdapter != null) {
            RecyclerView.Adapter adapter = mAdapter;
            mAdapter = null;
            setListAdapter(adapter);
        } else {
            // We are starting without an adapter, so assume we won't
            // have our data right away and start with the progress indicator.
            if (mProgressContainer != null) {
                setListShown(false, false);
            }
        }
        mHandler.post(mRequestFocus);
    }
}
