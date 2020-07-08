package com.lmw.ijkplayer.videoList.scroll;


import com.lmw.ijkplayer.videoList.items.ListItem;

/**
 * This interface is used by {@link com.lmw.ijkplayer.videoList.calculator.SingleListViewItemActiveCalculator}.
 * Using this class to get {@link com.lmw.ijkplayer.videoList.items.ListItem}
 *
 * @author Wayne
 */
public interface ItemsProvider {

    ListItem getListItem(int position);

    int listItemSize();

}
