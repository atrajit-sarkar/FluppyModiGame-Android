"""
Script to generate app icons from a source PNG file for Android app
"""
from PIL import Image
import os

# Define the icon sizes for different densities
icon_sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

def generate_icons(source_image_path, output_base_path):
    """Generate app icons in different sizes"""
    try:
        # Open the source image
        img = Image.open(source_image_path)
        print(f"Source image size: {img.size}")
        
        # Convert to RGBA if not already
        if img.mode != 'RGBA':
            img = img.convert('RGBA')
        
        # Generate icons for each density
        for folder, size in icon_sizes.items():
            output_dir = os.path.join(output_base_path, folder)
            os.makedirs(output_dir, exist_ok=True)
            
            # Resize image
            resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
            
            # Save as PNG for both launcher and round launcher
            output_path = os.path.join(output_dir, 'ic_launcher.png')
            resized_img.save(output_path, 'PNG')
            print(f"Created: {output_path}")
            
            output_path_round = os.path.join(output_dir, 'ic_launcher_round.png')
            resized_img.save(output_path_round, 'PNG')
            print(f"Created: {output_path_round}")
        
        print("\n✓ All app icons generated successfully!")
        return True
        
    except Exception as e:
        print(f"Error: {e}")
        return False

if __name__ == "__main__":
    # Define paths
    source_image = "app-icon.png"
    output_base = os.path.join("app", "src", "main", "res")
    
    if not os.path.exists(source_image):
        print(f"Error: {source_image} not found in the current directory")
    else:
        generate_icons(source_image, output_base)
