package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public @Data class User {
    String nickname;
    String password;
    String firstName;
    String lastName;
    String birthDate;
    String bio;
}
