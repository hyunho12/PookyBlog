package pookyBlog.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity{
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40, unique = true)
    private String username;

    @Column(unique = true)
    private String nickname;

    @Column(length = 100)
    private String password;

    @Column(length = 50, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public String getRoleValue(){
        return this.role.getValue();
    }

    @Builder
    public User(Long id, String username, String password, String nickname, String email, Role role){
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }
}
