SUMMARY = "RETIS Lab minimal image"
LICENSE = "MIT"
# This image provides ssh with dropbear and basic editors. It does not provide dev tools.

inherit core-image
require ./recipes-core/images/petalinux-image-minimal.bb
#require ./recipes-core/images/petalinux-image-full.bb

# autostart: to boot without login

IMAGE_INSTALL_append = " autostart vim nano htop"
