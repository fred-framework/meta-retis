SUMMARY = "RETIS Lab minimal image"
LICENSE = "MIT"
# This image DOES NOT provide wifi, ethernet, and dev tools

inherit core-image

IMAGE_INSTALL_append = " vim nano htop"
