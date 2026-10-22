
package controller;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import model.Comment;
import model.ModelException;
import model.Post;
import model.User;
import model.dao.CommentDAO;
import model.dao.DAOFactory;
import model.dao.PostDAO;
import model.dao.UserDAO;

@WebServlet(urlPatterns = {
        "/comments",
        "/comments/create",
        "/comments/save",
        "/comments/update",
        "/comments/delete"
})
public class CommentsController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        processRequest(req, resp);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        processRequest(req, resp);
    }

    private void processRequest(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws ServletException, IOException {

        String action = req.getServletPath();
        String contextPath = req.getContextPath();

        System.out.println("CommentsController: " + action);

        switch (action) {
            case "/comments": {
                loadComments(req);

                RequestDispatcher dispatcher =
                        req.getRequestDispatcher("/comments.jsp");

                dispatcher.forward(req, resp);
                break;
            }

            case "/comments/create": {
                loadPosts(req);
                loadUsers(req);

                RequestDispatcher dispatcher =
                        req.getRequestDispatcher("/comment-form.jsp");

                dispatcher.forward(req, resp);
                break;
            }

            case "/comments/save": {
                try {
                    saveOrUpdateComment(req);
                    resp.sendRedirect(contextPath + "/comments");
                } catch (ModelException e) {
                    throw new ServletException(
                            "Erro ao salvar o comentário.", e);
                }
                break;
            }

            case "/comments/update": {
                try {
                    loadComment(req);
                    loadPosts(req);
                    loadUsers(req);
                } catch (ModelException e) {
                    throw new ServletException(
                            "Erro ao carregar o comentário.", e);
                }

                RequestDispatcher dispatcher =
                        req.getRequestDispatcher("/comment-form.jsp");

                dispatcher.forward(req, resp);
                break;
            }

            case "/comments/delete": {
                try {
                    deleteComment(req);
                    resp.sendRedirect(contextPath + "/comments");
                } catch (ModelException e) {
                    throw new ServletException(
                            "Erro ao excluir o comentário.", e);
                }
                break;
            }

            default:
                resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Rota não encontrada: " + action);
        }
    }

    private void saveOrUpdateComment(
            HttpServletRequest req
    ) throws ModelException, ServletException {

        String commentIdStr = req.getParameter("comment_id");
        String content = req.getParameter("comment_content");
        String postIdStr = req.getParameter("post_id");
        String userIdStr = req.getParameter("user_id");

        if (content == null || content.isBlank()
                || postIdStr == null || postIdStr.isBlank()
                || userIdStr == null || userIdStr.isBlank()) {
            throw new ServletException(
                    "Preencha o conteúdo, o post e o usuário.");
        }

        boolean isUpdate =
                commentIdStr != null && !commentIdStr.isBlank();

        Comment comment = isUpdate
                ? new Comment(Integer.parseInt(commentIdStr))
                : new Comment();

        comment.setContent(content.trim());

        PostDAO postDAO = DAOFactory.createDAO(PostDAO.class);
        UserDAO userDAO = DAOFactory.createDAO(UserDAO.class);
        CommentDAO commentDAO =
                DAOFactory.createDAO(CommentDAO.class);

        Post post = postDAO.findById(Integer.parseInt(postIdStr));
        User user = userDAO.findById(Integer.parseInt(userIdStr));

        if (post == null || user == null) {
            throw new ServletException(
                    "O post ou usuário selecionado não existe.");
        }

        comment.setPost(post);
        comment.setUser(user);

        boolean saved = isUpdate
                ? commentDAO.update(comment)
                : commentDAO.save(comment);

        if (!saved) {
            throw new ServletException(
                    "Nenhum comentário foi salvo ou alterado.");
        }

        System.out.println("Comentário salvo com sucesso.");
    }

    private void deleteComment(
            HttpServletRequest req
    ) throws ModelException, ServletException {

        String id = req.getParameter("commentId");

        if (id == null || id.isBlank()) {
            throw new ServletException(
                    "ID do comentário não informado.");
        }

        Comment comment = new Comment(Integer.parseInt(id));

        CommentDAO dao = DAOFactory.createDAO(CommentDAO.class);

        if (!dao.delete(comment)) {
            throw new ServletException(
                    "O comentário não foi encontrado para exclusão.");
        }
    }

    private void loadComment(
            HttpServletRequest req
    ) throws ModelException, ServletException {

        String id = req.getParameter("commentId");

        if (id == null || id.isBlank()) {
            throw new ServletException(
                    "ID do comentário não informado.");
        }

        CommentDAO dao = DAOFactory.createDAO(CommentDAO.class);
        Comment comment = dao.findById(Integer.parseInt(id));

        if (comment == null) {
            throw new ServletException("Comentário não encontrado.");
        }

        req.setAttribute("comment", comment);
    }

    private void loadComments(HttpServletRequest req)
            throws ServletException {

        CommentDAO dao = DAOFactory.createDAO(CommentDAO.class);

        try {
            List<Comment> comments = dao.listAll();
            req.setAttribute("comments_list", comments);
        } catch (ModelException e) {
            throw new ServletException(
                    "Erro ao consultar os comentários.", e);
        }
    }

    private void loadPosts(HttpServletRequest req)
            throws ServletException {

        PostDAO dao = DAOFactory.createDAO(PostDAO.class);

        try {
            List<Post> posts = dao.listAll();
            req.setAttribute("posts_list", posts);
        } catch (ModelException e) {
            throw new ServletException(
                    "Erro ao carregar os posts.", e);
        }
    }

    private void loadUsers(HttpServletRequest req)
            throws ServletException {

        UserDAO dao = DAOFactory.createDAO(UserDAO.class);

        try {
            List<User> users = dao.listAll();
            req.setAttribute("users_list", users);
        } catch (ModelException e) {
            throw new ServletException(
                    "Erro ao carregar os usuários.", e);
        }
    }
}
