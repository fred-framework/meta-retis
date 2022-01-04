SUMMARY = "RETIS Lab production image"
LICENSE = "MIT"

inherit core-image

# features to be included
IMAGE_FEATURES += "splash"
CORE_IMAGE_EXTRA_INSTALL = "inotify-tools"
# extra tools
IMAGE_INSTALL_append = " \
    nano \
    htop \
" 

# features to be excluded
# Retis lab usualy dont use these features
DISTRO_FEATURES_remove = " bluetooth wifi 3g nfc x11 alsa touchscreen opengl wayland "
DISTRO_FEATURES_remove = " pam fbdev xen virtualization openamp"
PACKAGE_EXCLUDE = "perl5 sqlite3 udev-hwdb bluez3 bluez4"

# can-utils is inserted inthe image by this files
#/ssd/work/petalinux/2020.2/zcu102/components/yocto/layers/meta-petalinux/recipes-core/packagegroups/packagegroup-petalinux-utils.bb
#in the variable RDEPENDS_${PN}. So, to remove this tool, just write:
RDEPENDS_${PN}_remove += " can-utils"
