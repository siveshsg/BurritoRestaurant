package aus.restaurant.models;

//Represents a user and all the getters and setts

public class User {
    private int userId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private boolean isVip;
    private String email;
    private int credits;

    public User(int userId, String username, String password, String firstName, String lastName, boolean isVip, String email, int credits) {
        this.userId = userId;
        this.credits = credits;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isVip = isVip;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }


    public boolean isVip() {
        return isVip;
    }

    public String getEmail() {
        return email;
    }

    public int getCredits() {
        return credits;
    }
    public int getUserId() {
        return userId;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }
    public User(String username, boolean isVip) {
        this.username = username;
        this.isVip = isVip;

    }

    public void setVip(boolean vip) {
        isVip = vip;
    }
}
