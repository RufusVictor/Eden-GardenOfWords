public class Comment {
    private final int commentId;
    private final int postId;
    private final String username;
    private final String content;
    private final String timestamp;
    private boolean isAuthor;

    // Constructor for fetching from DB
    public Comment(int commentId, int postId, String username, String content, String timestamp, boolean isAuthor) {
        this.commentId = commentId;
        this.postId = postId;
        this.username = username;
        this.content = content;
        this.timestamp = timestamp;
        this.isAuthor = isAuthor;
    }

    public int getCommentId() {
        return commentId;
    }

    public int getPostId() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isAuthor() {
        return isAuthor;
    }

}
