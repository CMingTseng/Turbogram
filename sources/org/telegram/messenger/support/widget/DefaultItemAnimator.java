package org.telegram.messenger.support.widget;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewPropertyAnimator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.telegram.messenger.support.widget.RecyclerView.ViewHolder;

public class DefaultItemAnimator extends SimpleItemAnimator {
    private static final boolean DEBUG = false;
    private static TimeInterpolator sDefaultInterpolator;
    private boolean delayAnimations = true;
    ArrayList<ViewHolder> mAddAnimations = new ArrayList();
    ArrayList<ArrayList<ViewHolder>> mAdditionsList = new ArrayList();
    ArrayList<ViewHolder> mChangeAnimations = new ArrayList();
    ArrayList<ArrayList<DefaultItemAnimator$ChangeInfo>> mChangesList = new ArrayList();
    ArrayList<ViewHolder> mMoveAnimations = new ArrayList();
    ArrayList<ArrayList<DefaultItemAnimator$MoveInfo>> mMovesList = new ArrayList();
    private ArrayList<ViewHolder> mPendingAdditions = new ArrayList();
    private ArrayList<DefaultItemAnimator$ChangeInfo> mPendingChanges = new ArrayList();
    private ArrayList<DefaultItemAnimator$MoveInfo> mPendingMoves = new ArrayList();
    private ArrayList<ViewHolder> mPendingRemovals = new ArrayList();
    ArrayList<ViewHolder> mRemoveAnimations = new ArrayList();

    public void runPendingAnimations() {
        boolean removalsPending = !this.mPendingRemovals.isEmpty();
        boolean movesPending = !this.mPendingMoves.isEmpty();
        boolean changesPending = !this.mPendingChanges.isEmpty();
        boolean additionsPending = !this.mPendingAdditions.isEmpty();
        if (removalsPending || movesPending || additionsPending || changesPending) {
            Iterator it = this.mPendingRemovals.iterator();
            while (it.hasNext()) {
                animateRemoveImpl((ViewHolder) it.next());
            }
            this.mPendingRemovals.clear();
            if (movesPending) {
                ArrayList<DefaultItemAnimator$MoveInfo> arrayList = new ArrayList(this.mPendingMoves);
                this.mMovesList.add(arrayList);
                this.mPendingMoves.clear();
                Runnable mover = new DefaultItemAnimator$1(this, arrayList);
                if (this.delayAnimations && removalsPending) {
                    ViewCompat.postOnAnimationDelayed(((DefaultItemAnimator$MoveInfo) arrayList.get(0)).holder.itemView, mover, getRemoveDuration());
                } else {
                    mover.run();
                }
            }
            if (changesPending) {
                ArrayList<DefaultItemAnimator$ChangeInfo> changes = new ArrayList(this.mPendingChanges);
                this.mChangesList.add(changes);
                this.mPendingChanges.clear();
                Runnable changer = new DefaultItemAnimator$2(this, changes);
                if (this.delayAnimations && removalsPending) {
                    ViewCompat.postOnAnimationDelayed(((DefaultItemAnimator$ChangeInfo) changes.get(0)).oldHolder.itemView, changer, getRemoveDuration());
                } else {
                    changer.run();
                }
            }
            if (additionsPending) {
                ArrayList<ViewHolder> additions = new ArrayList(this.mPendingAdditions);
                this.mAdditionsList.add(additions);
                this.mPendingAdditions.clear();
                Runnable adder = new DefaultItemAnimator$3(this, additions);
                if (this.delayAnimations && (removalsPending || movesPending || changesPending)) {
                    ViewCompat.postOnAnimationDelayed(((ViewHolder) additions.get(0)).itemView, adder, (removalsPending ? getRemoveDuration() : 0) + Math.max(movesPending ? getMoveDuration() : 0, changesPending ? getChangeDuration() : 0));
                } else {
                    adder.run();
                }
            }
        }
    }

    public boolean animateRemove(ViewHolder holder) {
        resetAnimation(holder);
        this.mPendingRemovals.add(holder);
        return true;
    }

    public void setDelayAnimations(boolean value) {
        this.delayAnimations = value;
    }

    private void animateRemoveImpl(ViewHolder holder) {
        View view = holder.itemView;
        ViewPropertyAnimator animation = view.animate();
        this.mRemoveAnimations.add(holder);
        animation.setDuration(getRemoveDuration()).alpha(0.0f).setListener(new DefaultItemAnimator$4(this, holder, animation, view)).start();
    }

    public boolean animateAdd(ViewHolder holder) {
        resetAnimation(holder);
        holder.itemView.setAlpha(0.0f);
        this.mPendingAdditions.add(holder);
        return true;
    }

    void animateAddImpl(ViewHolder holder) {
        View view = holder.itemView;
        ViewPropertyAnimator animation = view.animate();
        this.mAddAnimations.add(holder);
        animation.alpha(1.0f).setDuration(getAddDuration()).setListener(new DefaultItemAnimator$5(this, holder, view, animation)).start();
    }

    public boolean animateMove(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        View view = holder.itemView;
        fromX += (int) holder.itemView.getTranslationX();
        fromY += (int) holder.itemView.getTranslationY();
        resetAnimation(holder);
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            view.setTranslationX((float) (-deltaX));
        }
        if (deltaY != 0) {
            view.setTranslationY((float) (-deltaY));
        }
        this.mPendingMoves.add(new DefaultItemAnimator$MoveInfo(holder, fromX, fromY, toX, toY));
        return true;
    }

    void animateMoveImpl(ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        View view = holder.itemView;
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX != 0) {
            view.animate().translationX(0.0f);
        }
        if (deltaY != 0) {
            view.animate().translationY(0.0f);
        }
        ViewPropertyAnimator animation = view.animate();
        this.mMoveAnimations.add(holder);
        animation.setDuration(getMoveDuration()).setListener(new DefaultItemAnimator$6(this, holder, deltaX, view, deltaY, animation)).start();
    }

    public boolean animateChange(ViewHolder oldHolder, ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        if (oldHolder == newHolder) {
            return animateMove(oldHolder, fromX, fromY, toX, toY);
        }
        float prevTranslationX = oldHolder.itemView.getTranslationX();
        float prevTranslationY = oldHolder.itemView.getTranslationY();
        float prevAlpha = oldHolder.itemView.getAlpha();
        resetAnimation(oldHolder);
        int deltaX = (int) (((float) (toX - fromX)) - prevTranslationX);
        int deltaY = (int) (((float) (toY - fromY)) - prevTranslationY);
        oldHolder.itemView.setTranslationX(prevTranslationX);
        oldHolder.itemView.setTranslationY(prevTranslationY);
        oldHolder.itemView.setAlpha(prevAlpha);
        if (newHolder != null) {
            resetAnimation(newHolder);
            newHolder.itemView.setTranslationX((float) (-deltaX));
            newHolder.itemView.setTranslationY((float) (-deltaY));
            newHolder.itemView.setAlpha(0.0f);
        }
        this.mPendingChanges.add(new DefaultItemAnimator$ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY));
        return true;
    }

    void animateChangeImpl(DefaultItemAnimator$ChangeInfo changeInfo) {
        View newView;
        ViewHolder holder = changeInfo.oldHolder;
        View view = holder == null ? null : holder.itemView;
        ViewHolder newHolder = changeInfo.newHolder;
        if (newHolder != null) {
            newView = newHolder.itemView;
        } else {
            newView = null;
        }
        if (view != null) {
            ViewPropertyAnimator oldViewAnim = view.animate().setDuration(getChangeDuration());
            this.mChangeAnimations.add(changeInfo.oldHolder);
            oldViewAnim.translationX((float) (changeInfo.toX - changeInfo.fromX));
            oldViewAnim.translationY((float) (changeInfo.toY - changeInfo.fromY));
            oldViewAnim.alpha(0.0f).setListener(new DefaultItemAnimator$7(this, changeInfo, oldViewAnim, view)).start();
        }
        if (newView != null) {
            ViewPropertyAnimator newViewAnimation = newView.animate();
            this.mChangeAnimations.add(changeInfo.newHolder);
            newViewAnimation.translationX(0.0f).translationY(0.0f).setDuration(getChangeDuration()).alpha(1.0f).setListener(new DefaultItemAnimator$8(this, changeInfo, newViewAnimation, newView)).start();
        }
    }

    private void endChangeAnimation(List<DefaultItemAnimator$ChangeInfo> infoList, ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            DefaultItemAnimator$ChangeInfo changeInfo = (DefaultItemAnimator$ChangeInfo) infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item) && changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                infoList.remove(changeInfo);
            }
        }
    }

    private void endChangeAnimationIfNecessary(DefaultItemAnimator$ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(DefaultItemAnimator$ChangeInfo changeInfo, ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder != item) {
            return false;
        } else {
            changeInfo.oldHolder = null;
            oldItem = true;
        }
        item.itemView.setAlpha(1.0f);
        item.itemView.setTranslationX(0.0f);
        item.itemView.setTranslationY(0.0f);
        dispatchChangeFinished(item, oldItem);
        return true;
    }

    public void endAnimation(ViewHolder item) {
        int i;
        View view = item.itemView;
        view.animate().cancel();
        for (i = this.mPendingMoves.size() - 1; i >= 0; i--) {
            if (((DefaultItemAnimator$MoveInfo) this.mPendingMoves.get(i)).holder == item) {
                view.setTranslationY(0.0f);
                view.setTranslationX(0.0f);
                dispatchMoveFinished(item);
                this.mPendingMoves.remove(i);
            }
        }
        endChangeAnimation(this.mPendingChanges, item);
        if (this.mPendingRemovals.remove(item)) {
            view.setAlpha(1.0f);
            dispatchRemoveFinished(item);
        }
        if (this.mPendingAdditions.remove(item)) {
            view.setAlpha(1.0f);
            dispatchAddFinished(item);
        }
        for (i = this.mChangesList.size() - 1; i >= 0; i--) {
            ArrayList<DefaultItemAnimator$ChangeInfo> changes = (ArrayList) this.mChangesList.get(i);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                this.mChangesList.remove(i);
            }
        }
        for (i = this.mMovesList.size() - 1; i >= 0; i--) {
            ArrayList<DefaultItemAnimator$MoveInfo> moves = (ArrayList) this.mMovesList.get(i);
            int j = moves.size() - 1;
            while (j >= 0) {
                if (((DefaultItemAnimator$MoveInfo) moves.get(j)).holder == item) {
                    view.setTranslationY(0.0f);
                    view.setTranslationX(0.0f);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        this.mMovesList.remove(i);
                    }
                } else {
                    j--;
                }
            }
        }
        for (i = this.mAdditionsList.size() - 1; i >= 0; i--) {
            ArrayList<ViewHolder> additions = (ArrayList) this.mAdditionsList.get(i);
            if (additions.remove(item)) {
                view.setAlpha(1.0f);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    this.mAdditionsList.remove(i);
                }
            }
        }
        if (this.mRemoveAnimations.remove(item)) {
        }
        if (this.mAddAnimations.remove(item)) {
        }
        if (this.mChangeAnimations.remove(item)) {
        }
        if (this.mMoveAnimations.remove(item)) {
            dispatchFinishedWhenDone();
        } else {
            dispatchFinishedWhenDone();
        }
    }

    private void resetAnimation(ViewHolder holder) {
        if (sDefaultInterpolator == null) {
            sDefaultInterpolator = new ValueAnimator().getInterpolator();
        }
        holder.itemView.animate().setInterpolator(sDefaultInterpolator);
        endAnimation(holder);
    }

    public boolean isRunning() {
        if (this.mPendingAdditions.isEmpty() && this.mPendingChanges.isEmpty() && this.mPendingMoves.isEmpty() && this.mPendingRemovals.isEmpty() && this.mMoveAnimations.isEmpty() && this.mRemoveAnimations.isEmpty() && this.mAddAnimations.isEmpty() && this.mChangeAnimations.isEmpty() && this.mMovesList.isEmpty() && this.mAdditionsList.isEmpty() && this.mChangesList.isEmpty()) {
            return false;
        }
        return true;
    }

    void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    public void endAnimations() {
        int i;
        for (i = this.mPendingMoves.size() - 1; i >= 0; i--) {
            DefaultItemAnimator$MoveInfo item = (DefaultItemAnimator$MoveInfo) this.mPendingMoves.get(i);
            View view = item.holder.itemView;
            view.setTranslationY(0.0f);
            view.setTranslationX(0.0f);
            dispatchMoveFinished(item.holder);
            this.mPendingMoves.remove(i);
        }
        for (i = this.mPendingRemovals.size() - 1; i >= 0; i--) {
            dispatchRemoveFinished((ViewHolder) this.mPendingRemovals.get(i));
            this.mPendingRemovals.remove(i);
        }
        for (i = this.mPendingAdditions.size() - 1; i >= 0; i--) {
            ViewHolder item2 = (ViewHolder) this.mPendingAdditions.get(i);
            item2.itemView.setAlpha(1.0f);
            dispatchAddFinished(item2);
            this.mPendingAdditions.remove(i);
        }
        for (i = this.mPendingChanges.size() - 1; i >= 0; i--) {
            endChangeAnimationIfNecessary((DefaultItemAnimator$ChangeInfo) this.mPendingChanges.get(i));
        }
        this.mPendingChanges.clear();
        if (isRunning()) {
            int j;
            for (i = this.mMovesList.size() - 1; i >= 0; i--) {
                ArrayList<DefaultItemAnimator$MoveInfo> moves = (ArrayList) this.mMovesList.get(i);
                for (j = moves.size() - 1; j >= 0; j--) {
                    DefaultItemAnimator$MoveInfo moveInfo = (DefaultItemAnimator$MoveInfo) moves.get(j);
                    view = moveInfo.holder.itemView;
                    view.setTranslationY(0.0f);
                    view.setTranslationX(0.0f);
                    dispatchMoveFinished(moveInfo.holder);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        this.mMovesList.remove(moves);
                    }
                }
            }
            for (i = this.mAdditionsList.size() - 1; i >= 0; i--) {
                ArrayList<ViewHolder> additions = (ArrayList) this.mAdditionsList.get(i);
                for (j = additions.size() - 1; j >= 0; j--) {
                    item2 = (ViewHolder) additions.get(j);
                    item2.itemView.setAlpha(1.0f);
                    dispatchAddFinished(item2);
                    additions.remove(j);
                    if (additions.isEmpty()) {
                        this.mAdditionsList.remove(additions);
                    }
                }
            }
            for (i = this.mChangesList.size() - 1; i >= 0; i--) {
                ArrayList<DefaultItemAnimator$ChangeInfo> changes = (ArrayList) this.mChangesList.get(i);
                for (j = changes.size() - 1; j >= 0; j--) {
                    endChangeAnimationIfNecessary((DefaultItemAnimator$ChangeInfo) changes.get(j));
                    if (changes.isEmpty()) {
                        this.mChangesList.remove(changes);
                    }
                }
            }
            cancelAll(this.mRemoveAnimations);
            cancelAll(this.mMoveAnimations);
            cancelAll(this.mAddAnimations);
            cancelAll(this.mChangeAnimations);
            dispatchAnimationsFinished();
        }
    }

    void cancelAll(List<ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            ((ViewHolder) viewHolders.get(i)).itemView.animate().cancel();
        }
    }

    public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder, @NonNull List<Object> payloads) {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads);
    }
}
