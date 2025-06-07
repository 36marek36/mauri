package com.example.mauri.model;

import com.example.mauri.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "player_id")
//    private Player player;
}
