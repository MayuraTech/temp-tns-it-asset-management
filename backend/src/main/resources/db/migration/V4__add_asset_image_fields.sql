-- V4__add_asset_image_fields.sql
-- Add image-related fields to Assets table for Task 28: Image Upload Functionality

-- Add image URL field
ALTER TABLE Assets 
ADD ImageUrl NVARCHAR(500) NULL;

-- Add image filename field  
ALTER TABLE Assets 
ADD ImageFilename NVARCHAR(255) NULL;

-- Add image size field (in bytes)
ALTER TABLE Assets 
ADD ImageSize BIGINT NULL;

-- Add image content type field
ALTER TABLE Assets 
ADD ImageContentType NVARCHAR(50) NULL;

-- Add index on ImageUrl for performance
CREATE INDEX IX_Assets_ImageUrl ON Assets(ImageUrl);

-- Add comments for documentation
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'URL path to the asset image file', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'Assets',
    @level2type = N'COLUMN', @level2name = N'ImageUrl';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Original filename of the uploaded image', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'Assets',
    @level2type = N'COLUMN', @level2name = N'ImageFilename';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Size of the image file in bytes', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'Assets',
    @level2type = N'COLUMN', @level2name = N'ImageSize';

EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'MIME type of the image (image/jpeg, image/png, image/webp)', 
    @level0type = N'SCHEMA', @level0name = N'dbo',
    @level1type = N'TABLE', @level1name = N'Assets',
    @level2type = N'COLUMN', @level2name = N'ImageContentType';