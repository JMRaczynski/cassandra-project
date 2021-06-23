package model;

import lombok.Data;

import java.util.Date;

public @Data
class Post {
    String authorNick;
    Date creationDate;
    String text;
}
