package kz.bapps.e_concrete.model;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by user on 14.03.16.
 */
@Table(name = "users")
public class UserModel extends TruncatableModel {

    @Column(name = "user_id")
    public int id;

    @Column(name = "email")
    public String email;

    @Column(name = "name")
    public String name;

    @Column(name = "pic_file")
    public String userPicFile;

    @Column(name = "role")
    public String role;


}
