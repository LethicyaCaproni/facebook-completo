
package model.dao;

import java.util.ArrayList;
import java.util.List;

import model.Comment;
import model.ModelException;
import model.Post;
import model.User;

public class MySQLCommentDAO implements CommentDAO {

    @Override
    public boolean save(Comment comment) throws ModelException {
        DBHandler db = new DBHandler();

        String sql = "INSERT INTO comments "
                + "(content, comment_date, post_id, user_id) "
                + "VALUES (?, CURDATE(), ?, ?)";

        db.prepareStatement(sql);
        db.setString(1, comment.getContent());
        db.setInt(2, comment.getPost().getId());
        db.setInt(3, comment.getUser().getId());

        return db.executeUpdate() > 0;
    }

    @Override
    public boolean update(Comment comment) throws ModelException {
        DBHandler db = new DBHandler();

        String sql = "UPDATE comments "
                + "SET content = ?, post_id = ?, user_id = ? "
                + "WHERE id = ?";

        db.prepareStatement(sql);
        db.setString(1, comment.getContent());
        db.setInt(2, comment.getPost().getId());
        db.setInt(3, comment.getUser().getId());
        db.setInt(4, comment.getId());

        return db.executeUpdate() > 0;
    }

    @Override
    public boolean delete(Comment comment) throws ModelException {
        DBHandler db = new DBHandler();

        String sql = "DELETE FROM comments WHERE id = ?";

        db.prepareStatement(sql);
        db.setInt(1, comment.getId());

        return db.executeUpdate() > 0;
    }

    @Override
    public List<Comment> listAll() throws ModelException {
        DBHandler db = new DBHandler();
        List<Comment> comments = new ArrayList<>();

        String sql = "SELECT * FROM comments ORDER BY id";

        db.createStatement();
        db.executeQuery(sql);

        while (db.next()) {
            comments.add(createComment(db));
        }

        return comments;
    }

    @Override
    public Comment findById(int id) throws ModelException {
        DBHandler db = new DBHandler();

        String sql = "SELECT * FROM comments WHERE id = ?";

        db.prepareStatement(sql);
        db.setInt(1, id);
        db.executeQuery();

        Comment comment = null;

        if (db.next()) {
            comment = createComment(db);
        }

        return comment;
    }

    private Comment createComment(DBHandler db) throws ModelException {
        Comment comment = new Comment(db.getInt("id"));

        comment.setContent(db.getString("content"));
        comment.setCommentDate(db.getDate("comment_date"));

        int postId = db.getInt("post_id");
        int userId = db.getInt("user_id");

        PostDAO postDAO = DAOFactory.createDAO(PostDAO.class);
        UserDAO userDAO = DAOFactory.createDAO(UserDAO.class);

        Post post = postDAO.findById(postId);
        User user = userDAO.findById(userId);

        comment.setPost(post);
        comment.setUser(user);

        return comment;
    }
}
