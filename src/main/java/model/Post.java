package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@AllArgsConstructor
public @Data
class Post {
    String authorNick;
    Date creationDate;
    String text;
}
