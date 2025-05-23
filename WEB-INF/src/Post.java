public class Post {
    private final int postId;
    private final String title;
    private final String content;
    private final String timestamp;
    private final String username;
    private int likeCount;
    private boolean liked;
    private boolean isAuthor;

    // Constructor for fetching from DB
    public Post(int postId, String title, String content, String username, String timestamp, int likeCount,
            boolean liked, boolean isAuthor) {
        this.postId = postId;
        this.title = title;
        this.content = content;
        this.username = username;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
        this.liked = liked;
        this.isAuthor = isAuthor;
    }

    public boolean isAuthor() {
        return isAuthor;
    }

    public int getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getUsername() {
        return username;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
