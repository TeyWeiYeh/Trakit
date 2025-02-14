package domain;

public class User {
    public String id;
    public String email;
    public String password;

    public String base64_profile_pic;

    public User(String id, String email, String base64_profile_pic){
        this.id = id;
        this.email = email;
        this.base64_profile_pic = base64_profile_pic;
    }

    public User(String email, String base64_profile_pic){
        this.email = email;
        this.base64_profile_pic = base64_profile_pic;
    }

    //setter and getters for the user entity
    public String getId() { return id;}
    public void setId(String id){this.id = id;}
    public String getEmail() {return email;}
    public void setEmail(String email){this.email = email;}
    public String getPassword() { return password;}
    public void setPassword(String password){this.password = password;}
    public String getBase64_profile_pic() {return base64_profile_pic;}
    public void setBase64_profile_pic(String base64ProfilePic){this.base64_profile_pic = base64ProfilePic;}
}
