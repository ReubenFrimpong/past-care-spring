-- Create event_images table for photo gallery
CREATE TABLE event_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    caption VARCHAR(500),
    display_order INT NOT NULL DEFAULT 0,
    is_cover_image BOOLEAN NOT NULL DEFAULT FALSE,
    uploaded_by_id BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_images_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_images_user FOREIGN KEY (uploaded_by_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Indexes for better performance
CREATE INDEX idx_event_images_event_id ON event_images(event_id);
CREATE INDEX idx_event_images_display_order ON event_images(event_id, display_order);
CREATE INDEX idx_event_images_cover ON event_images(event_id, is_cover_image);
