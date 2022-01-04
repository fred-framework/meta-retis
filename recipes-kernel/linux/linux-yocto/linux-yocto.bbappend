# TODO:  in the future implement configurationfragments to achieve greater modularity
# https://www.yoctoproject.org/docs/3.1/kernel-dev/kernel-dev.html#creating-config-fragments
FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}:"
# you can also add patches in this list
SRC_URI += "file://defconfig"
