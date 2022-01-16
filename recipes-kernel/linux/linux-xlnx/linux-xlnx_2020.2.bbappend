SUMMARY = "RETIS Lab kernel tweaks - using a complete config file"
LICENSE = "MIT"

SRC_URI += "file://defconfig"
KERNEL_FEATURES_append = " defconfig"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

#SUMMARY = "RETIS Lab kernel tweaks - using a fragment file"
#LICENSE = "MIT"
#
#FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
## the fragment extension must be .cfg. You can add as many fragment as you want
#SRC_URI += " file://rt-frag.cfg"
#
