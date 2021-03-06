package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public @Data class User {
    String nickname;
    String password;
    String firstName;
    String lastName;
    String birthDate;
    String bio;

    public String toString() {
        StringBuilder builder = new StringBuilder();
//        builder.append(nickname).append("\n");
        builder.append("Name: ").append(firstName).append(" ").append(lastName).append("\n");
        builder.append("Birthdate: ").append(birthDate).append("\n");
        builder.append("Bio: ").append(bio);
        return builder.toString();
    }
}
