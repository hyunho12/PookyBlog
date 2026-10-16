-- Apply once to an existing MySQL 8 schema before starting the updated services.
-- A fresh schema receives the same rules from the entity mappings.
-- DDL auto-commits in MySQL; back up the schema and run during a maintenance window.
ALTER TABLE post_like
    DROP FOREIGN KEY FKj7iy0k7n3d0vkh8o7ibjna884,
    ADD CONSTRAINT fk_post_like_post_cascade
        FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE;

ALTER TABLE comment
    DROP FOREIGN KEY FKs1slvnkuemjsq2kj4h3vhx7i1,
    ADD CONSTRAINT fk_comment_post_cascade
        FOREIGN KEY (post_id) REFERENCES post (id) ON DELETE CASCADE;
