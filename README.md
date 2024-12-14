# ImageProcessorFX

ImageProcessorFX is a user-friendly desktop application designed to transform your images with the power of AI. Whether you're looking to enhance image quality, upscale photos, or reduce file sizes, ImageProcessorFX simplifies the process, making it accessible for everyone.

## Why Use ImageProcessorFX?

- **Improve Image Quality**: Use advanced AI models to enhance the resolution and details of your images.
- **Save Space**: Optimized `.webp` output ensures your images take up less storage without compromising quality.
- **Effortless Processing**: Batch process multiple images and subfolders in just a few clicks.
- **Versatile Support**: Compatible with `.jpg`, `.png`, and `.webp` file formats.

## Key Features

- **Simple Folder Selection**: Easily pick input and output folders through an intuitive graphical interface.
- **AI Upscaling Models**:
  - `realesrgan-x4plus` for general image enhancement.
  - `realesr-animevideov3` tailored for anime-style images.
  - `realesrnet-x4plus` for fine-tuned upscaling.
- **Batch Processing**: Process multiple images simultaneously, saving you time and effort.
- **Subfolder Inclusion**: Option to include images from subdirectories for comprehensive folder processing.
- **Progress Indicators**:
  - A progress bar shows your processing status.
  - A spinner indicates ongoing tasks.
- **Optimized Output**:
  - Converts images into `.webp` format for reduced file size.
  - Compression using `cwebp` ensures minimal storage usage.

## Getting Started

### System Requirements

To use ImageProcessorFX, ensure your system meets the following requirements:

- Java 17 or later.
- JavaFX SDK (if not bundled with your JDK).
- Executables for:
  - `realesrgan-ncnn-vulkan` (AI processing).
  - `cwebp` (compression).

Both executables must be in the working directory of the application.

### Installation

1. Clone or download the repository.
2. Place the `realesrgan-ncnn-vulkan` and `cwebp` executables in the application directory.
3. Build and run the application using your favorite IDE or terminal.

### How to Use

1. **Launch the App**: Open ImageProcessorFX on your desktop.
2. **Select Input Folder**: Use the `Browse` button to choose a folder with your images.
3. **Select Output Folder**: Define where you want the processed images saved.
4. **Choose an AI Model**: Pick the upscaling model that suits your needs from the dropdown menu.
5. **Enable Subfolder Processing (Optional)**: Include images in subdirectories if needed.
6. **Start Processing**:
  - Click the `Process` button.
  - Monitor the progress bar and spinner to track the task.
7. **View Results**: Check the output folder for enhanced and optimized images.

## Advanced Features

- **Multithreading**: Keeps the application responsive during image processing.
- **Error Notifications**: Alerts you if any issue arises, such as missing folders or invalid inputs.
- **Temporary File Management**: Automatically cleans up temporary files after processing.

### Folder Structure Example

```
project-directory/
├── realesrgan-ncnn-vulkan   # AI model Linux executable
├── cwebp                   # Compression Linux executable
├── src/                    # Application source code
└── models/                 # realesrgan models
```

## Troubleshooting

- Ensure the `realesrgan-ncnn-vulkan` and `cwebp` executables are properly configured and executable.
- Verify that JavaFX is installed if the application does not launch.
- Confirm that input and output folder permissions allow file reading and writing.

## Open Source and Credits

This project is open-source. Feel free to modify and distribute it to suit your needs.

### Acknowledgments

- **[Real-ESRGAN](https://github.com/xinntao/Real-ESRGAN)**: For providing the AI models that power the upscaling features.
- **[cwebp](https://developers.google.com/speed/webp/docs/cwebp)**: For enabling high-efficiency image compression.

By using ImageProcessorFX, you can effortlessly enhance your images and optimize their storage requirements, all through a straightforward and accessible interface.
