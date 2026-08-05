package com.errorwiky.post;
import com.errorwiky.user.UserEntity;
import jakarta.persistence.*;
@Entity @Table(name="EW_POST_LIKES",uniqueConstraints=@UniqueConstraint(name="UK_EW_POST_LIKE",columnNames={"post_id","user_id"}))
public class PostLikeEntity{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="post_id") private PostEntity post;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="user_id") private UserEntity user;
 protected PostLikeEntity(){} public PostLikeEntity(PostEntity p,UserEntity u){post=p;user=u;}
}
