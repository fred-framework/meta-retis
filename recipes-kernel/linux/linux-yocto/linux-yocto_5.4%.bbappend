SUMMARY = "RETIS Lab kernel tweaks - using a complete .config file"
LICENSE = "MIT"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
SRC_URI += " file://defconfig"

unset KBUILD_DEFCONFIG

do_configure_prepend () {
        cp "${WORKDIR}/defconfig" "${B}/.config"  
}

#SUMMARY = "RETIS Lab kernel tweaks - using a fragment file"
#LICENSE = "MIT"
#
#FILESEXTRAPATHS:prepend := "${THISDIR}/files:"
## the fragment extension must be .cfg. You can add as many fragment as you want
#SRC_URI += " file://config-rt.cfg"
#
