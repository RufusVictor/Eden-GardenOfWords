public class User {
    private final int user_id;
    private final String name;
    private final String email;
    private final String role;

    public User(int user_id, String name, String email, String role) {
        this.user_id = user_id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public int getUserID() { return user_id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
