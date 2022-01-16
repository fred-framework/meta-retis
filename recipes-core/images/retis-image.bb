SUMMARY = "RETIS Lab minimal image"
LICENSE = "MIT"
# This image DOES NOT provide wifi, ethernet, and dev tools

inherit core-image
require ./recipes-core/images/petalinux-image-minimal.bb
#require ./recipes-core/images/petalinux-image-full.bb

IMAGE_INSTALL_append = " vim nano htop"
