package com.omnilog.controller;

import java.io.IOException;

import com.omnilog.dao.UserDAO;
import com.omnilog.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class AuthServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        if ("logout".equals(action)) {
            req.getSession().invalidate();
            resp.sendRedirect("auth?action=login");
            return;
        }
        if ("login".equals(action) || action == null) {
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        } else if ("register".equals(action)) {
            req.getRequestDispatcher("register.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        if ("login".equals(action)) {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                HttpSession session = req.getSession();
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userName", user.getDisplayName());
                session.setAttribute("avatar", user.getAvatarUrl());
                resp.sendRedirect("media?action=dashboard");
            } else {
                req.setAttribute("error", "Invalid username or password.");
                req.getRequestDispatcher("login.jsp").forward(req, resp);
            }
        } else if ("register".equals(action)) {
            String username = req.getParameter("username");
            String email = req.getParameter("email");
            String displayName = req.getParameter("displayName");
            String password = req.getParameter("password");
            String confirm = req.getParameter("confirmPassword");

            if (username == null || username.isBlank() ||
                email == null || email.isBlank() ||
                password == null || password.length() < 6) {
                req.setAttribute("error", "Please fill all fields. Password must be at least 6 characters.");
                req.getRequestDispatcher("register.jsp").forward(req, resp);
                return;
            }
            if (!password.equals(confirm)) {
                req.setAttribute("error", "Passwords do not match.");
                req.getRequestDispatcher("register.jsp").forward(req, resp);
                return;
            }
            User user = new User(username, email, null,
                    displayName == null || displayName.isBlank() ? username : displayName);
            if (userDAO.register(user, password)) {
                HttpSession session = req.getSession();
                session.setAttribute("userId", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("userName", user.getDisplayName());
                session.setAttribute("avatar", user.getAvatarUrl());
                resp.sendRedirect("media?action=dashboard");
            } else {
                req.setAttribute("error", "Username or email already exists.");
                req.getRequestDispatcher("register.jsp").forward(req, resp);
            }
        }
    }
}