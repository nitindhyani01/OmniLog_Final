package com.omnilog.util;

import com.omnilog.dao.MediaDAO;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class DataSeeder implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            MediaDAO dao = new MediaDAO();

            int existing = dao.getAllMedia().size();
            if (existing >= 30) {
                System.out.println("[OmniLog] Library already has " + existing
                        + " items - skipping fetch.");
                return;
            }

            System.out.println("[OmniLog] Library is empty. Fetching media from external APIs...");
            System.out.println("[OmniLog] First startup may take 30-60 seconds.");

            DataFetcher fetcher = new DataFetcher(dao);
            int total = fetcher.fetchAll();

            System.out.println("[OmniLog] Done. Fetched " + total + " items.");

        } catch (Throwable t) {
            System.out.println("[OmniLog] Fetch failed: " + t.getMessage());
            t.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) { /* nothing */ }
}