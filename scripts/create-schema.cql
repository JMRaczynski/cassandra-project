USE app;

CREATE TABLE Users (
  nick varchar,
  password varchar,
  firstName varchar,
  lastName varchar,
  birthDate varchar,
  bio varchar,
  PRIMARY KEY (nick)
);

CREATE TABLE Followers (
  nick varchar,
  followerNick varchar,
  followerFirstName varchar,
  followerLastName varchar,
  followerBirthDate varchar,
  followerBio varchar,
  PRIMARY KEY ((nick), followerNick)
);

CREATE TABLE Following (
  nick varchar,
  followingNick varchar,
  followingFirstName varchar,
  followingLastName varchar,
  followingBirthDate varchar,
  followingBio varchar,
  PRIMARY KEY ((nick), followingNick)
);

CREATE TABLE Posts (
  authorNick varchar,
  creationDate timestamp,
  text varchar,
  PRIMARY KEY ((authorNick), creationDate)
);
