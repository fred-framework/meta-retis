SUMMARY = "RETIS Lab minimal image"
LICENSE = "MIT"
# This image provides ssh with dropbear and basic editors. It does not provide dev tools.

inherit core-image
require ./recipes-core/images/petalinux-image-minimal.bb
#require ./recipes-core/images/petalinux-image-full.bb

# autostart: to boot without login

# for some reason, autostart is not found when this layer is run together with meta-ros
#IMAGE_INSTALL_append = " autostart vim nano htop"
IMAGE_INSTALL_append = " vim nano htop"
