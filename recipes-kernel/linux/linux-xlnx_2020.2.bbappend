SUMMARY = "RETIS Lab kernel tweaks - using a complete config file"
LICENSE = "MIT"

SRC_URI += "file://defconfig"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

#SUMMARY = "RETIS Lab kernel tweaks - using a fragment file"
#LICENSE = "MIT"
#
## the fragment extension must be .cfg. You can add as many fragment as you want
SRC_URI += "file://rt-frag.cfg"
KERNEL_FEATURES_append = " rt-frag.cfg"
FILESEXTRAPATHS_prepend := "${THISDIR}/files:"

