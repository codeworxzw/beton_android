package kz.bapps.e_concrete.model;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * Created by user on 13.03.16.
 */

@Table(name = "menu_items")
public class MenuItemModel extends TruncatableModel {

    @Column(name = "item_id")
    public int id;

    @Column(name = "id_hi")
    public int idHi;

    @Column(name = "code")
    public String code;

    @Column(name = "cnt_childs")
    public String cntChilds;

    @Column(name = "url")
    public String url;

    @Column(name = "icon")
    public String icon;

    @Column(name = "title")
    public String title;

    @Column(name = "title_ru")
    public String titleRu;

    @Column(name = "title_kk")
    public String titleKk;

    @Column(name = "title_en")
    public String titleEn;
/*
    public List<MenuItemModel> items() {
        return getMany(MenuItemModel.class, "idHi");
    }*/

}