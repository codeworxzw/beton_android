package kz.bapps.e_concrete.model;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.Date;


@Table(name = "locations")
public class LocationModel extends TruncatableModel {

    @Column(name = "lat")
    public double lat;

    @Column(name = "lng")
    public double lng;

    @Column(name = "created_at")
    public Date createdAt;

}
