package models;

import com.avaje.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by goose on 01/06/16.
 */
@Entity
public class SwanSongExpression extends Model{

    @Id
    public String expressionId;

    public String tokenId;

    public String expression;

    public static Finder<String, SwanSongExpression> find = new Finder<String, SwanSongExpression>(SwanSongExpression.class);


}