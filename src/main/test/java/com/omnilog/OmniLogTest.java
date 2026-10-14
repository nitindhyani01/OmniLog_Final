package com.omnilog;

import com.omnilog.dao.MediaDAO;
import com.omnilog.model.MediaItem;
import com.omnilog.util.PasswordUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OmniLogTest {

    @Test
    void passwordHashingWorks() {
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword("hunter2", salt);
        assertTrue(PasswordUtil.verifyPassword("hunter2", salt, hash),
                   "correct password should verify");
        assertFalse(PasswordUtil.verifyPassword("wrong", salt, hash),
                    "wrong password should not verify");
    }

    @Test
    void getAllMediaReturnsSeedDataWhenMongoDown() {
        List<MediaItem> items = new MediaDAO().getAllMedia();
        assertNotNull(items);
        assertFalse(items.isEmpty(), "fallback seed should not be empty");
    }

    @Test
    void getMediaByIdReturnsNullForUnknownId() {
        assertNull(new MediaDAO().getMediaById("does_not_exist"));
    }

    @Test
    void recommendForReturnsSomethingForNewUser() {
        List<MediaItem> recs = new MediaDAO().recommendFor("brand_new_user", 5);
        assertNotNull(recs);
        assertFalse(recs.isEmpty(), "cold-start user should get top-rated fallback");
    }
}