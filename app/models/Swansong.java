package models;

import com.avaje.ebean.Model;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by goose on 01/06/16.
 */
@Entity
public class SwanSong extends Model{

    @Id
    public String expressionId;

    public String tokenId;

    public String expression;

    public static Finder<String, SwanSong> find = new Finder<String,SwanSong>(SwanSong.class);


}
